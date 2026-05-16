package lpctools.mixin.client.accessors;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = EditBox.class)
public interface TextFieldWidgetAccessor {
	@Invoker(value = "updateTextPosition") void invokeUpdateTextPosition();
	@Accessor(value = "font") void setTextRenderer(Font renderer);
}
