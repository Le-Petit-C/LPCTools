package lpctools.mixin.client.utils;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.GameRenderState;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import static lpctools.lpcfymasaapi.render.translucentShapes.RenderInstance.*;

@Mixin(GameRenderer.class)
public class RenderInstance {
	@Shadow
	@Final
	private GameRenderState gameRenderState;
	
	@Inject(method = "renderLevel", at = @At(value = "HEAD"))
	void recordBasicProjectionMatrix(DeltaTracker deltaTracker, CallbackInfo ci) {
		worldBasicProjectionMatrix.set(gameRenderState.levelRenderState.cameraRenderState.projectionMatrix);
	}
	@ModifyArg(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ProjectionMatrixBuffer;getBuffer(Lorg/joml/Matrix4f;)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;"))
	Matrix4f recordWorldProjectionMatrix(Matrix4f projectionMatrix) {
		worldProjectionMatrix.set(projectionMatrix);
		return projectionMatrix;
	}
	@ModifyReceiver(method = "renderLevel", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack$Pose;pose()Lorg/joml/Matrix4f;"))
	PoseStack.Pose recordMatrixStackEntry(PoseStack.Pose instance) {
		worldProjectionTranslateMatrix.set(instance.pose());
		return instance;
	}
	@ModifyArgs(method = "renderLevel", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4f;scale(FFF)Lorg/joml/Matrix4f;", remap = false))
	void recordScaleEffects(Args args) {
		float x = args.get(0);
		float y = args.get(1);
		float z = args.get(2);
		worldBasicProjectionMatrix.scale(x, y, z);
		worldProjectionTranslateMatrix.scaleLocal(1.0f / x, 1.0f / y, 1.0f / z).scale(x, y, z);
	}
	@Unique final Quaternionf qCache = new Quaternionf();
	@ModifyArgs(method = "renderLevel", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4f;rotate(FLorg/joml/Vector3fc;)Lorg/joml/Matrix4f;", remap = false))
	void recordRotateEffects(Args args) {
		float angle = args.get(0);
		Vector3fc axis = args.get(1);
		qCache.fromAxisAngleRad(axis, angle);
		worldBasicProjectionMatrix.rotate(qCache);
		worldProjectionTranslateMatrix.rotate(qCache).rotateLocal(qCache.conjugate());
	}
}
