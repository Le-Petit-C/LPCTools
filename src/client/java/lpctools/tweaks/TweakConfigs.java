package lpctools.tweaks;

import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.LPCConfigPage;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.HotkeyConfig;
import lpctools.mixin.client.BlockReplaceAction;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.NotNull;

public class TweakConfigs {
    public static LPCConfigList tweaks;
    public static HotkeyConfig blockReplaceHotkey;
    public static void init(@NotNull LPCConfigPage page){
        tweaks = page.addList("tweaks");
        blockReplaceHotkey = tweaks.addHotkeyConfig("blockReplaceHotkey", null, TweakConfigs::blockReplaceHotkeyCallback);
    }
    private static boolean blockReplaceHotkeyCallback(KeyAction keyAction, IKeybind iKeybind) {
        ((BlockReplaceAction)MinecraftClient.getInstance()).invokeDoAttack();
        ((BlockReplaceAction)MinecraftClient.getInstance()).invokeDoItemUse();
        return true;
    }
}
