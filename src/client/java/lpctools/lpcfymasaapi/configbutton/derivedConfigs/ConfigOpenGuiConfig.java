package lpctools.lpcfymasaapi.configbutton.derivedConfigs;

import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import lpctools.lpcfymasaapi.LPCConfigPage;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.HotkeyConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConfigOpenGuiConfig extends HotkeyConfig {
    public ConfigOpenGuiConfig(@NotNull ILPCConfigReadable parent, @Nullable String defaultStorageString){
        super(parent, "configOpenGui", defaultStorageString, new ConfigOpenGuiConfigInstance(parent.getPage()));
    }
    private record ConfigOpenGuiConfigInstance(LPCConfigPage page) implements IHotkeyCallback{
        @Override public boolean onKeyAction(KeyAction action, IKeybind key) {
            page.showPage();
            return true;
        }
    }
}
