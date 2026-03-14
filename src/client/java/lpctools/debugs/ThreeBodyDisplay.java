package lpctools.debugs;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import lpctools.LPCTools;
import lpctools.lpcfymasaapi.Registries;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.BooleanThirdListConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.UniqueDoubleConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.Vector3dConfig;
import lpctools.lpcfymasaapi.render.LPCRenderPipelines;
import lpctools.lpcfymasaapi.render.translucentShapes.RenderInstance;
import lpctools.lpcfymasaapi.render.translucentShapes.Sphere;
import lpctools.util.javaex.QuietAutoCloseable;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.render.RawProjectionMatrix;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.*;
import org.jspecify.annotations.NonNull;
import org.lwjgl.system.MemoryUtil;

import java.lang.Math;
import java.nio.ByteBuffer;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Random;
import java.util.function.Supplier;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;

public class ThreeBodyDisplay {
	public static BooleanThirdListConfig threeBody = new BooleanThirdListConfig(DebugConfigs.debugs,
		"threeBody", false, ThreeBodyDisplay::drawSphereCallback);
	static { listStack.push(threeBody); }
	public static final Vector3dConfig massCenter = addConfigEx(l->new Vector3dConfig(l, "massCenter", new Vec3d(0, 0, 0), null));
	public static final UniqueDoubleConfig distanceLimit = addConfigEx(l->new UniqueDoubleConfig(l, "distanceLimit", 64));
	static { listStack.pop(); }
	private static @Nullable Runner runner;
	private static void drawSphereCallback(){
		if(threeBody.getBooleanValue()) {
			if(runner == null)
				runner = new Runner();
		}
		else {
			if(runner != null){
				runner.close();
				runner = null;
			}
		}
	}
	private static class Runner implements QuietAutoCloseable, Registries.WorldPreMainRender, WorldRenderEvents.BeforeTranslucent {
		private static final String baseLabel = "LPCTools TranslucentQuadsRenderInstance";
		private static final Supplier<String> indexBufferLabel = () -> appendLabel("IndexBuffer");
		private static final Supplier<String> vertexBufferLabel = () -> appendLabel("VertexBuffer");
		private static final Supplier<String> renderPassLabel = () -> appendLabel("RenderPass");
		
		private static String appendLabel(String tail) { return baseLabel + ' ' + tail; }
		private static final int ticksPerLoop = 256;
		private static final double tickFactor = 1.0 / 256.0 / ticksPerLoop;
		private final Star[] stars = new Star[3];
		private final StarRenderData[] starsRenderData = new StarRenderData[3];
		private volatile boolean running = true;
		private final RawProjectionMatrix rawProjectionMatrixBuffer = new RawProjectionMatrix("LPCTools ThreeBodyDisplay");
		private final ByteBuffer dataBuffer;
		private final GpuBuffer indexBuffer;
		private final GpuBuffer vertexBuffer;
		
		Runner() {
			registerAll(true);
			synchronized (this) {
				new Thread(this::run, "ThreeBodySim").start();
				try {
					this.wait();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
			var indexDataBuffer = MemoryUtil.memAlloc(3 * 2 * Sphere.baseIndices.length * Sphere.baseIndices[0].length);
			for(int i = 0; i < 3; ++i) for(var a : Sphere.baseIndices) for(var b : a)
				indexDataBuffer.putShort((short) (i * 8 + b));
			indexDataBuffer.flip();
			indexBuffer = RenderSystem.getDevice().createBuffer(indexBufferLabel,
				GpuBuffer.USAGE_INDEX | GpuBuffer.USAGE_COPY_DST, indexDataBuffer);
			MemoryUtil.memFree(indexDataBuffer);
			dataBuffer = MemoryUtil.memAlloc(VertexFormats.POSITION_COLOR_LINE_WIDTH.getVertexSize() * 24);
			vertexBuffer = RenderSystem.getDevice().createBuffer(vertexBufferLabel,
				GpuBuffer.USAGE_INDEX | GpuBuffer.USAGE_COPY_DST, dataBuffer.capacity());
		}
		
		@Override public void close() {
			registerAll(false);
			running = false;
			rawProjectionMatrixBuffer.close();
			MemoryUtil.memFree(dataBuffer);
			indexBuffer.close();
			vertexBuffer.close();
		}
		
		
		@Override public void beforeTranslucent(@NonNull WorldRenderContext ignored) {
			var context = recordedContext;
			dataBuffer.clear();
			synchronized (starsRenderData) {
				for(var star : starsRenderData) {
					for(int i = 0; i < 8; ++i) {
						dataBuffer.putFloat((float)star.position.x).putFloat((float)star.position.y).putFloat((float)star.position.z)
							.putInt(star.color).putFloat(star.radius);
					}
				}
			}
			dataBuffer.flip();
			var commandEncoder = RenderSystem.getDevice().createCommandEncoder();
			commandEncoder.writeToBuffer(vertexBuffer.slice(), dataBuffer);
			var fb = context.fb();
			GpuTextureView colorAttachmentView = fb.getColorAttachmentView();
			GpuTextureView depthAttachmentView = fb.useDepthAttachment ? fb.getDepthAttachmentView() : null;
			var camPos = context.camera().getCameraPos();
			Vector3f offset = new Vector3f();
			Matrix4f modelViewMatrix = new Matrix4f(RenderSystem.getModelViewMatrix());
			Matrix4f projectionMatrix = new Matrix4f(RenderInstance.worldBasicProjectionMatrix);
			RenderInstance.worldProjectionTranslateMatrix.mul(modelViewMatrix, modelViewMatrix);
			modelViewMatrix.get3x3(new Matrix3f()).invert().transform(modelViewMatrix.getColumn(3, offset));
			offset.mul(-1);
			modelViewMatrix.translate(offset);
			offset.mul(-1);
			Vector3d basePoint = massCenter.getPos(new Vector3d());
			offset.add((float) (basePoint.x - camPos.x), (float) (basePoint.y - camPos.y), (float) (basePoint.z - camPos.z));
			GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
				.write(modelViewMatrix, new Vector4f(1.0F, 1.0F, 1.0F, 1.0F), offset, new Matrix4f());
			GpuBufferSlice projection = rawProjectionMatrixBuffer.set(projectionMatrix);
			try (RenderPass renderPass = commandEncoder
				.createRenderPass(renderPassLabel, colorAttachmentView, OptionalInt.empty(), depthAttachmentView, OptionalDouble.empty())) {
				renderPass.setPipeline(LPCRenderPipelines.spherePipeline);
				renderPass.setUniform("DynamicTransforms", dynamicTransforms);
				renderPass.setUniform("Projection", projection);
				renderPass.setIndexBuffer(indexBuffer, VertexFormat.IndexType.SHORT);
				renderPass.setVertexBuffer(0, vertexBuffer);
				renderPass.drawIndexed(0, 0, 3 * Sphere.baseIndices.length * Sphere.baseIndices[0].length, 1);
			}
		}
		
		private void run() {
			try {
				Star.CalcCache cache = new Star.CalcCache();
				double backSeconds = System.currentTimeMillis() * 0.001 - 1 / tickFactor;
				Random random = new Random();
				for(int i = 0; i < stars.length; ++i) stars[i] = new Star(random);
				for(int i = 0; i < starsRenderData.length; ++i) starsRenderData[i] = new StarRenderData(stars[i]);
				synchronized (this) { this.notify(); }
				while(running) {
					double reserved = System.currentTimeMillis() * 0.001 - backSeconds;
					double dt = reserved * tickFactor;
					backSeconds += dt * ticksPerLoop;
					if(Star.isOutOfRange(stars, cache))
						Star.randomizeStars(stars, random, cache);
					for(int i = 0; i < ticksPerLoop; ++i)
						Star.tick(stars, dt, cache);
					Star.normalize(stars, cache);
					synchronized (starsRenderData) {
						for (int i = 0; i < stars.length; ++i)
							starsRenderData[i].set(stars[i]);
					}
				}
			} catch (Exception e) {
				LPCTools.LOGGER.error("Error while rendering stars: ", e);
				throw e;
			}
		}
		
		Registries.MASAWorldRenderContext recordedContext;
		
		@Override public void onRenderWorldPreMain(Registries.MASAWorldRenderContext context) {
			recordedContext = context;
		}
		
		private static class StarRenderData {
			float radius;
			int color;
			final Vector3d position = new Vector3d();
			StarRenderData(Star star) { set(star); }
			void set(Star star) {
				radius = star.radius;
				color = star.color;
				position.set(star.position);
			}
		}
		
		private static class Star {
			double mass;
			float radius;
			int color;
			final Vector3d position = new Vector3d(), velocity = new Vector3d(), vChange = new Vector3d();
			Star(Random random) { randomize(random); }
			void randomize(Random random) {
				mass = 16;
				radius = 0.5f;
				color = 0xff7fafff;
				position.set(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).mul(8);
				velocity.set(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).div(4);
			}
			static void randomizeStars(Star[] stars, Random random, CalcCache cache) {
				for(Star star : stars) star.randomize(random);
				normalize(stars, cache);
			}
			static class CalcCache {
				final Vector3d tmp1 = new Vector3d();
				final Vector3d tmp2 = new Vector3d();
				final Vector3d tmp3 = new Vector3d();
			}
			static void tick(Star[] stars, double dt, CalcCache cache) {
				for(var star : stars) star.vChange.set(0, 0, 0);
				for(int i = 0; i < stars.length; ++i){
					for(int j = i + 1; j < stars.length; ++j){
						var star1 = stars[i];
						var star2 = stars[j];
						star2.position.sub(star1.position, cache.tmp1);
						double dstSquareInv = 1.0 / cache.tmp1.lengthSquared();
						cache.tmp1.mul(dstSquareInv * Math.sqrt(dstSquareInv) * dt);
						star1.vChange.add(cache.tmp1.mul(star2.mass, cache.tmp2));
						star2.vChange.sub(cache.tmp1.mul(star1.mass, cache.tmp2));
					}
				}
				for(var star : stars) {
					star.position.fma(dt, star.velocity).fma(0.5 * dt, star.vChange);
					star.velocity.add(star.vChange);
				}
			}
			static void normalize(Star[] stars, CalcCache cache) {
				double massSum = 0;
				cache.tmp1.set(0, 0, 0);
				for(var star : stars) {
					massSum += star.mass;
					star.position.mul(star.mass, cache.tmp2);
					cache.tmp1.add(cache.tmp2);
				}
				cache.tmp1.mul(-1.0 / massSum);
				for(var star : stars) star.position.add(cache.tmp1);
				cache.tmp1.set(0, 0, 0);
				for(var star : stars) {
					star.velocity.mul(star.mass, cache.tmp2);
					cache.tmp1.add(cache.tmp2);
				}
				cache.tmp1.mul(-1.0 / massSum);
				for(var star : stars) star.velocity.add(cache.tmp1);
			}
			// 通过比较动能和势能判断是否超出了限制
			static boolean isOutOfRange(Star[] stars, CalcCache cache) {
				for(var star : stars) {
					if(star.position.dot(star.velocity) <= 0) continue;
					if(star.position.length() <= distanceLimit.getDoubleValue()) continue;
					double othersMassSum = 0;
					cache.tmp1.set(0, 0, 0);
					cache.tmp2.set(0, 0, 0);
					for(var s : stars) {
						if(s == star) continue;
						othersMassSum += s.mass;
						cache.tmp1.add(s.position.mul(s.mass, cache.tmp3));
						cache.tmp2.add(s.velocity.mul(s.mass, cache.tmp3));
					}
					double invOtherMassSum = 1.0 / othersMassSum;
					cache.tmp1.mul(invOtherMassSum);
					cache.tmp2.mul(invOtherMassSum);
					double Ep = othersMassSum * star.mass / cache.tmp1.distance(star.position);
					double Ek = 0.5 * (othersMassSum * cache.tmp2.lengthSquared() + star.mass * star.velocity.lengthSquared());
					if(!(Ek < Ep)) return true;
				}
				return false;
			}
		}
		
		private void registerAll(boolean b) {
			Registries.PRE_MAIN.register(this, b);
			Registries.BEFORE_TRANSLUCENT.register(this, b);
		}
	}
}
