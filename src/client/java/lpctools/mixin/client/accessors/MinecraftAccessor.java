package lpctools.mixin.client.accessors;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Minecraft.class)
public interface MinecraftAccessor {
	@Invoker void invokeStartUseItem();
	@SuppressWarnings("UnusedReturnValue")
	@Invoker boolean invokeStartAttack();
}
