package lpctools.mixinInterfaces.MASAMixins;

import fi.dy.masa.malilib.gui.widgets.WidgetListBase;
import fi.dy.masa.malilib.gui.widgets.WidgetListEntryBase;

public interface IGuiListBaseMixin<TYPE, WIDGET extends WidgetListEntryBase<TYPE>, WIDGETLIST extends WidgetListBase<TYPE, WIDGET>> {
	WIDGETLIST invokeGetListWidget();
}
