package lpctools.mixin.client.utils;

import com.llamalad7.mixinextras.sugar.Local;
import lpctools.generic.GenericUtils;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class RenderInstance {
	@Inject(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RawProjectionMatrix;set(Lorg/joml/Matrix4f;)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;"))
	void recordWorldProjectionMatrixAndBias(RenderTickCounter renderTickCounter, CallbackInfo ci, @Local Matrix4f matrix4f) {
		var recorded = lpctools.lpcfymasaapi.render.translucentShapes.RenderInstance.worldProjectionMatrixBiased;
		recorded.set(matrix4f);
		recorded.m23(recorded.m23() - GenericUtils.zFightBias());
	}
}
