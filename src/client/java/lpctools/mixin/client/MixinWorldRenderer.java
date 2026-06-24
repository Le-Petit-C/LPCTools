package lpctools.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import lpctools.lpcfymasaapi.render.RenderEventHandler;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.profiling.ProfilerFiller;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LevelRenderer.class)
public abstract class MixinWorldRenderer
{
	@Shadow @Final private LevelTargetBundle targets;
	@Shadow @Final private RenderBuffers renderBuffers;
	
	@SuppressWarnings("DiscouragedShift") @Inject(method = "render",
		at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/LevelRenderer;addMainPass(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher$PreparedFrame;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lnet/minecraft/client/renderer/state/level/LevelRenderState;Lnet/minecraft/util/profiling/ProfilerFiller;Lnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;)V",
			shift = At.Shift.BEFORE))
	private void lpctools_onRenderWorldMain(GraphicsResourceAllocator resourceAllocator, DeltaTracker deltaTracker, boolean renderOutline, CameraRenderState cameraState,
											Matrix4fc modelViewMatrix, GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky,
											CallbackInfo ci,
											@Local(name = "profiler") ProfilerFiller profiler,
											@Local(name = "frame") FrameGraphBuilder frame)
	{
		Minecraft minecraft = Minecraft.getInstance();
		Frustum cullFrustum = cameraState.cullFrustum;
		Camera camera = minecraft.gameRenderer.mainCamera();
		RenderEventHandler.runRenderWorldPreMain(new Matrix4f(), cameraState.projectionMatrix, minecraft, frame, this.targets, cullFrustum, camera, this.renderBuffers, profiler);
	}
}
