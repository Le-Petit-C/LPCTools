package lpctools.mixin.client;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = TextFieldWidget.class)
public interface TextFieldWidgetAccessor {
	@Invoker(value = "updateTextPosition") void invokeUpdateTextPosition();
	@Accessor(value = "textRenderer") void setTextRenderer(TextRenderer renderer);
}
