package lpctools.mixin.client.accessors;

import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MatrixStack.Entry.class)
public interface MatrixStackEntryAccessor {
	@Invoker void invokeCopy(MatrixStack.Entry entry);
}
