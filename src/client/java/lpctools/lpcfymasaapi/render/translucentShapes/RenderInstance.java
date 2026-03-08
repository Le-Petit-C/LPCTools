package lpctools.lpcfymasaapi.render.translucentShapes;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import fi.dy.masa.malilib.render.MaLiLibPipelines;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import lpctools.lpcfymasaapi.Registries;
import lpctools.lpcfymasaapi.render.IPositionVertex;
import lpctools.util.CachedSupplier;
import lpctools.util.javaex.QuietAutoCloseable;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.RawProjectionMatrix;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

// TODO:
//  trim清理
//  渲染优先级（或许可以在渲染选项中指定）
public class RenderInstance implements QuietAutoCloseable, Registries.WorldPreMainRender, IRenderCallback {
	private static final ConcurrentHashMap<RenderOption, RenderInstance> renderInstances = new ConcurrentHashMap<>();
	
	// 进行重排的最小tan值，值越小重排越频繁，也就越卡，但是对应地出现深度错误的情况越少
	private static final double resortTan = 0.25;
	private static final double resortTanSquare = resortTan * resortTan;
	
	private static final String baseLabel = "LPCTools TranslucentQuadsRenderInstance";
	private static final Supplier<String> indexBufferLabel = () -> appendLabel("IndexBuffer");
	private static final Supplier<String> vertexBufferLabel = () -> appendLabel("VertexBuffer");
	private static final Supplier<String> renderPassLabel = () -> appendLabel("RenderPass");
	// should only be modified in referred mixin
	public static final Matrix4f worldProjectionMatrixBiased = new Matrix4f();
	
	private static String appendLabel(String tail) { return baseLabel + ' ' + tail; }
	
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
	
	private final RenderOption renderOption;
	private final RawProjectionMatrix rawProjectionMatrix = new RawProjectionMatrix("LPCToolsRenderInstance");
	// 变换基点，所有vertex以此为基点进行变换
	private final Vector3d basePoint = new Vector3d();
	private final ArrayList<SubChunk> subChunks = new ArrayList<>();
	private final Long2IntOpenHashMap pos2IndexMap = new Long2IntOpenHashMap();
	private final IntHeapPriorityQueue emptySubChunks = new IntHeapPriorityQueue();
	private final ArrayList<SubChunk> sortedRenderSubChunks = new ArrayList<>();
	private final HashSet<SubChunk> subChunksNeedUpload = new HashSet<>();
	private final CachedSupplier<ArrayList<ArrayList<SubChunk>>> subChunkSortingCache = new CachedSupplier<>(ArrayList::new);
	
	private CompletableFuture<Void> prepareTasks;
	private CompletableFuture<CompletableFuture<Void>> dispatchTask;
	
	private Registries.MASAWorldRenderContext recordedWorldRenderContext;
	
	private int sizePerVertex(){ return renderOption.pipeline().getVertexFormat().getVertexSize(); }
	
	private RenderInstance(RenderOption renderOption) {
		this.renderOption = renderOption;
		Registries.PRE_MAIN.register(this);
		renderOption.timing().register(this, true);
	}
	
	public static RenderInstance getRenderInstance(RenderOption renderOption) {
		return renderInstances.computeIfAbsent(renderOption, RenderInstance::new);
	}
	
	public static RenderPipeline shapePipeline = MaLiLibPipelines.POSITION_COLOR_TRANSLUCENT;
	
	public static RenderPipeline linePipeline = MaLiLibPipelines.DEBUG_LINES_TRANSLUCENT;
	
	public static RenderOption shapeOptionWithDepth = new RenderOption(shapePipeline, true, true, RenderTiming.BEFORE_TRANSLUCENT);
	public static RenderOption shapeOptionDepthless = new RenderOption(shapePipeline, true, false, RenderTiming.END_MAIN);
	public static RenderOption lineOptionWithDepth = new RenderOption(linePipeline, true, true, RenderTiming.BEFORE_TRANSLUCENT);
	public static RenderOption lineOptionDepthless = new RenderOption(linePipeline, true, false, RenderTiming.END_MAIN);
	
	public static RenderInstance shapeInstanceWithDepth() { return getRenderInstance(shapeOptionWithDepth); }
	public static RenderInstance shapeInstanceDepthless() { return getRenderInstance(shapeOptionDepthless); }
	public static RenderInstance lineInstanceWithDepth() { return getRenderInstance(lineOptionWithDepth); }
	public static RenderInstance lineInstanceDepthless() { return getRenderInstance(lineOptionDepthless); }
	public static RenderInstance defaultRenderInstance(boolean isLine, boolean depthless) {
		if(isLine){
			if(depthless) return lineInstanceDepthless();
			else return lineInstanceWithDepth();
		}
		else {
			if(depthless) return shapeInstanceDepthless();
			else return shapeInstanceWithDepth();
		}
	}
	
	public ShapeReference addShape(Shape<? extends IPositionVertex> shape) {
		long packedGreaterSectionPos = toPackedGreaterSectionPos(shape.center);
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
		return subChunks.get(index).addShape(shape);
	}
	
	@Override public void render() {
		var context = recordedWorldRenderContext;
		waitForTasks();
		var commandEncoder = RenderSystem.getDevice().createCommandEncoder();
		if(!subChunksNeedUpload.isEmpty()) {
			for (var subChunk : subChunksNeedUpload) subChunk.upload(commandEncoder);
			subChunksNeedUpload.clear();
		}
		var fb = context.fb();
		GpuTextureView colorAttachmentView = renderOption.useColorBuffer() ? fb.getColorAttachmentView() : null;
		GpuTextureView depthAttachmentView = renderOption.useDepthBuffer() ? (fb.useDepthAttachment ? fb.getDepthAttachmentView() : null) : null;
		var camPos = context.camera().getCameraPos();
		var modelViewMatrix = RenderSystem.getModelViewMatrix().translate(new Vector3f((float) (basePoint.x - camPos.x), (float) (basePoint.y - camPos.y), (float) (basePoint.z - camPos.z)), new Matrix4f());
		GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
			.write(modelViewMatrix, new Vector4f(1.0F, 1.0F, 1.0F, 1.0F), new Vector3f(), new Matrix4f());
		// z-fighting解决方案
		// 或许可以把具有相似配置的RenderInstance一起绘制从而避免频繁的重设数据？
		GpuBufferSlice projection;
		if(depthAttachmentView != null) projection = rawProjectionMatrix.set(worldProjectionMatrixBiased);
		else projection = RenderSystem.getProjectionMatrixBuffer();
		try (RenderPass renderPass = commandEncoder
			.createRenderPass(renderPassLabel, colorAttachmentView, OptionalInt.empty(), depthAttachmentView, OptionalDouble.empty())) {
			renderPass.setPipeline(renderOption.pipeline());
			renderPass.setUniform("DynamicTransforms", dynamicTransforms);
			renderPass.setUniform("Projection", projection);
			for (var subChunk : sortedRenderSubChunks)
				subChunk.render(renderPass);
		}
	}
	
	@Override public void onRenderWorldPreMain(Registries.MASAWorldRenderContext context) {
		recordedWorldRenderContext = context;
		prepareRenderDataAsync(Util.getMainWorkerExecutor(), true);
	}
	
	public void prepareRenderDataAsync(Executor executor, boolean recordData) {
		var context = recordedWorldRenderContext;
		RenderSystem.assertOnRenderThread();
		waitForTasks();
		final Frustum frustum = recordData ? new Frustum(context.frustum()) : context.frustum();
		dispatchTask = CompletableFuture.supplyAsync(() -> dispatchPrepareTasks(frustum, context.camera().getCameraPos(), executor), executor);
		prepareTasks = dispatchTask.thenCompose(tasks -> tasks);
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
		rawProjectionMatrix.close();
		
		Registries.PRE_MAIN.unregister(this);
		renderOption.timing().unregister(this);
	}
	
	private void waitForTasks() {
		if (prepareTasks == null) return;
		prepareTasks.join();
		prepareTasks = null;
	}
	
	private void waitForDispatchTask() {
		if (dispatchTask == null) return;
		dispatchTask.join();
		dispatchTask = null;
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
	
	private class SubChunk implements QuietAutoCloseable {
		final Vector3d markerPos = new Vector3d();
		final Vector3d basePoint = new Vector3d();
		
		long packedGreaterSectionPos;
		Box sectionBox;
		GpuBuffer vertexBuffer = null;
		GpuBuffer indexBuffer = null;
		VertexFormat.IndexType indexType = null;
		ByteBuffer vertexBufferToUpload = null;
		ByteBuffer indexBufferToUpload = null;
		
		final ArrayList<ShapeInfo> shapes = new ArrayList<>();
		final ArrayList<ElementReference> elements = new ArrayList<>();
		int vertices_size = 0, elements_size = 0;
		
		int uploadedSize = 0;
		boolean verticesChanged = false;
		@SuppressWarnings("unused") boolean isEmpty(){ return shapes.isEmpty(); }
		boolean veryInitialized = false;
		
		CompletableFuture<Void> subChunkPrepareTask = null;
		
		void waitForSubChunkTask() {
			waitForDispatchTask();
			if (subChunkPrepareTask == null) return;
			subChunkPrepareTask.join();
			subChunkPrepareTask = null;
		}
		
		void removeElement(ElementReference element){
			var last = elements.removeLast();
			if(last != element) elements.set(last.index = element.index, last);
		}
		
		class ShapeInfo implements ShapeReference {
			final Shape<? extends IPositionVertex> shape;
			final ElementReference[] elementReferences;
			int vertexGpuIndex;
			int index;
			ShapeInfo(Shape<? extends IPositionVertex> shape, int index) {
				this.shape = shape;
				this.index = index;
				elementReferences = new ElementReference[shape.baseIndices.length];
				for(int i = 0; i < shape.baseIndices.length; ++i)
					elementReferences[i] = new ElementReference(this, i, shape.centers[i]);
			}
			
			@Override public void removeShape() {
				RenderSystem.assertOnRenderThread();
				if(index < 0) return;
				waitForSubChunkTask();
				var last = shapes.removeLast();
				if(last != this) shapes.set(last.index = index, last);
				index = -1;
				vertices_size -= shape.vertices.length;
				for(var elementRef : elementReferences){
					removeElement(elementRef);
					elements_size -= shape.baseIndices[elementRef.elementIndex].length;
				}
				markVerticesChanged();
			}
		}
		
		static class ElementReference {
			final ShapeInfo shapeInfo;
			final Vector3d pos;
			final int elementIndex;
			int index = 0;
			float distanceSquared = 0; // 排序时的距离缓存
			ElementReference(ShapeInfo shapeInfo, int elementIndex, Vector3d pos) {
				this.shapeInfo = shapeInfo;
				this.elementIndex = elementIndex;
				this.pos = pos;
			}
		}
		
		SubChunk() {}
		
		void markVerticesChanged(){
			verticesChanged = true;
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
		
		ShapeReference addShape(Shape<? extends IPositionVertex> shape) {
			RenderSystem.assertOnRenderThread();
			var res = new ShapeInfo(shape, shapes.size());
			waitForSubChunkTask();
			shapes.add(res);
			vertices_size += shape.vertices.length;
			for(var element : res.elementReferences) {
				element.index = elements.size();
				elements.add(element);
				elements_size += shape.baseIndices[element.elementIndex].length;
			}
			markVerticesChanged();
			return res;
		}
		
		void updateIfVisible(ArrayList<CompletableFuture<Void>> taskOutput, RenderInstance caller, Frustum frustum, Vec3d camPos, Executor executor) {
			if(shapes.isEmpty() || !frustum.isVisible(sectionBox)) return;
			caller.sortedRenderSubChunks.add(this);
			if(!basePoint.equals(caller.basePoint)){
				basePoint.set(caller.basePoint);
				markVerticesChanged();
			}
			boolean markerChanged = updateMarkerPos(camPos);
			CompletableFuture<Void> task = null;
			if(verticesChanged) task = CompletableFuture.runAsync(this::buildVertexByteBuffer, executor);
			if(verticesChanged || markerChanged){
				caller.subChunksNeedUpload.add(this);
				Runnable resortRunnable = ()->resort(camPos);
				if(task != null) task = task.thenRunAsync(resortRunnable, executor);
				else task = CompletableFuture.runAsync(resortRunnable, executor);
			}
			if(task != null) {
				subChunkPrepareTask = task;
				taskOutput.add(task);
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
			int vertexSize = sizePerVertex();
			vertexBufferToUpload = MemoryUtil.memAlloc(vertices_size * vertexSize);
			indexType = vertices_size > 65536 ? VertexFormat.IndexType.INT : VertexFormat.IndexType.SHORT;
			int gpuIndex = 0;
			int position = 0;
			for(var shape : shapes){
				shape.vertexGpuIndex = gpuIndex;
				gpuIndex += shape.shape.vertices.length;
				for(var vertex : shape.shape.vertices) {
					vertex.putBytesRelatively(vertexBufferToUpload, basePoint);
					if(vertexBufferToUpload.position() != (position += vertexSize))
						throw new RuntimeException("Invalid vertex, expected " + vertexSize + " bytes but put "
							+ (vertexBufferToUpload.position() - position + vertexSize) + " bytes");
				}
			}
			vertexBufferToUpload.flip();
		}
		
		void resort(Vec3d camPos) {
			// 更新摄像机距离
			for(var shape : shapes){
				for(int i = 0; i < shape.elementReferences.length; ++i){
					var elementRef = shape.elementReferences[i];
					var center = shape.shape.centers[i];
					elementRef.distanceSquared
						= MathHelper.square((float)(center.x - camPos.x))
						+ MathHelper.square((float)(center.y - camPos.y))
						+ MathHelper.square((float)(center.z - camPos.z));
					// 排序默认是从小到大排序，但是我们需要从远到近绘制，乘以-1以适配二者
					elementRef.distanceSquared *= -1;
				}
			}
			// 排序
			elements.sort(Comparator.comparingDouble(elementRef -> elementRef.distanceSquared));
			// 更新索引
			for(int i = 0; i < elements.size(); ++i) elements.get(i).index = i;
			// 更新缓冲
			buildIndexByteBuffer();
		}
		
		void buildIndexByteBuffer(){
			if(indexBufferToUpload != null) MemoryUtil.memFree(indexBufferToUpload);
			indexBufferToUpload = MemoryUtil.memAlloc(elements_size * indexType.size);
			IntConsumer indexConsumer = switch(indexType){
				case SHORT -> i->indexBufferToUpload.putShort((short)i);
				case INT -> i->indexBufferToUpload.putInt(i);
			};
			for(var element : elements) {
				int[] baseIndices = element.shapeInfo.shape.baseIndices[element.elementIndex];
				int vertexGpuIndex = element.shapeInfo.vertexGpuIndex;
				for(int i : baseIndices) indexConsumer.accept(i + vertexGpuIndex);
			}
			indexBufferToUpload.flip();
		}
		
		void upload(CommandEncoder encoder){
			if(indexBufferToUpload == null) return;
			
			int requiredIndexSize = indexBufferToUpload.limit();
			if(indexBuffer == null || indexBuffer.size() < requiredIndexSize){
				long oldSize = indexBuffer == null ? 0 : indexBuffer.size();
				if(indexBuffer != null) indexBuffer.close();
				indexBuffer = RenderSystem.getDevice().createBuffer(
					indexBufferLabel, GpuBuffer.USAGE_INDEX | GpuBuffer.USAGE_COPY_DST,
					Math.max(oldSize * 2, requiredIndexSize));
			}
			encoder.writeToBuffer(indexBuffer.slice(0, requiredIndexSize), indexBufferToUpload);
			MemoryUtil.memFree(indexBufferToUpload);
			indexBufferToUpload = null;
			
			if(vertexBufferToUpload != null) {
				int requiredVertexSize = vertexBufferToUpload.limit();
				if(vertexBuffer == null || vertexBuffer.size() < requiredVertexSize){
					long oldSize = vertexBuffer == null ? 0 : vertexBuffer.size();
					if(vertexBuffer != null) vertexBuffer.close();
					vertexBuffer = RenderSystem.getDevice().createBuffer(
						vertexBufferLabel, GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST,
						Math.max(oldSize * 2, requiredVertexSize));
				}
				encoder.writeToBuffer(vertexBuffer.slice(0, requiredVertexSize), vertexBufferToUpload);
				MemoryUtil.memFree(vertexBufferToUpload);
				vertexBufferToUpload = null;
			}
			verticesChanged = false;
			veryInitialized = true;
			uploadedSize = requiredIndexSize / indexType.size;
		}
		
		void render(RenderPass renderPass) {
			if(!veryInitialized) return;
			renderPass.setVertexBuffer(0, vertexBuffer);
			renderPass.setIndexBuffer(indexBuffer, indexType);
			renderPass.drawIndexed(0, 0, uploadedSize, 1);
		}
		
		@Contract("_,_->null")
		static <T> T closeIfExist(@Nullable T val, Consumer<T> close){
			if(val != null) close.accept(val);
			return null;
		}
		
		@Override public void close() {
			waitForSubChunkTask();
			vertexBuffer = closeIfExist(vertexBuffer, GpuBuffer::close);
			indexBuffer = closeIfExist(indexBuffer, GpuBuffer::close);
			vertexBufferToUpload = closeIfExist(vertexBufferToUpload, MemoryUtil::memFree);
			indexBufferToUpload = closeIfExist(indexBufferToUpload, MemoryUtil::memFree);
		}
	}
}
