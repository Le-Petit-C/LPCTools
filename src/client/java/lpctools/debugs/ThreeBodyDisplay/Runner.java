package lpctools.debugs.ThreeBodyDisplay;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import lpctools.LPCTools;
import lpctools.lpcfymasaapi.Registries;
import lpctools.lpcfymasaapi.render.LPCRenderPipelines;
import lpctools.lpcfymasaapi.render.translucentShapes.*;
import lpctools.util.javaex.QuietAutoCloseable;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.render.RawProjectionMatrix;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.MathHelper;
import org.joml.*;
import org.jspecify.annotations.NonNull;
import org.lwjgl.system.MemoryUtil;

import java.lang.Math;
import java.nio.ByteBuffer;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.Supplier;

import static lpctools.debugs.ThreeBodyDisplay.ThreeBodyDisplay.*;
import static lpctools.debugs.ThreeBodyDisplay.Utils.getBrightness;
import static lpctools.debugs.ThreeBodyDisplay.Utils.vector3d2Color;

class Runner implements QuietAutoCloseable, Registries.WorldPreMainRender, WorldRenderEvents.BeforeTranslucent {
	private static final String baseLabel = "LPCTools TranslucentQuadsRenderInstance";
	private static final Supplier<String> indexBufferLabel = () -> appendLabel("IndexBuffer");
	private static final Supplier<String> vertexBufferLabel = () -> appendLabel("VertexBuffer");
	private static final Supplier<String> renderPassLabel = () -> appendLabel("RenderPass");
	
	private static String appendLabel(String tail) {return baseLabel + ' ' + tail;}
	
	private static final int ticksPerLoop = 256;
	private static final double tickFactor = 1.0 / 256.0 / ticksPerLoop;
	
	private final Star[] stars = new Star[3];
	private final StarRenderData[] starsRenderData = new StarRenderData[3];
	private final RawProjectionMatrix rawProjectionMatrixBuffer = new RawProjectionMatrix("LPCTools ThreeBodyDisplay");
	private final ByteBuffer dataBuffer;
	private final GpuBuffer indexBuffer;
	private final GpuBuffer vertexBuffer;
	
	private volatile boolean running = true;
	private volatile RunnerDataPack runnerDataPack;
	private int currentTrackCount;
	private double lightFactor = 1;
	private double lastTimeSeconds = System.currentTimeMillis() / 1000.0;
	private boolean shouldCleanTracks = false;
	
	Runner() {
		updateRandomizeDataPack();
		synchronized (this) {
			new Thread(this::run, "ThreeBodySim").start();
			try {
				this.wait();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		var indexDataBuffer = MemoryUtil.memAlloc(3 * 2 * Sphere.baseIndices.length * Sphere.baseIndices[0].length);
		for (int i = 0; i < 3; ++i)
			for (var a : Sphere.baseIndices)
				for (var b : a)
					indexDataBuffer.putShort((short) (i * 8 + b));
		indexDataBuffer.flip();
		indexBuffer = RenderSystem.getDevice().createBuffer(indexBufferLabel,
			GpuBuffer.USAGE_INDEX | GpuBuffer.USAGE_COPY_DST, indexDataBuffer);
		MemoryUtil.memFree(indexDataBuffer);
		dataBuffer = MemoryUtil.memAlloc(VertexFormats.POSITION_COLOR_LINE_WIDTH.getVertexSize() * 24);
		vertexBuffer = RenderSystem.getDevice().createBuffer(vertexBufferLabel,
			GpuBuffer.USAGE_INDEX | GpuBuffer.USAGE_COPY_DST, dataBuffer.capacity());
		updateTracks();
		registerAll(true);
	}
	
	void updateRandomizeDataPack() {
		runnerDataPack = new RunnerDataPack(
			maxTrackSpeed.getDoubleValue(),
			MathHelper.square(distanceLimit.getDoubleValue()),
			timeSpeed.getDoubleValue(),
			spreadRadius.getDoubleValue(),
			spreadSpeed.getDoubleValue(),
			massDeviation.getDoubleValue(),
			(float) starRadiusFactor.getDoubleValue()
		);
	}
	
	void updateTracks() {
		int settingTrackCount = ThreeBodyDisplay.renderTrackCount.getIntegerValue();
		if (settingTrackCount != currentTrackCount) {
			synchronized (this) {
				for (var star : starsRenderData)
					star.resetTracks(settingTrackCount);
			}
			currentTrackCount = settingTrackCount;
		}
	}
	
	@Override public void close() {
		registerAll(false);
		running = false;
		rawProjectionMatrixBuffer.close();
		MemoryUtil.memFree(dataBuffer);
		indexBuffer.close();
		vertexBuffer.close();
		for (var star : starsRenderData)
			star.close();
	}
	
	@Override public void beforeTranslucent(@NonNull WorldRenderContext ignored) {
		var context = recordedContext;
		dataBuffer.clear();
		var camPos = context.camera().getCameraPos();
		double deltaSeconds = System.currentTimeMillis() * 0.001 - lastTimeSeconds;
		lastTimeSeconds += deltaSeconds;
		double brightness = 0;
		Vector3d basePoint = ThreeBodyDisplay.massCenter.getPos(new Vector3d());
		synchronized (this) {
			for (var star : starsRenderData) {
				double starBrightness = getBrightness(star.light);
				for (int i = 0; i < 8; ++i) {
					dataBuffer.putFloat((float)star.position.x).putFloat((float)star.position.y).putFloat((float)star.position.z)
						.putInt(vector3d2Color(star.light, lightFactor)).putFloat(star.radius * runnerDataPack.starRadiusFactor());
				}
				double dstSqr = star.position.distanceSquared(camPos.x, camPos.y, camPos.z);
				brightness = Math.max(starBrightness * (dstSqr <= star.rSquare ? 1.0 : 1.0 / (1.0 + Math.log(dstSqr / star.rSquare))), brightness);
				if(shouldCleanTracks) star.resetTracks(currentTrackCount);
				else star.updateRenderThreadTracks(basePoint);
			}
			shouldCleanTracks = false;
		}
		double logInvBrightness = -Math.log(brightness);
		double logLightFactor = Math.log(lightFactor);
		lightFactor = Math.exp(logInvBrightness + Math.exp(-deltaSeconds) * (logLightFactor - logInvBrightness));
		
		dataBuffer.flip();
		var commandEncoder = RenderSystem.getDevice().createCommandEncoder();
		commandEncoder.writeToBuffer(vertexBuffer.slice(), dataBuffer);
		var fb = context.fb();
		GpuTextureView colorAttachmentView = fb.getColorAttachmentView();
		GpuTextureView depthAttachmentView = fb.useDepthAttachment ? fb.getDepthAttachmentView() : null;
		Vector3f offset = new Vector3f();
		Matrix4f modelViewMatrix = new Matrix4f(RenderSystem.getModelViewMatrix());
		Matrix4f projectionMatrix = new Matrix4f(RenderInstance.worldBasicProjectionMatrix);
		RenderInstance.worldProjectionTranslateMatrix.mul(modelViewMatrix, modelViewMatrix);
		modelViewMatrix.get3x3(new Matrix3f()).invert().transform(modelViewMatrix.getColumn(3, offset));
		offset.mul(-1);
		modelViewMatrix.translate(offset);
		offset.mul(-1);
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
			double lastSeconds = System.currentTimeMillis() * 0.001;
			double backSeconds = lastSeconds;
			double trackingProgress = 0;
			java.util.Random random = new java.util.Random();
			for (int i = 0; i < stars.length; ++i) stars[i] = new Star(random, runnerDataPack);
			for (int i = 0; i < starsRenderData.length; ++i) //noinspection resource
				starsRenderData[i] = new StarRenderData(stars[i]);
			synchronized (this) {
				this.notify();
			}
			while (running) {
				double deltaSeconds = System.currentTimeMillis() * 0.001 - lastSeconds;
				lastSeconds += deltaSeconds;
				trackingProgress += deltaSeconds * runnerDataPack.maxTrackSpeed();
				double reserved = lastSeconds - backSeconds;
				double ds = reserved * tickFactor;
				backSeconds += ds * ticksPerLoop;
				double dt = ds * runnerDataPack.timeSpeed();
				for (int i = 0; i < ticksPerLoop; ++i) Star.tick(stars, dt, cache);
				boolean isOutOfRange = Star.isOutOfRange(stars, cache, runnerDataPack);
				if(isOutOfRange) Star.randomizeStars(stars, random, runnerDataPack, cache);
				else Star.normalize(stars, cache);
				boolean shouldTrack = trackingProgress >= 1;
				if (shouldTrack) {
					trackingProgress -= 1;
					if (trackingProgress > 1) trackingProgress = 1;
				}
				synchronized (this) {
					for (int i = 0; i < stars.length; ++i)
						starsRenderData[i].set(stars[i], shouldTrack);
					if(isOutOfRange) shouldCleanTracks = true;
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
	
	private void registerAll(boolean b) {
		Registries.PRE_MAIN.register(this, b);
		Registries.BEFORE_TRANSLUCENT.register(this, b);
	}
}
