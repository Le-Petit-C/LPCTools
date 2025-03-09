package lpctools.tools.liquidcleaner;

import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import lpctools.lpcfymasaapi.configbutton.HotkeyConfig;
import lpctools.lpcfymasaapi.configbutton.ThirdListConfig;

public class LiquidCleaner {
    public static void init(ThirdListConfig FAConfig){
        hotkeyConfig = FAConfig.addHotkeyConfig("FA_Hotkey", "", new HotkeyCallback());
    }

    static HotkeyConfig hotkeyConfig;

    private static class HotkeyCallback implements IHotkeyCallback{
        @Override
        public boolean onKeyAction(KeyAction action, IKeybind key) {
            return false;
        }
    }
}
