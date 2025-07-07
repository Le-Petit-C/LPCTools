package lpctools.lpcfymasaapi.interfaces;

import fi.dy.masa.malilib.config.IConfigResettable;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;

public interface ButtonConsumer {
    <T extends ButtonBase> T addButton(T button, IButtonActionListener listener);
    @SuppressWarnings("UnusedReturnValue")
    default ButtonGeneric addButton(int x, int y, int w, boolean rightAlign, String translationKey, IButtonActionListener listener){
        return addButton(new ButtonGeneric(x, y, w, rightAlign, translationKey), listener);
    }
    ButtonGeneric createResetButton(int x, int y, IConfigResettable config);
}
