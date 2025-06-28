package lpctools.slightTools;

import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.HotkeyConfig;
import lpctools.mixin.client.BlockReplaceAction;
import net.minecraft.client.MinecraftClient;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;

public class SlightToolConfigs {
    public static HotkeyConfig blockReplaceHotkey;
    public static void init(){
        blockReplaceHotkey = addHotkeyConfig("blockReplaceHotkey", null, SlightToolConfigs::blockReplaceHotkeyCallback);
    }
    private static boolean blockReplaceHotkeyCallback(KeyAction keyAction, IKeybind iKeybind) {
        ((BlockReplaceAction)MinecraftClient.getInstance()).invokeDoAttack();
        ((BlockReplaceAction)MinecraftClient.getInstance()).invokeDoItemUse();
        return true;
    }
}
