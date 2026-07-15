package lpctools.mixin.client.events;

import com.llamalad7.mixinextras.sugar.Local;
import lpctools.lpcfymasaapi.render.RenderEventHandler;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.profiling.ProfilerFiller;
import org.joml.Matrix4f;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LevelRenderer.class)
public abstract class MixinWorldRenderer {
	@Shadow @Final private Minecraft minecraft;
	@Shadow @Final private RenderBuffers renderBuffers;
	
	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;clear(Z)V", shift = At.Shift.AFTER))
	private void lpctools_onRenderWorldMain(DeltaTracker deltaTracker, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f positionMatrix, Matrix4f projectionMatrix, CallbackInfo ci, @Local ProfilerFiller profiler, @Local Frustum frustum) {
		RenderEventHandler.runRenderWorldPreMain(positionMatrix, projectionMatrix, this.minecraft, frustum, camera, profiler);
	}
}
