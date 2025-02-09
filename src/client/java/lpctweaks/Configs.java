package lpctweaks;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;

import java.util.List;

public class Configs implements IConfigHandler {

    private static final String CONFIG_FILE_NAME = Reference.MOD_ID + ".json";
    public static final ConfigHotkey OpenGuiConfigs = new ConfigHotkey("openGuiConfigs", "Z,C", KeybindSettings.PRESS_ALLOWEXTRA).apply("LPCTweaks.config");

    public static List<ConfigHotkey> HOTKEYS_OPTIONS = ImmutableList.of(
            OpenGuiConfigs
    );

    @Override
    public void load() {

    }

    @Override
    public void save() {

    }
}
