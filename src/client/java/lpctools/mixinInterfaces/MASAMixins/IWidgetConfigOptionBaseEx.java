package lpctools.mixinInterfaces.MASAMixins;

import fi.dy.masa.malilib.config.gui.ConfigOptionChangeListenerTextField;
import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;

public interface IWidgetConfigOptionBaseEx {
    void lPCTools$addExtraTextField(GuiTextFieldGeneric field, ConfigOptionChangeListenerTextField listener);
}
