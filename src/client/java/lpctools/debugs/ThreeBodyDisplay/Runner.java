package lpctools.debugs.ThreeBodyDisplay;

import com.mojang.blaze3d.IndexType;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import fi.dy.masa.malilib.util.data.Color4f;
import lpctools.LPCTools;
import lpctools.lpcfymasaapi.Registries;
import lpctools.lpcfymasaapi.render.LPCRenderPipelines;
import lpctools.lpcfymasaapi.render.translucentShapes.*;
import lpctools.util.RenderUtils;
import lpctools.util.javaex.QuietAutoCloseable;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.util.Mth;
import org.joml.*;
import org.jspecify.annotations.NonNull;
import org.lwjgl.system.MemoryUtil;

import java.lang.Math;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Supplier;

import static lpctools.debugs.ThreeBodyDisplay.ThreeBodyDisplay.*;
import static lpctools.debugs.ThreeBodyDisplay.Utils.getBrightness;
import static lpctools.debugs.ThreeBodyDisplay.Utils.vector3d2Color;

class Runner implements QuietAutoCloseable, Registries.WorldPreMainRender, LevelRenderEvents.BeforeTranslucentTerrain {
	private static final String baseLabel = "LPCTools TranslucentQuadsRenderInstance";
	private static final Supplier<String> indexBufferLabel = () -> appendLabel("IndexBuffer");
	private static final Supplier<String> vertexBufferLabel = () -> appendLabel("VertexBuffer");
	private static final Supplier<String> renderPassLabel = () -> appendLabel("RenderPass");
	private static final Supplier<String> dropCircleIndexBufferBufferLabel = () -> appendLabel("DropCircleIndexBuffer");
	private static final Supplier<String> dropCircleVertexBufferLabel = () -> appendLabel("DropCircleVertexBuffer");
	private static final Supplier<String> gridVertexBufferLabel = () -> appendLabel("DropCircleVertexBuffer");
	
	private static String appendLabel(String tail) { return baseLabel + ' ' + tail; }
	
	private static final int ticksPerLoop = 256;
	private static final double tickFactor = 1.0 / 256.0 / ticksPerLoop;
	private static final int dropCircleSplitCount = 1024;
	
	private final Star[] stars = new Star[3];
	private final StarRenderData[] starsRenderData = new StarRenderData[3];
	private final ProjectionMatrixBuffer rawProjectionMatrixBuffer = new ProjectionMatrixBuffer("LPCTools ThreeBodyDisplay");
	private final ByteBuffer dataBuffer;
	private final GpuBuffer indexBuffer;
	private final GpuBuffer vertexBuffer;
	private GpuBuffer dropCircleIndexBuffer;
	private GpuBuffer dropCircleVertexBuffer;
	private GpuBuffer gridVertexBuffer;
	private int lastGridSize;
	
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
		dataBuffer = MemoryUtil.memAlloc(DefaultVertexFormat.POSITION_COLOR_LINE_WIDTH.getVertexSize() * 24);
		vertexBuffer = RenderSystem.getDevice().createBuffer(vertexBufferLabel,
			GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST, dataBuffer.capacity());
		updateTracks();
		registerAll(true);
	}
	
	void updateRandomizeDataPack() {
		runnerDataPack = new RunnerDataPack(
			maxTrackSpeed.getDoubleValue(),
			Mth.square(distanceLimit.getDoubleValue()),
			timeSpeed.getDoubleValue(),
			spreadRadius.getDoubleValue(),
			spreadSpeed.getDoubleValue(),
			massDeviation.getDoubleValue()
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

	private GpuBuffer getDropCircleIndexBuffer() {
		if(dropCircleIndexBuffer != null) return dropCircleIndexBuffer;
		ByteBuffer buffer = MemoryUtil.memAlloc(4 * 2 * (dropCircleSplitCount + 1));
		buffer.putInt(0);
		for(int i = 1; i < dropCircleSplitCount; ++i)
			buffer.putInt(i).putInt(i);
		buffer.putInt(0);
		buffer.putInt(dropCircleSplitCount).putInt(dropCircleSplitCount + 1);
		buffer.flip();
		dropCircleIndexBuffer = RenderSystem.getDevice().createBuffer(dropCircleIndexBufferBufferLabel, GpuBuffer.USAGE_INDEX | GpuBuffer.USAGE_COPY_DST, buffer);
		MemoryUtil.memFree(buffer);
		return dropCircleIndexBuffer;
	}

	private GpuBuffer getDropCircleVertexBuffer() {
		if(dropCircleVertexBuffer != null) return dropCircleVertexBuffer;
		ByteBuffer buffer = MemoryUtil.memAlloc(DefaultVertexFormat.POSITION_COLOR_LINE_WIDTH.getVertexSize() * (dropCircleSplitCount + 2));
		for(int i = 0; i < dropCircleSplitCount; ++i) {
			double angle = Math.PI * 2 * i / dropCircleSplitCount;
			buffer.putFloat(Mth.cos(angle)).putFloat(0).putFloat(Mth.sin(angle)).putInt(0xffffffff).putFloat(1.0f);
		}
		buffer.putFloat(0).putFloat(0).putFloat(0).putInt(0xffffffff).putFloat(1.0f);
		buffer.putFloat(0).putFloat(1).putFloat(0).putInt(0xffffffff).putFloat(1.0f);
		buffer.flip();
		dropCircleVertexBuffer = RenderSystem.getDevice().createBuffer(dropCircleVertexBufferLabel, GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST, buffer);
		MemoryUtil.memFree(buffer);
		return dropCircleVertexBuffer;
	}

	private GpuBuffer getGridVertexBuffer(int gridSize) {
		if(gridVertexBuffer != null && gridSize == lastGridSize) return gridVertexBuffer;
		if(gridVertexBuffer != null) gridVertexBuffer.close();
		lastGridSize = gridSize;
		ByteBuffer buffer = MemoryUtil.memAlloc(DefaultVertexFormat.POSITION_COLOR_LINE_WIDTH.getVertexSize() * (gridSize + 1) * 4);
		float min = -gridSize * 0.5f, max = -min;
		for(int i = 0; i <= gridSize; ++i) {
			float pos = min + i;
			buffer.putFloat(pos).putFloat(0).putFloat(min).putInt(0xffffffff).putFloat(1.0f);
			buffer.putFloat(pos).putFloat(0).putFloat(max).putInt(0xffffffff).putFloat(1.0f);
			buffer.putFloat(min).putFloat(0).putFloat(pos).putInt(0xffffffff).putFloat(1.0f);
			buffer.putFloat(max).putFloat(0).putFloat(pos).putInt(0xffffffff).putFloat(1.0f);
		}
		buffer.flip();
		return gridVertexBuffer = RenderSystem.getDevice().createBuffer(gridVertexBufferLabel, GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST, buffer);
	}
	
	@Override public void close() {
		registerAll(false);
		running = false;
		rawProjectionMatrixBuffer.close();
		MemoryUtil.memFree(dataBuffer);
		indexBuffer.close();
		vertexBuffer.close();
		for (var star : starsRenderData) star.close();
		if(dropCircleIndexBuffer != null) dropCircleIndexBuffer.close();
		if(dropCircleVertexBuffer != null) dropCircleVertexBuffer.close();
		if(gridVertexBuffer != null) gridVertexBuffer.close();
	}
	
	@Override public void beforeTranslucentTerrain(@NonNull LevelRenderContext ignored) {
		var context = recordedContext;
		dataBuffer.clear();
		var camPos = context.camera().pos;
		double deltaSeconds = System.currentTimeMillis() * 0.001 - lastTimeSeconds;
		lastTimeSeconds += deltaSeconds;
		double brightness = 0;
		Vector3d basePoint = ThreeBodyDisplay.massCenter.getPos(new Vector3d());
		float radiusFactor = (float) starRadiusFactor.getDoubleValue();
		synchronized (this) {
			for (var star : starsRenderData) {
				double starBrightness = getBrightness(star.light);
				for (int i = 0; i < 8; ++i) {
					dataBuffer.putFloat((float)star.position.x).putFloat((float)star.position.y).putFloat((float)star.position.z)
						.putInt(vector3d2Color(star.light, lightFactor)).putFloat(star.radius * radiusFactor);
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
		RenderTarget fb = context.fb();
		GpuTextureView colorAttachmentView = RenderUtils.colorAttachmentViewOrDef(fb);
		GpuTextureView depthAttachmentView = fb.useDepth ? fb.getDepthTextureView() : null;
		Vector3f offset = new Vector3f((float) (basePoint.x - camPos.x), (float) (basePoint.y - camPos.y), (float) (basePoint.z - camPos.z));
		Vector4f colorModulator = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
		Matrix4f identity = new Matrix4f();
		GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
			.writeTransform(RenderSystem.getModelViewMatrixCopy(), colorModulator, offset, identity);
		try (RenderPass renderPass = commandEncoder
			.createRenderPass(renderPassLabel, colorAttachmentView, Optional.empty(), depthAttachmentView, OptionalDouble.empty())) {
			renderPass.setPipeline(LPCRenderPipelines.spherePipeline);
			RenderSystem.bindDefaultUniforms(renderPass);
			renderPass.setUniform("DynamicTransforms", dynamicTransforms);
			renderPass.setIndexBuffer(indexBuffer, IndexType.SHORT);
			renderPass.setVertexBuffer(0, vertexBuffer.slice());
			renderPass.drawIndexed(3 * Sphere.baseIndices.length * Sphere.baseIndices[0].length, 1, 0, 0, 0);
		}
		double projOffset = projectionPlaneYOffset.getDoubleValue();
		if(renderStarProjection.getAsBoolean()) {
			GpuBuffer dropCircleIndexBuffer = getDropCircleIndexBuffer();
			GpuBuffer dropCircleVertexBuffer = getDropCircleVertexBuffer();
			Vector3d dOffset = new Vector3d();
			Vector3f zeroOffset = new Vector3f();
			RenderPipeline pipeline = LPCRenderPipelines.positionColorPipeline(true, false);
			for (StarRenderData star : starsRenderData) {
				Matrix4f modelView = RenderSystem.getModelViewMatrixCopy();
				float height = (float) (star.position.y() - projOffset);
				dOffset.set(star.position.x, projOffset, star.position.z).add(offset);
				modelView.translate((float) dOffset.x(), (float) dOffset.y(), (float) dOffset.z());
				modelView.scale(star.radius * radiusFactor, height, star.radius * radiusFactor);
				Color4f lineColor = Color4f.fromColor(star.lineColor);
				colorModulator.set(lineColor.r, lineColor.g, lineColor.b, (float)projectionAlpha.getDoubleValue());
				GpuBufferSlice starDropLineTransforms = RenderSystem.getDynamicUniforms()
					.writeTransform(modelView, colorModulator, zeroOffset, identity);
				try (RenderPass renderPass = commandEncoder
					.createRenderPass(renderPassLabel, colorAttachmentView, Optional.empty(), depthAttachmentView, OptionalDouble.empty())) {
					renderPass.setPipeline(pipeline);
					RenderSystem.bindDefaultUniforms(renderPass);
					renderPass.setUniform("DynamicTransforms", starDropLineTransforms);
					renderPass.setIndexBuffer(dropCircleIndexBuffer, IndexType.INT);
					renderPass.setVertexBuffer(0, dropCircleVertexBuffer.slice());
					renderPass.drawIndexed(2 * (dropCircleSplitCount + 1), 1, 0, 0, 0);
				}
			}
		}
		if(renderProjectionPlaneGrid.getAsBoolean()) {
			int gridSize = ThreeBodyDisplay.gridSize.getIntegerValue();
			GpuBuffer gridVertexBuffer = getGridVertexBuffer(gridSize);
			Vector3f zeroOffset = new Vector3f();
			RenderPipeline pipeline = LPCRenderPipelines.positionColorPipeline(true, false);
			Matrix4f modelView = RenderSystem.getModelViewMatrixCopy();
			modelView.translate(offset).translate(0, (float)projOffset, 0);
			modelView.scale((float) gridUnitLength.getDoubleValue());
			Color4f color = gridColor.getColor();
			colorModulator.set(color.r, color.g, color.b, color.a);
			GpuBufferSlice gridTransforms = RenderSystem.getDynamicUniforms()
				.writeTransform(modelView, colorModulator, zeroOffset, identity);
			try (RenderPass renderPass = commandEncoder
				.createRenderPass(renderPassLabel, colorAttachmentView, Optional.empty(), depthAttachmentView, OptionalDouble.empty())) {
				renderPass.setPipeline(pipeline);
				RenderSystem.bindDefaultUniforms(renderPass);
				renderPass.setUniform("DynamicTransforms", gridTransforms);
				renderPass.setVertexBuffer(0, gridVertexBuffer.slice());
				renderPass.draw(4 * (gridSize + 1), 1, 0, 0);
			}
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
		Registries.BEFORE_TRANSLUCENT_TERRAIN.register(this, b);
	}
}
