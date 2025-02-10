package lpctools.lpcfymasaapi.configbutton;

import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import lpctools.lpcfymasaapi.LPCConfigPage;

public class ConfigOpenGuiConfig extends HotkeyConfig implements IHotkeyCallback {
    public ConfigOpenGuiConfig(LPCConfigPage page, String defaultStorageString){
        super(page, "configOpenGui", defaultStorageString, "generic");
        setCallBack(this);
    }
    @Override
    public boolean onKeyAction(KeyAction action, IKeybind key)
    {
        GuiBase.openGui(getPage().newPage());
        return true;
    }
}
