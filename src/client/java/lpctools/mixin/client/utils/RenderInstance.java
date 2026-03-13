package lpctools.mixin.client.utils;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static lpctools.lpcfymasaapi.render.translucentShapes.RenderInstance.*;

@Mixin(GameRenderer.class)
public class RenderInstance {
	@ModifyExpressionValue(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;getBasicProjectionMatrix(F)Lorg/joml/Matrix4f;"))
	Matrix4f recordBasicProjectionMatrix(Matrix4f original) {
		worldBasicProjectionMatrix.set(original);
		return original;
	}
	@Inject(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RawProjectionMatrix;set(Lorg/joml/Matrix4f;)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;"))
	void recordWorldProjectionMatrixAndBias(RenderTickCounter renderTickCounter, CallbackInfo ci, @Local Matrix4f matrix4f) {
		worldProjectionMatrix.set(matrix4f);
	}
	@Inject(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;peek()Lnet/minecraft/client/util/math/MatrixStack$Entry;"))
	void recordMatrixStack(RenderTickCounter renderTickCounter, CallbackInfo ci, @Local MatrixStack matrixStack) {
		worldProjectionTranslateMatrix.set(matrixStack.peek().getPositionMatrix());
	}
	@Inject(method = "renderWorld", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4f;rotate(FLorg/joml/Vector3fc;)Lorg/joml/Matrix4f;", ordinal = 0))
	void recordRotateEffects(RenderTickCounter renderTickCounter, CallbackInfo ci, @Local(name = "m") float m, @Local Vector3f vector3f, @Local(name = "l") float l) {
		Matrix4f A = new Matrix4f().rotate(m, vector3f).scale(1.0F / l, 1.0F, 1.0F).rotate(-m, vector3f);
		worldBasicProjectionMatrix.mul(A);
		A.invert(new Matrix4f()).mul(worldProjectionTranslateMatrix, worldProjectionTranslateMatrix).mul(A);
	}
}
