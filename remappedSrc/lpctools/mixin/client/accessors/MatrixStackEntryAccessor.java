package lpctools.mixin.client.accessors;

import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PoseStack.Pose.class)
public interface MatrixStackEntryAccessor {
	@Invoker void invokeSet(PoseStack.Pose entry);
}
