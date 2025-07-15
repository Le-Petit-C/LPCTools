package lpctools.lpcfymasaapi.interfaces;

import fi.dy.masa.malilib.config.IConfigResettable;
import fi.dy.masa.malilib.config.gui.ConfigOptionChangeListenerTextField;
import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.gui.interfaces.IKeybindConfigGui;
import fi.dy.masa.malilib.gui.widgets.WidgetBase;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptionsBase;
import net.minecraft.client.font.TextRenderer;

public interface ButtonConsumer {
    @SuppressWarnings("UnusedReturnValue") <T extends WidgetBase> T addWidget(T widget);
    @SuppressWarnings("UnusedReturnValue") <T extends ButtonBase> T addButton(T button, IButtonActionListener listener);
    @SuppressWarnings("unused")
    GuiTextFieldGeneric createTextField(int x, int y, int width, int height);
    int getMaxTextFieldTextLength();
    @SuppressWarnings("unused")
    void addTextField(GuiTextFieldGeneric field, ConfigOptionChangeListenerTextField listener);
    void addExtraTextField(GuiTextFieldGeneric field, ConfigOptionChangeListenerTextField listener);
    ButtonGeneric createResetButton(int x, int y, IConfigResettable config);
    IKeybindConfigGui getKeybindHost();
    WidgetListConfigOptionsBase<?, ?> getWidgetListConfigOptionsBase();
    TextRenderer getTextRenderer();
}
