package lpctools.tweaks;

import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.HotkeyConfig;
import lpctools.mixin.client.BlockReplaceAction;
import net.minecraft.client.MinecraftClient;

public class BlockReplaceHotkey {
    public static final HotkeyConfig blockReplaceHotkey = new HotkeyConfig(TweakConfigs.tweaks, "blockReplaceHotkey", null, BlockReplaceHotkey::blockReplaceHotkeyCallback);
    private static boolean blockReplaceHotkeyCallback(KeyAction keyAction, IKeybind iKeybind) {
        ((BlockReplaceAction) MinecraftClient.getInstance()).invokeDoAttack();
        ((BlockReplaceAction)MinecraftClient.getInstance()).invokeDoItemUse();
        return true;
    }
}
