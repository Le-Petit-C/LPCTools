package lpctools.mixin.client.accessors;

import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.world.level.block.entity.SignTextSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** 告示牌分数辅助要拿界面的私有字段：编辑的是哪一面、以及四行文本（原版的 {@code removed()} 就是拿 messages 发的包） */
@Mixin(AbstractSignEditScreen.class)
public interface AbstractSignEditScreenAccessor {
	@Accessor("slot") SignTextSlot lpctools$getSlot();
	@Accessor("messages") String[] lpctools$messages();
}
