package lpctools.lpcfymasaapi.configbutton;

import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.LPCConfigPage;

public class ConfigOpenGuiConfig extends HotkeyConfig{
    public ConfigOpenGuiConfig(LPCConfigList list, String defaultStorageString){
        super(list, "configOpenGui", defaultStorageString, new ConfigOpenGuiConfigInstance(list.getPage()));
    }
    private record ConfigOpenGuiConfigInstance(LPCConfigPage page) implements IHotkeyCallback{
        @Override public boolean onKeyAction(KeyAction action, IKeybind key) {
            page.showPage();
            return true;
        }
    }
}
