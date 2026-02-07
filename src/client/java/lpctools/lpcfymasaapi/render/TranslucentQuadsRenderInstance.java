package lpctools.lpcfymasaapi.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.doubles.DoubleComparators;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import lpctools.lpcfymasaapi.Registries;
import lpctools.util.CachedSupplier;
import lpctools.util.javaex.QuietAutoCloseable;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.Frustum;
import net.minecraft.util.Util;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

class TranslucentQuadsRenderInstance implements QuietAutoCloseable, Registries.WorldPreWeatherRender, Registries.WorldLastRender {
	private static final String baseLabel = "LPCTools TranslucentQuadsRenderInstance";
	private static final RenderPipeline pipeline = RenderPipelines.DEBUG_QUADS;
	private static final int vertexPerQuad = 4;
	private static final int sizePerQuad = vertexPerQuad * pipeline.getVertexFormat().getVertexSize();
	private static final Supplier<String> indexBufferLabel = () -> appendLabel("IndexBuffer");
	private static final Supplier<String> vertexBufferLabel = () -> appendLabel("VertexBuffer");
	private static final Supplier<String> renderPassLabel = () -> appendLabel("RenderPass");
	
	private static String appendLabel(String tail) {return baseLabel + ' ' + tail;}
	
	private final Long2ObjectOpenHashMap<SubChunk> subChunks = new Long2ObjectOpenHashMap<>();
	private final IntArrayList unusedQuadIndexes = new IntArrayList();
	// 变换基点，所有vertex以此为基点进行变换
	private final Vector3d basePoint = new Vector3d();
	private final Vector3d vecCache = new Vector3d();
	private final ArrayList<SubChunk> renderSubChunks = new ArrayList<>();
	private final ArrayList<SubChunk> subChunksNeedToUpload = new ArrayList<>();
	private final LongOpenHashSet probablyEmptySubChunks = new LongOpenHashSet();
	private final IntComparator subChunkSortComparator = (i1, i2) -> DoubleComparators.OPPOSITE_COMPARATOR.compare(this.distanceCache[i1], this.distanceCache[i2]);
	private final CachedSupplier<ArrayList<ArrayList<SubChunk>>> subChunkSortingCache = new CachedSupplier<>(ArrayList::new);
	
	private GpuBuffer vertexBuffer = null;
	private ByteBuffer bufferCache = null;
	private Quad[] quads = new Quad[0];
	private double[] distanceCache = new double[0];
	private boolean vertexUploaded = false;
	private CompletableFuture<Void> prepareTasks;
	
	TranslucentQuadsRenderInstance() {
		Registries.RENDER_WORLD_PRE_WEATHER.register(this);
		Registries.WORLD_RENDER_LAST.register(this);
	}
	
	public int addQuad(Quad quad) {
		waitForTasks();
		if (unusedQuadIndexes.isEmpty()) {
			var lastQuads = quads;
			quads = new Quad[Math.max(lastQuads.length * 2, 16)];
			distanceCache = new double[quads.length];
			System.arraycopy(lastQuads, 0, quads, 0, lastQuads.length);
			if (vertexBuffer != null) vertexBuffer.close();
			int size = quads.length * sizePerQuad;
			vertexBuffer = RenderSystem.getDevice().createBuffer(vertexBufferLabel, GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST, size);
			if (bufferCache != null) MemoryUtil.memFree(bufferCache);
			bufferCache = MemoryUtil.memAlloc(size);
			for (int i = lastQuads.length; i < quads.length; ++i) unusedQuadIndexes.add(i);
			vertexUploaded = false;
		}
		int index = unusedQuadIndexes.popInt();
		if (quads[index] != null) quads[index].set(quad);
		else quads[index] = new Quad(quad);
		if (vertexUploaded) {
			bufferCache.clear();
			appendQuadToByteBuffer(quads[index]);
			bufferCache.flip();
			RenderSystem.getDevice().createCommandEncoder()
				.writeToBuffer(vertexBuffer.slice(index * sizePerQuad, sizePerQuad), bufferCache);
		}
		long pos = quad.getPackedCenterSectionPos();
		SubChunk chunk;
		if (subChunks.containsKey(pos)) chunk = subChunks.get(pos);
		else subChunks.put(pos, chunk = new SubChunk(ChunkSectionPos.from(pos)));
		chunk.putQuad(index);
		return index;
	}
	
	public void removeQuad(int index) {
		waitForTasks();
		var quad = quads[index];
		if(quad == null) throw new IllegalArgumentException();
		long pos = quad.getPackedCenterSectionPos();
		var subChunk = subChunks.get(pos);
		if(subChunk == null) throw new IllegalArgumentException();
		subChunk.removeQuad(index);
		unusedQuadIndexes.add(index);
		if(subChunk.subChunkQuads == 0)
			probablyEmptySubChunks.add(pos);
	}
	
	@Override public void onLast(Registries.WorldRenderContext context) {
		waitForTasks();
		var commandEncoder = RenderSystem.getDevice().createCommandEncoder();
		for (var subChunk : subChunksNeedToUpload)
			commandEncoder.writeToBuffer(subChunk.indexBufferSlice, subChunk.byteBufferCache);
		subChunksNeedToUpload.clear();
		var fb = context.fb();
		GpuTextureView colorAttachmentView = fb.getColorAttachmentView();
		GpuTextureView depthAttachmentView = fb.useDepthAttachment ? fb.getDepthAttachmentView() : null;
		var camPos = context.camera().getCameraPos();
		GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
			.write(RenderSystem.getModelViewMatrix().translate(new Vector3f((float) (basePoint.x - camPos.x), (float) (basePoint.y - camPos.y), (float) (basePoint.z - camPos.z)), new Matrix4f()),
				new Vector4f(1.0F, 1.0F, 1.0F, 1.0F), new Vector3f(), new Matrix4f(), 0.0F);
		GpuBufferSlice projection = RenderSystem.getProjectionMatrixBuffer();
		try (RenderPass renderPass = commandEncoder
			.createRenderPass(renderPassLabel, colorAttachmentView, OptionalInt.empty(), depthAttachmentView, OptionalDouble.empty())) {
			renderPass.setPipeline(pipeline);
			renderPass.setUniform("DynamicTransforms", dynamicTransforms);
			renderPass.setUniform("Projection", projection);
			renderPass.setVertexBuffer(0, vertexBuffer);
			for (var subChunk : renderSubChunks)
				subChunk.render(renderPass);
		}
	}
	
	@Override public void onRenderWorldPreWeather(Registries.WorldRenderContext context) {
		prepareRenderDataAsync(context, Util.getMainWorkerExecutor(), true);
	}
	
	public void prepareRenderDataAsync(Registries.WorldRenderContext context, Executor executor, boolean recordData) {
		waitForTasks();
		if(!probablyEmptySubChunks.isEmpty()){
			for(long pos : probablyEmptySubChunks) {
				var subChunk = subChunks.get(pos);
				if(subChunk.subChunkQuads == 0)
					subChunks.remove(pos).close();
			}
			probablyEmptySubChunks.clear();
		}
		final Frustum frustum = recordData ? new Frustum(context.frustum()) : context.frustum();
		prepareTasks = CompletableFuture.supplyAsync(() -> dispatchPrepareTasks(frustum, context.camera().getCameraPos(), executor), executor)
			.thenCompose(tasks -> tasks);
	}
	
	@Override public void close() {
		waitForTasks();
		for (SubChunk subChunk : subChunks.values()) subChunk.close();
		subChunks.clear();
		Registries.RENDER_WORLD_PRE_WEATHER.unregister(this);
		Registries.WORLD_RENDER_LAST.unregister(this);
	}
	
	private void appendPositionToByteBuffer(Vector3d pos) {
		bufferCache.putFloat((float) (pos.x - basePoint.x)).putFloat((float) (pos.y - basePoint.y)).putFloat((float) (pos.z - basePoint.z));
	}
	
	private void appendQuadToByteBuffer(Quad quad) {
		appendPositionToByteBuffer(quad.base);
		bufferCache.putInt(quad.color);
		appendPositionToByteBuffer(quad.base.add(quad.x, vecCache));
		bufferCache.putInt(quad.color);
		appendPositionToByteBuffer(quad.base.add(quad.y, vecCache));
		bufferCache.putInt(quad.color);
		appendPositionToByteBuffer(vecCache.add(quad.x));
		bufferCache.putInt(quad.color);
	}
	
	private void waitForTasks() {
		if (prepareTasks == null) return;
		prepareTasks.join();
		prepareTasks = null;
		if (!vertexUploaded) {
			RenderSystem.getDevice().createCommandEncoder().writeToBuffer(vertexBuffer.slice(), bufferCache);
			vertexUploaded = true;
		}
	}
	
	private CompletableFuture<Void> dispatchPrepareTasks(Frustum frustum, Vec3d camPos, Executor executor) {
		ArrayList<CompletableFuture<Void>> tasks = new ArrayList<>();
		if (!vertexUploaded) {
			bufferCache.clear();
			for (Quad quad : quads) appendQuadToByteBuffer(quad == null ? Quad.EMPTY_QUAD : quad);
			bufferCache.flip();
		}
		renderSubChunks.clear();
		
		// 计数排序
		int camSectionX = ChunkSectionPos.getSectionCoord(camPos.x);
		int camSectionY = ChunkSectionPos.getSectionCoord(camPos.y);
		int camSectionZ = ChunkSectionPos.getSectionCoord(camPos.z);
		var cacheList = subChunkSortingCache.get();
		for(var list : cacheList) list.clear();
		for(var subChunk : subChunks.values()){
			var sectionPos = subChunk.sectionPos;
			int manhattanDistance = Math.abs(camSectionX - sectionPos.getX()) + Math.abs(camSectionY - sectionPos.getY()) + Math.abs(camSectionZ - sectionPos.getZ());
			while(cacheList.size() <= manhattanDistance) cacheList.add(new ArrayList<>());
			cacheList.get(manhattanDistance).add(subChunk);
		}
		
		for(int i = cacheList.size() - 1; i >= 0; --i) {
			for(var subChunk : cacheList.get(i)) {
				var task = subChunk.updateIfVisible(frustum, camPos, executor);
				if (task == null) continue;
				tasks.add(task);
			}
		}
		
		return CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0]));
	}
	
	private class SubChunk implements QuietAutoCloseable {
		static final int[] quadBaseIndex = {0, 1, 2, 2, 1, 3};
		static final int indexPerQuad = quadBaseIndex.length;
		static final VertexFormat.IndexType indexType = VertexFormat.IndexType.INT;
		final Vector3d markerPos = new Vector3d();
		final Vector3d cacheVec = new Vector3d();
		final ChunkSectionPos sectionPos;
		final Box sectionBox;
		GpuBuffer indexBuffer = null;
		GpuBufferSlice indexBufferSlice = null;
		ByteBuffer byteBufferCache = null;
		int[] quadIndexes = new int[0];
		int subChunkQuads = 0;
		boolean forceUpdate = false;
		// 由全局index获取其在此subChunk中的index的map, quadIndexes[index2indexMap.getInt(i)] == i (if exist)
		@Nullable Int2IntOpenHashMap index2indexMap = null;
		
		SubChunk(ChunkSectionPos sectionPos) {
			this.sectionPos = sectionPos;
			sectionBox = new Box(sectionPos.getMinX(), sectionPos.getMinY(), sectionPos.getMinZ(),
				sectionPos.getMinX() + 16, sectionPos.getMinY() + 16, sectionPos.getMinZ() + 16);
		}
		
		void putQuad(int index) {
			if (subChunkQuads + 1 > quadIndexes.length) {
				var oldIndexes = quadIndexes;
				quadIndexes = new int[Math.max(oldIndexes.length, 16)];
				System.arraycopy(oldIndexes, 0, quadIndexes, 0, oldIndexes.length);
				if (indexBuffer != null) indexBuffer.close();
				if (byteBufferCache != null) MemoryUtil.memFree(byteBufferCache);
				int size = quadIndexes.length * indexPerQuad * indexType.size;
				indexBuffer = RenderSystem.getDevice().createBuffer(indexBufferLabel, GpuBuffer.USAGE_INDEX | GpuBuffer.USAGE_COPY_DST, size);
				indexBufferSlice = indexBuffer.slice();
				byteBufferCache = MemoryUtil.memAlloc(size);
			}
			quadIndexes[subChunkQuads++] = index;
			forceUpdate = true;
		}
		
		void removeQuad(int index){
			if(index2indexMap == null){
				index2indexMap = new Int2IntOpenHashMap();
				for(int i = 0; i < subChunkQuads; ++i)
					index2indexMap.put(quadIndexes[i], i);
			}
			int i = index2indexMap.getOrDefault(index, -1);
			if(i < 0) throw new IllegalArgumentException("Index not exist!");
			index2indexMap.remove(index);
			if(i == --subChunkQuads) return;
			quadIndexes[i] = quadIndexes[subChunkQuads];
		}
		
		CompletableFuture<Void> updateIfVisible(Frustum frustum, Vec3d camPos, Executor executor) {
			if (!frustum.isVisible(sectionBox)) return null;
			renderSubChunks.add(this);
			if (updateMarker(camPos)) {
				subChunksNeedToUpload.add(this);
				return CompletableFuture.runAsync(() -> resort(camPos), executor);
			} else return null;
		}
		
		// 返回值表示是否需要resort
		boolean updateMarker(Vec3d camPos) {
			double markerX = Math.clamp(camPos.x, sectionPos.getMinX(), sectionPos.getMinX() + 16);
			double markerY = Math.clamp(camPos.y, sectionPos.getMinY(), sectionPos.getMinY() + 16);
			double markerZ = Math.clamp(camPos.z, sectionPos.getMinZ(), sectionPos.getMinZ() + 16);
			var res = forceUpdate || markerX != markerPos.x || markerY != markerPos.y || markerZ != markerPos.z;
			if (res) {
				markerPos.set(markerX, markerY, markerZ);
				forceUpdate = false;
			}
			return res;
		}
		
		void resort(Vec3d camPos) {
			index2indexMap = null;
			// 更新摄像机距离
			for (int i = 0; i < subChunkQuads; ++i) {
				int quadIndex = quadIndexes[i];
				var quad = quads[quadIndex];
				double distanceSquared;
				var center = quad.getCenterPos(cacheVec);
				distanceSquared =
					MathHelper.square(camPos.x - center.x) +
						MathHelper.square(camPos.y - center.y) +
						MathHelper.square(camPos.z - center.z);
				distanceCache[quadIndex] = distanceSquared;
			}
			// 排序
			IntArrays.unstableSort(quadIndexes, 0, subChunkQuads, subChunkSortComparator);
			// 更新byteBuffer数据
			byteBufferCache.clear();
			for (int i = 0; i < subChunkQuads; ++i) {
				int quadStartIndex = quadIndexes[i] * vertexPerQuad;
				for (int j = 0; j < indexPerQuad; ++j)
					byteBufferCache.putInt(quadStartIndex + quadBaseIndex[j]);
			}
			byteBufferCache.flip();
		}
		
		void render(RenderPass renderPass) {
			renderPass.setIndexBuffer(indexBuffer, indexType);
			renderPass.drawIndexed(0, 0, subChunkQuads * indexPerQuad, 1);
		}
		
		@Override public void close() {
			if (indexBuffer != null) {
				indexBuffer.close();
				indexBuffer = null;
			}
			if (byteBufferCache != null) {
				MemoryUtil.memFree(byteBufferCache);
				byteBufferCache = null;
			}
		}
	}
}
