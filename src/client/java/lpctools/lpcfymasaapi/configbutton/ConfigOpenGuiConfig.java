package lpctools.lpcfymasaapi.configbutton;

import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import lpctools.lpcfymasaapi.LPCConfigList;

public class ConfigOpenGuiConfig extends HotkeyConfig implements IHotkeyCallback {
    public ConfigOpenGuiConfig(LPCConfigList list, String defaultStorageString){
        super(list, "configOpenGui", defaultStorageString);
        setCallBack(this);
    }
    @Override
    public boolean onKeyAction(KeyAction action, IKeybind key) {
        getPage().showPage();
        return true;
    }
}
