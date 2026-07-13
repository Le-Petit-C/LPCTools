package lpctools.mixin.client.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import lpctools.mixinInterfaces.minecraft.IMatrixStackEntryMixin;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PoseStack.Pose.class)
public class MatrixStackEntryMixin implements IMatrixStackEntryMixin {
	@Shadow @Final Matrix4f pose;
	@Shadow @Final Matrix3f normal;
	@Shadow boolean trustedNormals;
	@SuppressWarnings("DataFlowIssue") @Override
	public void lPCTools$copy(PoseStack.Pose entry) {
		var e = (MatrixStackEntryMixin)(Object)entry;
		pose.set(e.pose);
		normal.set(e.normal);
		trustedNormals = e.trustedNormals;
	}
}
