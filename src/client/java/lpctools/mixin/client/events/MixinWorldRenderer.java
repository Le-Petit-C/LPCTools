package lpctools.mixin.client.events;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import lpctools.lpcfymasaapi.render.RenderEventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.util.profiling.ProfilerFiller;
import org.joml.Matrix4f;
import org.joml.Vector4f;

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
	@Shadow @Final private LevelRenderState levelRenderState;
	@Shadow @Final private LevelTargetBundle targets;
	@Shadow @Final private RenderBuffers renderBuffers;
	
	@Inject(method = "render", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/renderer/LevelRenderer;addMainPass(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher$PreparedFrame;Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;Lnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;Z)V"))
	private void lpctools_onRenderWorldMain(GraphicsResourceAllocator resourceAllocator, boolean renderOutline, CameraRenderState cameraState, GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky, boolean consistentDepthRequired, CallbackInfo ci, @Local(name = "profiler") ProfilerFiller profiler, @Local(name = "frame") FrameGraphBuilder frame)
	{
		Minecraft minecraft = Minecraft.getInstance();
		Frustum cullFrustum = cameraState.cullFrustum;
		RenderEventHandler.runRenderWorldPreMain(minecraft, frame, this.targets, cullFrustum, levelRenderState.cameraRenderState, this.renderBuffers, profiler);
	}
}
