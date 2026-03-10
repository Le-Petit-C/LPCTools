package lpctools.tweaks;

import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.HotkeyConfig;
import lpctools.mixin.client.BlockReplaceAction;
import lpctools.util.DataUtils;
import lpctools.util.mixin.PacketRecorder;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.DefaultPermissions;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;

import java.util.Locale;

import static lpctools.generic.GenericConfigs.maxCommandLength;

public class BlockReplaceHotkey {
    public static final HotkeyConfig blockReplaceHotkey = new HotkeyConfig(TweakConfigs.tweaks, "blockReplaceHotkey", null, BlockReplaceHotkey::blockReplaceHotkeyCallback);
    public static final HotkeyConfig setBlockReplaceHotkey = new HotkeyConfig(TweakConfigs.tweaks, "setBlockReplaceHotkey", null, BlockReplaceHotkey::setBlockReplaceHotkeyCallbackEx);
    private static boolean shouldModifyClientTest = false;
    public static boolean shouldModifyClientTest() { return shouldModifyClientTest; }
    
    private static boolean normalReplacePair() {
        ((BlockReplaceAction) MinecraftClient.getInstance()).invokeDoAttack();
        ((BlockReplaceAction)MinecraftClient.getInstance()).invokeDoItemUse();
        return true;
    }
    private static boolean blockReplaceHotkeyCallback(KeyAction keyAction, IKeybind iKeybind) {
        if(blockReplaceHotkey.getKeybind().getKeys().equals(setBlockReplaceHotkey.getKeybind().getKeys()))
            return false;
        return normalReplacePair();
    }
    private static boolean setBlockReplaceHotkeyCallbackEx(KeyAction keyAction, IKeybind iKeybind){
        var client = MinecraftClient.getInstance();
        var player = client.player;
        if(player == null) return false;
        if(!player.isCreative()
            || !player.getPermissions().hasPermission(DefaultPermissions.GAMEMASTERS)
            || !(client.crosshairTarget instanceof BlockHitResult hitResult))
            return normalReplacePair();
        var world = player.getEntityWorld();
        var pos = hitResult.getBlockPos();
        try (var recorder = PacketRecorder.startInterceptedPackets()) {
            ((BlockReplaceAction) MinecraftClient.getInstance()).invokeDoAttack();
            shouldModifyClientTest = true;
            ((BlockReplaceAction)MinecraftClient.getInstance()).invokeDoItemUse();
            shouldModifyClientTest = false;
            BlockState state = world.getBlockState(pos);
            BlockEntity blockEntity = world.getBlockEntity(pos);
            StringBuilder blockData = new StringBuilder(BlockArgumentParser.stringifyBlockState(state));
            if (blockEntity != null) blockData.append(blockEntity.createNbt(player.getRegistryManager()));
            String command = String.format(Locale.ROOT, "setblock %d %d %d %s", pos.getX(), pos.getY(), pos.getZ(), blockData);
            if(command.length() > maxCommandLength.getIntegerValue()) {
                DataUtils.clientMessage(Text.translatable("lpctools.configs.tweaks.setBlockReplaceHotkey.commandTooLong", command.length(), maxCommandLength.getIntegerValue()), false);
                return true;
            }
            recorder.clear();
            player.networkHandler.sendChatCommand(command);
        }
        return true;
    }
}
