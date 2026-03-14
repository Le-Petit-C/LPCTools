package lpctools.mixin.client.utils;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

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
	@ModifyArgs(method = "renderWorld", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4f;scale(FFF)Lorg/joml/Matrix4f;", remap = false))
	void recordScaleEffects(Args args) {
		float x = args.get(0);
		float y = args.get(1);
		float z = args.get(2);
		worldBasicProjectionMatrix.scale(x, y, z);
		worldProjectionTranslateMatrix.scaleLocal(1.0f / x, 1.0f / y, 1.0f / z).scale(x, y, z);
	}
	@Unique Quaternionf qCache = new Quaternionf();
	@ModifyArgs(method = "renderWorld", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4f;rotate(FLorg/joml/Vector3fc;)Lorg/joml/Matrix4f;", remap = false))
	void recordRotateEffects(Args args) {
		float angle = args.get(0);
		Vector3fc axis = args.get(1);
		qCache.fromAxisAngleRad(axis, angle);
		worldBasicProjectionMatrix.rotate(qCache);
		worldProjectionTranslateMatrix.rotate(qCache).rotateLocal(qCache.conjugate());
	}
}
