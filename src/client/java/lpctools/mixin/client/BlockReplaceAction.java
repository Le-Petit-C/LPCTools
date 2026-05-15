package lpctools.mixin.client;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Minecraft.class)
public interface BlockReplaceAction {
    @SuppressWarnings("UnusedReturnValue")
    @Invoker("startAttack")
    boolean invokeDoAttack();
    @Invoker("startUseItem")
    void invokeDoItemUse();
}
