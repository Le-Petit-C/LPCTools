package lpctools.lpcfymasaapi.interfaces;

import fi.dy.masa.malilib.config.IConfigResettable;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.gui.interfaces.IKeybindConfigGui;
import fi.dy.masa.malilib.gui.widgets.WidgetBase;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptionsBase;

public interface ButtonConsumer {
    @SuppressWarnings("UnusedReturnValue")
    <T extends ButtonBase> T addButton(T button, IButtonActionListener listener);
    ButtonGeneric createResetButton(int x, int y, IConfigResettable config);
    IKeybindConfigGui getKeybindHost();
    @SuppressWarnings("UnusedReturnValue")
    <T extends WidgetBase> T addWidget(T widget);
    WidgetListConfigOptionsBase<?, ?> getWidgetListConfigOptionsBase();
}
