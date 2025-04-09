package lpctools.lpcfymasaapi.configbutton;

import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import lpctools.lpcfymasaapi.LPCConfigPage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConfigOpenGuiConfig extends HotkeyConfig{
    public ConfigOpenGuiConfig(@NotNull ILPCConfigList defaultParent,@Nullable String defaultStorageString){
        super(defaultParent, "configOpenGui", defaultStorageString, new ConfigOpenGuiConfigInstance(defaultParent.getPage()));
    }
    private record ConfigOpenGuiConfigInstance(LPCConfigPage page) implements IHotkeyCallback{
        @Override public boolean onKeyAction(KeyAction action, IKeybind key) {
            page.showPage();
            return true;
        }
    }
}
