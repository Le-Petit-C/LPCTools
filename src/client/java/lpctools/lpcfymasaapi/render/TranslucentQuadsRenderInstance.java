package lpctools.lpcfymasaapi.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.doubles.DoubleComparators;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Supplier;

// TODO trim清理
class TranslucentQuadsRenderInstance implements QuietAutoCloseable, Registries.WorldPreWeatherRender, Registries.WorldLastRender {
	static final TranslucentQuadsRenderInstance instance = new TranslucentQuadsRenderInstance();
	// 进行重排的最小tan值，值越小重排越频繁，也就越卡，但是对应地出现深度错误的情况越少
	private static final double resortTan = 0.25;
	private static final double resortTanSquare = resortTan * resortTan;
	
	private static final String baseLabel = "LPCTools TranslucentQuadsRenderInstance";
	private static final RenderPipeline pipeline = RenderPipelines.DEBUG_QUADS;
	private static final Supplier<String> indexBufferLabel = () -> appendLabel("IndexBuffer");
	private static final Supplier<String> vertexBufferLabel = () -> appendLabel("VertexBuffer");
	private static final Supplier<String> renderPassLabel = () -> appendLabel("RenderPass");
	
	private static String appendLabel(String tail) { return baseLabel + ' ' + tail; }
	
	// 变换基点，所有vertex以此为基点进行变换
	private final Vector3d basePoint = new Vector3d();
	private final ArrayList<SubChunk> subChunks = new ArrayList<>();
	private final Long2IntOpenHashMap pos2IndexMap = new Long2IntOpenHashMap();
	private final IntHeapPriorityQueue emptySubChunks = new IntHeapPriorityQueue();
	private final ArrayList<SubChunk> sortedRenderSubChunks = new ArrayList<>();
	private final HashSet<SubChunk> subChunksNeedUpload = new HashSet<>();
	private final CachedSupplier<ArrayList<ArrayList<SubChunk>>> subChunkSortingCache = new CachedSupplier<>(ArrayList::new);
	
	private static long packQuadId(int quadIndex, int subChunkIndex) { return quadIndex | ((long)subChunkIndex << 32); }
	private static int unpackQuadIndex(long quadId) { return (int)quadId; }
	private static int unpackSubChunkIndex(long quadId) { return (int)(quadId >>> 32); }
	
	// 比ChunkSection大一圈的Section，叫做Greater Section没什么不妥吧？
	private static final int greaterExponent = 1;
	private static final int greaterSectionSideLength = ChunkSectionPos.getBlockCoord(1) << greaterExponent;
	private static long asLongGreater(int x, int y, int z){ return ChunkSectionPos.asLong(x, y, z); }
	private static int getGreaterSectionCoord(double k){return ChunkSectionPos.getSectionCoord(k) >> greaterExponent;}
	private static int getBlockCoordGreater(int greaterSectionCoord){return ChunkSectionPos.getBlockCoord(greaterSectionCoord) << greaterExponent;}
	private static long getPackedGreaterSectionPos(double x, double y, double z)
	{ return asLongGreater(getGreaterSectionCoord(x), getGreaterSectionCoord(y), getGreaterSectionCoord(z)); }
	private static int unpackGreaterSectionX(long packed){ return ChunkSectionPos.unpackX(packed); }
	private static int unpackGreaterSectionY(long packed){ return ChunkSectionPos.unpackY(packed); }
	private static int unpackGreaterSectionZ(long packed){ return ChunkSectionPos.unpackZ(packed); }
	private static int getBlockCoordXGreater(long packed){ return getBlockCoordGreater(unpackGreaterSectionX(packed)); }
	private static int getBlockCoordYGreater(long packed){ return getBlockCoordGreater(unpackGreaterSectionY(packed)); }
	private static int getBlockCoordZGreater(long packed){ return getBlockCoordGreater(unpackGreaterSectionZ(packed)); }
	private static long toPackedGreaterSectionPos(Vector3d pos){ return getPackedGreaterSectionPos(pos.x, pos.y, pos.z); }
	
	private CompletableFuture<Void> prepareTasks;
	
	TranslucentQuadsRenderInstance() {
		Registries.RENDER_WORLD_PRE_WEATHER.register(this);
		Registries.WORLD_RENDER_LAST.register(this);
	}
	
	public long addQuad(Quad quad) {
		waitForTasks();
		var center = quad.getCenterPos(new Vector3d());
		long packedGreaterSectionPos = toPackedGreaterSectionPos(center);
		int index;
		if(pos2IndexMap.containsKey(packedGreaterSectionPos))
			index = pos2IndexMap.get(packedGreaterSectionPos);
		else {
			if (emptySubChunks.isEmpty()) {
				index = subChunks.size();
				subChunks.add(new SubChunk());
			}
			else index = emptySubChunks.dequeueInt();
			pos2IndexMap.put(packedGreaterSectionPos, index);
			subChunks.get(index).setSectionPos(packedGreaterSectionPos);
		}
		return packQuadId(subChunks.get(index).addQuad(quad), index);
	}
	
	public void removeQuad(long id) {
		waitForTasks();
		var subChunk = subChunks.get(unpackSubChunkIndex(id));
		subChunk.removeQuad(unpackQuadIndex(id));
		if(subChunk.isEmpty()) {
			pos2IndexMap.remove(subChunk.packedGreaterSectionPos);
			emptySubChunks.enqueue(unpackSubChunkIndex(id));
		}
	}
	
	@Override public void onLast(Registries.WorldRenderContext context) {
		waitForTasks();
		var commandEncoder = RenderSystem.getDevice().createCommandEncoder();
		if(!subChunksNeedUpload.isEmpty()) {
			for (var subChunk : subChunksNeedUpload) subChunk.upload(commandEncoder);
			subChunksNeedUpload.clear();
		}
		var fb = context.fb();
		GpuTextureView colorAttachmentView = fb.getColorAttachmentView();
		//GpuTextureView depthAttachmentView = fb.useDepthAttachment ? fb.getDepthAttachmentView() : null;
		var camPos = context.camera().getCameraPos();
		GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
			.write(RenderSystem.getModelViewMatrix().translate(new Vector3f((float) (basePoint.x - camPos.x), (float) (basePoint.y - camPos.y), (float) (basePoint.z - camPos.z)), new Matrix4f()),
				new Vector4f(1.0F, 1.0F, 1.0F, 1.0F), new Vector3f(), new Matrix4f(), 0.0F);
		GpuBufferSlice projection = RenderSystem.getProjectionMatrixBuffer();
		try (RenderPass renderPass = commandEncoder
			.createRenderPass(renderPassLabel, colorAttachmentView, OptionalInt.empty(), null, OptionalDouble.empty())) {
			renderPass.setPipeline(pipeline);
			renderPass.setUniform("DynamicTransforms", dynamicTransforms);
			renderPass.setUniform("Projection", projection);
			for (var subChunk : sortedRenderSubChunks)
				subChunk.render(renderPass);
		}
	}
	
	@Override public void onRenderWorldPreWeather(Registries.WorldRenderContext context) {
		prepareRenderDataAsync(context, Util.getMainWorkerExecutor(), true);
	}
	
	public void prepareRenderDataAsync(Registries.WorldRenderContext context, Executor executor, boolean recordData) {
		waitForTasks();
		final Frustum frustum = recordData ? new Frustum(context.frustum()) : context.frustum();
		prepareTasks = CompletableFuture.supplyAsync(() -> dispatchPrepareTasks(frustum, context.camera().getCameraPos(), executor), executor)
			.thenCompose(tasks -> tasks);
	}
	
	@Override public void close() {
		waitForTasks();
		for (SubChunk subChunk : subChunks) subChunk.close();
		subChunks.clear();
		pos2IndexMap.clear();
		emptySubChunks.clear();
		sortedRenderSubChunks.clear();
		subChunksNeedUpload.clear();
		subChunkSortingCache.close();
		
		Registries.RENDER_WORLD_PRE_WEATHER.unregister(this);
		Registries.WORLD_RENDER_LAST.unregister(this);
	}
	
	private void waitForTasks() {
		if (prepareTasks == null) return;
		prepareTasks.join();
		prepareTasks = null;
	}
	
	private CompletableFuture<Void> dispatchPrepareTasks(Frustum frustum, Vec3d camPos, Executor executor) {
		ArrayList<CompletableFuture<Void>> tasks = new ArrayList<>();
		double dstMax = Math.max(Math.max(Math.abs(camPos.x - basePoint.x), Math.abs(camPos.z - basePoint.z)), Math.abs(camPos.y - basePoint.y));
		if(dstMax > 1024) basePoint.set(camPos.x, camPos.y, camPos.z);
		
		// 计数排序
		int camSectionX = getGreaterSectionCoord(camPos.x);
		int camSectionY = getGreaterSectionCoord(camPos.y);
		int camSectionZ = getGreaterSectionCoord(camPos.z);
		var cacheList = subChunkSortingCache.get();
		for(var list : cacheList) list.clear();
		pos2IndexMap.long2IntEntrySet().fastForEach(entry->{
			var subChunk = subChunks.get(entry.getIntValue());
			var greaterSectionPos = subChunk.packedGreaterSectionPos;
			int manhattanDistance
				= Math.abs(camSectionX - unpackGreaterSectionX(greaterSectionPos))
				+ Math.abs(camSectionY - unpackGreaterSectionY(greaterSectionPos))
				+ Math.abs(camSectionZ - unpackGreaterSectionZ(greaterSectionPos));
			while(cacheList.size() <= manhattanDistance) cacheList.add(new ArrayList<>());
			cacheList.get(manhattanDistance).add(subChunk);
		});
		
		sortedRenderSubChunks.clear();
		for(int i = cacheList.size() - 1; i >= 0; --i)
			for(var subChunk : cacheList.get(i))
				subChunk.updateIfVisible(tasks, this, frustum, camPos, executor);
		
		return CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0]));
	}
	
	private static class SubChunk implements QuietAutoCloseable {
		static final int vertexPerQuad = 4;
		static final int sizePerQuad = vertexPerQuad * pipeline.getVertexFormat().getVertexSize();
		static final byte[] paddingBytes = new byte[sizePerQuad];
		static final int[] baseIndex = {0, 1, 2, 2, 1, 3};
		static final int indexPerQuad = baseIndex.length;
		
		final Vector3d markerPos = new Vector3d();
		final Vector3d cacheVec = new Vector3d();
		final IntHeapPriorityQueue unusedQuadIndex = new IntHeapPriorityQueue();
		final IntComparator sortComparator = (i1, i2) -> DoubleComparators.OPPOSITE_COMPARATOR.compare(this.distanceCache[i1], this.distanceCache[i2]);
		final Vector3d basePoint = new Vector3d();
		
		long packedGreaterSectionPos;
		Box sectionBox;
		GpuBuffer vertexBuffer = null;
		GpuBuffer indexBuffer = null;
		VertexFormat.IndexType indexType = null;
		ByteBuffer vertexBufferToUpload = null;
		ByteBuffer indexBufferToUpload = null;
		
		Quad[] quads = new Quad[0];
		double[] distanceCache = new double[0];
		int[] indexes = new int[0];
		int[] inversedIndexes = new int[0];
		int size = 0;
		int uploadedSize = 0;
		boolean vertexesChanged = false;
		int capacity(){ return quads.length; }
		boolean isEmpty(){ return size == 0; }
		boolean veryInitialized = false;
		
		SubChunk() {}
		
		void markVertexesChanged(){
			vertexesChanged = true;
			vertexBufferToUpload = closeIfExist(vertexBufferToUpload, MemoryUtil::memFree);
			indexBufferToUpload = closeIfExist(indexBufferToUpload, MemoryUtil::memFree);
		}
		
		void setSectionPos(long packedGreaterSectionPos) {
			this.packedGreaterSectionPos = packedGreaterSectionPos;
			sectionBox = new Box(
				getBlockCoordXGreater(packedGreaterSectionPos),
				getBlockCoordYGreater(packedGreaterSectionPos),
				getBlockCoordZGreater(packedGreaterSectionPos),
				getBlockCoordXGreater(packedGreaterSectionPos) + greaterSectionSideLength,
				getBlockCoordYGreater(packedGreaterSectionPos) + greaterSectionSideLength,
				getBlockCoordZGreater(packedGreaterSectionPos) + greaterSectionSideLength);
		}
		
		int addQuad(Quad quad) {
			int index; // quad位置的索引，不是索引的索引，addQuad情况下索引的索引是size
			if(unusedQuadIndex.isEmpty()){
				if (size >= capacity()) {
					quads = Arrays.copyOf(quads, Math.max(quads.length * 2, 16));
					indexes = Arrays.copyOf(indexes, capacity());
					inversedIndexes = Arrays.copyOf(inversedIndexes, capacity());
					distanceCache = new double[capacity()];
					if(vertexBuffer != null) vertexBuffer.close();
					if(indexBuffer != null) indexBuffer.close();
					vertexBuffer = RenderSystem.getDevice().createBuffer(vertexBufferLabel, GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST, capacity() * sizePerQuad);
					int indexCount = capacity() * indexPerQuad;
					indexType = indexCount <= 65536 ? VertexFormat.IndexType.SHORT : VertexFormat.IndexType.INT;
					indexBuffer = RenderSystem.getDevice().createBuffer(indexBufferLabel, GpuBuffer.USAGE_INDEX | GpuBuffer.USAGE_COPY_DST, indexCount * indexType.size);
					veryInitialized = false;
				}
				index = size;
			}
			else index = unusedQuadIndex.dequeueInt();
			quads[index] = quad;
			indexes[size] = index;
			inversedIndexes[index] = size++;
			markVertexesChanged();
			return index;
		}
		
		void removeQuad(int index){
			if(index < 0 || index >= capacity() || quads[index] == null) throw new IllegalArgumentException("Index not exist!");
			int i = inversedIndexes[index];
			quads[index] = null;
			if(i != --size){
				inversedIndexes[indexes[i] = indexes[size]] = i;
				markVertexesChanged();
			}
			unusedQuadIndex.enqueue(index);
		}
		
		void updateIfVisible(ArrayList<CompletableFuture<Void>> taskOutput, TranslucentQuadsRenderInstance caller, Frustum frustum, Vec3d camPos, Executor executor) {
			if(size == 0 || !frustum.isVisible(sectionBox)) return;
			caller.sortedRenderSubChunks.add(this);
			if(!basePoint.equals(caller.basePoint)){
				basePoint.set(caller.basePoint);
				markVertexesChanged();
			}
			boolean markerChanged = updateMarkerPos(camPos);
			if(vertexesChanged) taskOutput.add(CompletableFuture.runAsync(this::buildVertexByteBuffer, executor));
			if(vertexesChanged || markerChanged){
				caller.subChunksNeedUpload.add(this);
				taskOutput.add(CompletableFuture.runAsync(() -> resort(camPos), executor));
			}
		}
		
		boolean updateMarkerPos(Vec3d camPos){
			double mx = Math.clamp(camPos.x, sectionBox.minX, sectionBox.maxX);
			double my = Math.clamp(camPos.y, sectionBox.minY, sectionBox.maxY);
			double mz = Math.clamp(camPos.z, sectionBox.minZ, sectionBox.maxZ);
			boolean res;
			if(markerPos.x == mx && markerPos.y == my && markerPos.z == mz) res = false;
			else {
				double asq = 0, hsq = 0;
				if(markerPos.x == mx) asq += MathHelper.square(camPos.x - markerPos.x);
				else hsq += MathHelper.square(camPos.x - markerPos.x);
				if(markerPos.y == my) asq += MathHelper.square(camPos.y - markerPos.y);
				else hsq += MathHelper.square(camPos.y - markerPos.y);
				if(markerPos.z == mz) asq += MathHelper.square(camPos.z - markerPos.z);
				else hsq += MathHelper.square(camPos.z - markerPos.z);
				res = hsq  > resortTanSquare * asq;
			}
			if(res) markerPos.set(mx, my, mz);
			return res;
		}
		
		void buildVertexByteBuffer(){
			if(vertexBufferToUpload != null) MemoryUtil.memFree(vertexBufferToUpload);
			vertexBufferToUpload = MemoryUtil.memAlloc(quads.length * sizePerQuad);
			Vector3d vecCache = new Vector3d();
			for (Quad quad : quads) {
				if(quad == null) vertexBufferToUpload.put(paddingBytes);
				else appendQuadToVertexByteBuffer(quad, vecCache);
			}
			vertexBufferToUpload.flip();
		}
		
		private void appendQuadToVertexByteBuffer(Quad quad, Vector3d vecCache) {
			appendPositionToVertexByteBuffer(quad.base);
			vertexBufferToUpload.putInt(quad.color);
			appendPositionToVertexByteBuffer(quad.base.add(quad.u, vecCache));
			vertexBufferToUpload.putInt(quad.color);
			appendPositionToVertexByteBuffer(quad.base.add(quad.v, vecCache));
			vertexBufferToUpload.putInt(quad.color);
			appendPositionToVertexByteBuffer(quad.base.add(quad.u, vecCache).add(quad.v));
			vertexBufferToUpload.putInt(quad.color);
		}
		
		private void appendPositionToVertexByteBuffer(Vector3d pos) {
			vertexBufferToUpload.putFloat((float) (pos.x - basePoint.x)).putFloat((float) (pos.y - basePoint.y)).putFloat((float) (pos.z - basePoint.z));
		}
		
		void resort(Vec3d camPos) {
			// 更新摄像机距离
			for (int i = 0; i < size; ++i) {
				int index = indexes[i];
				var quad = quads[index];
				double distanceSquared;
				var center = quad.getCenterPos(cacheVec);
				distanceSquared =
					MathHelper.square(camPos.x - center.x) +
						MathHelper.square(camPos.y - center.y) +
						MathHelper.square(camPos.z - center.z);
				distanceCache[index] = distanceSquared;
			}
			// 排序
			IntArrays.unstableSort(indexes, 0, size, sortComparator);
			// 更新缓冲
			buildIndexByteBuffer();
			// 更新映射
			for(int i = 0; i < size; ++i) inversedIndexes[indexes[i]] = i;
		}
		
		void buildIndexByteBuffer(){
			if(indexBufferToUpload != null) MemoryUtil.memFree(indexBufferToUpload);
			indexBufferToUpload = MemoryUtil.memAlloc(size * indexPerQuad * indexType.size);
			if(indexType == VertexFormat.IndexType.INT) {
				for (int i = 0; i < size; ++i) {
					int startIndex = indexes[i] * vertexPerQuad;
					for (int j : baseIndex) indexBufferToUpload.putInt(startIndex + j);
				}
			}
			else {
				for (int i = 0; i < size; ++i) {
					int startIndex = indexes[i] * vertexPerQuad;
					for (int j : baseIndex) indexBufferToUpload.putShort((short) (startIndex + j));
				}
			}
			indexBufferToUpload.flip();
		}
		
		void upload(CommandEncoder encoder){
			if(indexBufferToUpload == null) return;
			if(size * indexPerQuad * indexType.size != indexBufferToUpload.limit()) return;
			encoder.writeToBuffer(indexBuffer.slice(0, indexBufferToUpload.limit()), indexBufferToUpload);
			MemoryUtil.memFree(indexBufferToUpload);
			indexBufferToUpload = null;
			if(vertexBufferToUpload != null) {
				encoder.writeToBuffer(vertexBuffer.slice(), vertexBufferToUpload);
				MemoryUtil.memFree(vertexBufferToUpload);
				vertexBufferToUpload = null;
			}
			vertexesChanged = false;
			veryInitialized = true;
			uploadedSize = size;
		}
		
		void render(RenderPass renderPass) {
			if(!veryInitialized) return;
			renderPass.setVertexBuffer(0, vertexBuffer);
			renderPass.setIndexBuffer(indexBuffer, indexType);
			renderPass.drawIndexed(0, 0, uploadedSize * indexPerQuad, 1);
		}
		
		@Contract("_,_->null")
		static <T> T closeIfExist(@Nullable T val, Consumer<T> close){
			if(val != null) close.accept(val);
			return null;
		}
		
		@Override public void close() {
			vertexBuffer = closeIfExist(vertexBuffer, GpuBuffer::close);
			indexBuffer = closeIfExist(indexBuffer, GpuBuffer::close);
			vertexBufferToUpload = closeIfExist(vertexBufferToUpload, MemoryUtil::memFree);
			indexBufferToUpload = closeIfExist(indexBufferToUpload, MemoryUtil::memFree);
		}
	}
}
