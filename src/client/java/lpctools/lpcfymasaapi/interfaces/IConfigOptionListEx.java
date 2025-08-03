package lpctools.lpcfymasaapi.interfaces;

import fi.dy.masa.malilib.config.IConfigOptionList;
import fi.dy.masa.malilib.gui.button.ButtonBase;

public interface IConfigOptionListEx extends IConfigOptionList, ILPCValueChangeCallback {
    default void cycle(boolean forward){
        setOptionListValue(getOptionListValue().cycle(forward));
    }
    default void cycleByMouseButton(ButtonBase ignoredButton, int mouseButton){
        switch (mouseButton){
            case 0 -> cycle(true);
            case 1 -> cycle(false);
            default -> {}
        }
    }
}
