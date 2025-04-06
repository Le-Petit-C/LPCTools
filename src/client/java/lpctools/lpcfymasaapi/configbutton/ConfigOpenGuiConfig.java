package lpctools.lpcfymasaapi.configbutton;

import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.LPCConfigPage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConfigOpenGuiConfig extends HotkeyConfig{
    public ConfigOpenGuiConfig(@NotNull LPCConfigList list,@Nullable String defaultStorageString){
        super(list, "configOpenGui", defaultStorageString, new ConfigOpenGuiConfigInstance(list.getPage()));
    }
    private record ConfigOpenGuiConfigInstance(LPCConfigPage page) implements IHotkeyCallback{
        @Override public boolean onKeyAction(KeyAction action, IKeybind key) {
            page.showPage();
            return true;
        }
    }
}
