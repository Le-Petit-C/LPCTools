package lpctools.mixin.client.MASAMixins;

import fi.dy.masa.malilib.gui.GuiListBase;
import fi.dy.masa.malilib.gui.widgets.WidgetListBase;
import fi.dy.masa.malilib.gui.widgets.WidgetListEntryBase;
import lpctools.mixinInterfaces.MASAMixins.IGuiListBaseMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = GuiListBase.class, remap = false)
public abstract class GuiListBaseMixin<TYPE, WIDGET extends WidgetListEntryBase<TYPE>, WIDGETLIST extends WidgetListBase<TYPE, WIDGET>> implements IGuiListBaseMixin<TYPE, WIDGET, WIDGETLIST> {
	@Invoker(remap = false) public abstract WIDGETLIST invokeGetListWidget();
}
