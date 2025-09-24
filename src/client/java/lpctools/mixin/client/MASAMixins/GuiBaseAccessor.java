package lpctools.mixin.client.MASAMixins;

import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.widgets.WidgetBase;
import fi.dy.masa.malilib.gui.wrappers.TextFieldWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(value = GuiBase.class, remap = false)
public interface GuiBaseAccessor {
	@Accessor(value = "widgets", remap = false) List<WidgetBase> getWidgets();
	@Accessor(value = "buttons", remap = false) List<ButtonBase> getButtons();
	@Accessor(value = "textFields", remap = false) List<TextFieldWrapper<? extends GuiTextFieldGeneric>> getTextFields();
}
