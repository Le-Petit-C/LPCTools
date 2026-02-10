package lpctools.debugs;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import lpctools.lpcfymasaapi.Registries;
import lpctools.lpcfymasaapi.configButtons.derivedConfigs.ArrayOptionListConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.BooleanHotkeyThirdListConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.UniqueIntegerConfig;
import net.minecraft.client.gl.RenderPipelines;
import org.jetbrains.annotations.Contract;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Random;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;

public class GpuCacheMissTest {
	public static final BooleanHotkeyThirdListConfig gpuCacheMissTest
		= new BooleanHotkeyThirdListConfig(DebugConfigs.debugs, "gpuCacheMissTest", GpuCacheMissTest::reallocate);
	static {listStack.push(gpuCacheMissTest);}
	public static final UniqueIntegerConfig shapeCount = addConfig(new UniqueIntegerConfig(
		peekConfigList(), "shapeCount", 512, 0, Integer.MAX_VALUE, GpuCacheMissTest::reallocate));
	public static final ArrayOptionListConfig<VertexShuffleMethod> vertexShuffleMethod
		= addArrayOptionListConfig("vertexShuffleMethod", GpuCacheMissTest::reallocate);
	static {
		for(var method : VertexShuffleMethod.values())
			vertexShuffleMethod.addOption(
				vertexShuffleMethod.getFullTranslationKey() + '.' + method.name(),
				method.name(), method);
	}
	public static final ArrayOptionListConfig<IndexShuffleMethod> indexShuffleMethod
		= addArrayOptionListConfig("indexShuffleMethod", GpuCacheMissTest::reallocate);
	static {
		for(var method : IndexShuffleMethod.values())
			indexShuffleMethod.addOption(
				indexShuffleMethod.getFullTranslationKey() + '.' + method.name(),
				method.name(), method);
	}
	static {listStack.pop();}
	public enum VertexShuffleMethod {
		NONE(Integer.MAX_VALUE),
		PER_SHAPE(12),
		TWO_PARTS(6),
		THREE_PARTS(4),
		FOUR_PARTS(3),
		SIX_PARTS(2),
		FULL_RANDOM(1);
		final int unitLength;
		VertexShuffleMethod(int unitLength){
			this.unitLength = unitLength;
		}
	}
	public enum IndexShuffleMethod {
		NONE(Integer.MAX_VALUE),
		PER_SHAPE(60),
		TWO_PARTS(30),
		FOUR_PARTS(15),
		FIVE_PARTS(12),
		TEN_PARTS(6),
		FULL_RANDOM(3);
		final int unitLength;
		IndexShuffleMethod(int unitLength){
			this.unitLength = unitLength;
		}
	}
	
	private static RenderInstance renderInstance;
	private static void reallocate(){
		if(renderInstance != null) renderInstance.close();
		if(gpuCacheMissTest.getBooleanValue()) renderInstance = new RenderInstance();
		else renderInstance = null;
	}
	@Contract(value = "_,_->param1")
	private static Quaternionf randomIdentityQuaternion(Quaternionf res, Random random){
		do{
			res.set(
				(float)random.nextGaussian(),
				(float)random.nextGaussian(),
				(float)random.nextGaussian(),
				(float)random.nextGaussian()
			);
		} while(res.lengthSquared() < 1e-9);
		return res.normalize();
	}
	private static void shuffleIntsUnited(int[] values, int unit, Random random){
		if(values.length % unit != 0) return;
		int unitCount = values.length / unit;
		for(int i = 0; i < unitCount; ++i){
			int swapTarget = random.nextInt(unitCount - i) + i;
			if(i == swapTarget) continue;
			int here = i * unit, there = swapTarget * unit;
			for(int j = 0; j < unit; ++j){
				int tmp = values[here + j];
				values[here + j] = values[there + j];
				values[there + j] = tmp;
			}
		}
	}
	private static final float phi = ((float)Math.sqrt(5) + 1) / 2;
	private static final Vector3f[] shapeBaseVectors = {
		new Vector3f( 0  , 1  , phi), // 0
		new Vector3f( 0  ,-1  , phi), // 1
		new Vector3f( 0  , 1  ,-phi), // 2
		new Vector3f( 0  ,-1  ,-phi), // 3
		new Vector3f( 1  , phi, 0  ), // 4
		new Vector3f(-1  , phi, 0  ), // 5
		new Vector3f( 1  ,-phi, 0  ), // 6
		new Vector3f(-1  ,-phi, 0  ), // 7
		new Vector3f( phi, 0  , 1  ), // 8
		new Vector3f( phi, 0  ,-1  ), // 9
		new Vector3f(-phi, 0  , 1  ), // 10
		new Vector3f(-phi, 0  ,-1  ), // 11
	};
	private static final int vertexPerShape = shapeBaseVectors.length;
	// 未处理绕序
	private static final int[] shapeBaseIndexes = {
		0 , 1 , 8 , 0 , 1 , 10,
		2 , 3 , 9 , 2 , 3 , 11,
		4 , 5 , 0 , 4 , 5 , 2 ,
		6 , 7 , 1 , 6 , 7 , 3 ,
		8 , 9 , 4 , 8 , 9 , 6 ,
		10, 11, 5 , 10, 11, 7 ,
		0 , 4 , 8 , 0 , 5 , 10,
		1 , 6 , 8 , 1 , 7 , 10,
		2 , 4 , 9 , 2 , 5 , 11,
		3 , 6 , 9 , 3 , 7 , 11
	};
	private static final int indexPerShape = shapeBaseIndexes.length;
	private static class RenderInstance implements AutoCloseable, Registries.WorldLastRender {
		static final RenderPipeline renderPipeline = RenderPipelines.DEBUG_QUADS;
		final GpuBuffer vertexBuffer, indexBuffer;
		final VertexFormat.IndexType indexType;
		final int shapeCount;
		RenderInstance(){
			Random random = new Random();
			shapeCount = GpuCacheMissTest.shapeCount.getIntegerValue();
			int vertexCount = shapeCount * vertexPerShape;
			var basicVertexIndexes = new int[vertexCount];
			Arrays.setAll(basicVertexIndexes, i -> i);
			int vertexUnitLength = vertexShuffleMethod.getCurrentUserdata().unitLength;
			shuffleIntsUnited(basicVertexIndexes, vertexUnitLength, random);
			float separateK = (float)Math.pow(shapeCount, 1.0 / 3);
			Vector3f[] shapeCache = new Vector3f[vertexPerShape];
			Arrays.setAll(shapeCache, indexType -> new Vector3f());
			var rotationCache = new Quaternionf();
			var centerCache = new Vector3f();
			Vector3f[] vertexCache = new Vector3f[vertexCount];
			for(int i = 0; i < shapeCount; i++){
				int shapeStart = i * vertexPerShape;
				Vector3f center = centerCache.set((float)random.nextGaussian(), (float)random.nextGaussian(), (float)random.nextGaussian()).mul(separateK);
				var rotation = randomIdentityQuaternion(rotationCache, random);
				for(int j = 0; j < vertexPerShape; ++j) shapeBaseVectors[j].rotate(rotation, shapeCache[j]).add(center);
				for(int j = 0; j < vertexPerShape; ++j) vertexCache[basicVertexIndexes[shapeStart + j]] = new Vector3f(shapeCache[j]);
			}
			var vertexByteBuffer = MemoryUtil.memAlloc(renderPipeline.getVertexFormat().getVertexSize() * vertexCount);
			for(int i = 0; i < vertexCount; i++){
				var p = vertexCache[i];
				vertexByteBuffer.putFloat(p.x).putFloat(p.y).putFloat(p.z);
				vertexByteBuffer.putInt(random.nextInt() | 0xff000000);
			}
			vertexByteBuffer.flip();
			vertexBuffer = RenderSystem.getDevice().createBuffer(
				()->"GpuCacheMissTest VertexBuffer", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST,
				vertexByteBuffer);
			MemoryUtil.memFree(vertexByteBuffer);
			int indexCount = shapeCount * indexPerShape;
			var indexes = new int[indexCount];
			for(int i = 0; i < shapeCount; ++i){
				int vertexStart = i * vertexPerShape;
				int indexStart = i * indexPerShape;
				for(int j = 0; j < indexPerShape; ++j)
					indexes[indexStart + j] = basicVertexIndexes[vertexStart + shapeBaseIndexes[j]];
			}
			shuffleIntsUnited(indexes, indexShuffleMethod.get().unitLength, random);
			ByteBuffer indexByteBuffer;
			if(indexCount <= 65536) {
				indexType = VertexFormat.IndexType.SHORT;
				indexByteBuffer = MemoryUtil.memAlloc(indexType.size * indexCount);
				for(int i = 0; i < indexCount; ++i) indexByteBuffer.putShort((short)indexes[i]);
			}
			else {
				indexType = VertexFormat.IndexType.INT;
				indexByteBuffer = MemoryUtil.memAlloc(indexType.size * indexCount);
				for(int i = 0; i < indexCount; ++i) indexByteBuffer.putInt(indexes[i]);
			}
			indexByteBuffer.flip();
			indexBuffer = RenderSystem.getDevice().createBuffer(
				()->"GpuCacheMissTest IndexBuffer", GpuBuffer.USAGE_INDEX | GpuBuffer.USAGE_COPY_DST,
				indexByteBuffer);
			MemoryUtil.memFree(indexByteBuffer);
			Registries.WORLD_RENDER_LAST.register(this);
		}
		
		@Override public void close() {
			vertexBuffer.close();
			indexBuffer.close();
			Registries.WORLD_RENDER_LAST.unregister(this);
		}
		
		@Override public void onLast(Registries.WorldRenderContext context) {
			var fb = context.fb();
			GpuTextureView colorAttachmentView = fb.getColorAttachmentView();
			GpuTextureView depthAttachmentView = fb.useDepthAttachment ? fb.getDepthAttachmentView() : null;
			GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
				.write(RenderSystem.getModelViewMatrix().translate(context.camera().getCameraPos().toVector3f().mul(-1),
					new Matrix4f()), new Vector4f(1.0F, 1.0F, 1.0F, 1.0F), new Vector3f(), new Matrix4f(), 0.0F);
			GpuBufferSlice projection = RenderSystem.getProjectionMatrixBuffer();
			try(RenderPass renderPass = RenderSystem.getDevice()
				.createCommandEncoder()
				.createRenderPass(() -> "LPCTools Vertex Cache Miss Test",
					colorAttachmentView, OptionalInt.empty(), depthAttachmentView, OptionalDouble.empty())){
				renderPass.setPipeline(renderPipeline);
				renderPass.setUniform("DynamicTransforms", dynamicTransforms);
				renderPass.setUniform("Projection", projection);
				renderPass.setVertexBuffer(0, vertexBuffer);
				renderPass.setIndexBuffer(indexBuffer, indexType);
				renderPass.drawIndexed(0, 0, shapeCount * indexPerShape, 1);
			}
		}
	}
}
