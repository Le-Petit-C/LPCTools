package lpctools.mixin.client.utils;

import lpctools.mixinInterfaces.minecraft.IMatrixStackEntryMixin;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MatrixStack.Entry.class)
public class MatrixStackEntryMixin implements IMatrixStackEntryMixin {
	@Shadow @Final Matrix4f positionMatrix;
	@Shadow @Final Matrix3f normalMatrix;
	@Shadow boolean canSkipNormalization;
	@SuppressWarnings("DataFlowIssue") @Override
	public void lPCTools$copy(MatrixStack.Entry entry) {
		var e = (MatrixStackEntryMixin)(Object)entry;
		positionMatrix.set(e.positionMatrix);
		normalMatrix.set(e.normalMatrix);
		canSkipNormalization = e.canSkipNormalization;
	}
}
