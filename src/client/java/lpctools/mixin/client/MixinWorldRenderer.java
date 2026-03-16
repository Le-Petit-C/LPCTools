package lpctools.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import lpctools.lpcfymasaapi.render.RenderEventHandler;
import net.minecraft.client.render.*;
import org.joml.Matrix4f;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.util.profiler.Profiler;

@Mixin(value = WorldRenderer.class)
public abstract class MixinWorldRenderer
{
	@Shadow @Final private MinecraftClient client;
	@Shadow @Final private DefaultFramebufferSet framebufferSet;
	@Shadow @Final private BufferBuilderStorage bufferBuilders;
	
	@SuppressWarnings("DiscouragedShift") @Inject(method = "render",
		at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/render/WorldRenderer;renderMain(Lnet/minecraft/client/render/FrameGraphBuilder;Lnet/minecraft/client/render/Frustum;Lnet/minecraft/client/render/Camera;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lnet/minecraft/client/render/Fog;ZZLnet/minecraft/client/render/RenderTickCounter;Lnet/minecraft/util/profiler/Profiler;)V",
			shift = At.Shift.BEFORE))
	private void lpctools_onRenderWorldMain(ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, Matrix4f positionMatrix, Matrix4f projectionMatrix, CallbackInfo ci, @Local Profiler profiler, @Local Frustum frustum, @Local FrameGraphBuilder frameGraphBuilder)
	{
		RenderEventHandler.runRenderWorldPreMain(positionMatrix, projectionMatrix, this.client, frameGraphBuilder, this.framebufferSet, frustum, camera, profiler);
	}
}
