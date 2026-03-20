package lpctools.tweaks;

import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.HotkeyConfig;
import lpctools.mixin.client.BlockReplaceAction;
import lpctools.mixin.client.accessors.AbstractBlockAccessor;
import lpctools.util.DataUtils;
import lpctools.util.mixin.PacketRecorder;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.DefaultPermissions;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.*;

import static lpctools.generic.GenericConfigs.maxCommandLength;

public class VanillaBlockInteractionModifier {
    public static final HotkeyConfig blockReplaceHotkey = new HotkeyConfig(TweakConfigs.tweaks, "blockReplaceHotkey", null, VanillaBlockInteractionModifier::blockReplaceHotkeyCallback);
    public static final HotkeyConfig setBlockReplaceHotkey = new HotkeyConfig(TweakConfigs.tweaks, "setBlockReplaceHotkey", null, VanillaBlockInteractionModifier::setBlockReplaceHotkeyCallback);
    public static final HotkeyConfig quietBlockBreakHotkey = new HotkeyConfig(TweakConfigs.tweaks, "quietBlockBreakHotkey", null, VanillaBlockInteractionModifier::quietBlockBreakHotkeyCallback);
    private static boolean shouldModifyClientTest = false;
    public static boolean shouldModifyClientTest() { return shouldModifyClientTest; }
    
    private static boolean normalReplacePair() {
        ((BlockReplaceAction) MinecraftClient.getInstance()).invokeDoAttack();
        ((BlockReplaceAction)MinecraftClient.getInstance()).invokeDoItemUse();
        return true;
    }
    private static boolean normalAttack() {
        ((BlockReplaceAction) MinecraftClient.getInstance()).invokeDoAttack();
        return true;
    }
    private static boolean blockReplaceHotkeyCallback(KeyAction keyAction, IKeybind iKeybind) {
        if(blockReplaceHotkey.getKeybind().getKeys().equals(setBlockReplaceHotkey.getKeybind().getKeys()))
            return false;
        return normalReplacePair();
    }
    private static boolean setBlockReplaceHotkeyCallback(KeyAction keyAction, IKeybind iKeybind){
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
    
    private static boolean quietBlockBreakHotkeyCallback(KeyAction keyAction, IKeybind iKeybind) {
        var client = MinecraftClient.getInstance();
        var player = client.player;
        if(player == null) return false;
        if(!player.isCreative() || !player.getPermissions().hasPermission(DefaultPermissions.GAMEMASTERS)
            || !(client.crosshairTarget instanceof BlockHitResult hitResult))
            return normalAttack();
        LinkedHashMap<BlockPos, BlockState> brokenStates = buildBrokenBlockPoses(hitResult.getBlockPos(), player.getEntityWorld());
        while (!brokenStates.isEmpty()) {
            var entry = brokenStates.lastEntry();
            var pos = entry.getKey();
            var state = entry.getValue();
            boolean shouldCommand;
            // 一些特判，有空应该想办法把它改为自动的或可配置的，或许可以暂时设为TODO
            if(state.getBlock() instanceof PistonHeadBlock) shouldCommand = false;
            else if(state.getBlock() instanceof TallPlantBlock && state.get(TallPlantBlock.HALF) == DoubleBlockHalf.UPPER) shouldCommand = false;
            else if(state.getBlock() instanceof DoorBlock && state.get(DoorBlock.HALF) == DoubleBlockHalf.UPPER) shouldCommand = false;
            else shouldCommand = true;
            if(shouldCommand) {
                String command = String.format("setblock %d %d %d minecraft:air", pos.getX(), pos.getY(), pos.getZ());
                player.networkHandler.sendChatCommand(command);
            }
            var blockSoundGroup = state.getSoundGroup();
            player.getEntityWorld().playSoundAtBlockCenterClient(
                pos, blockSoundGroup.getBreakSound(), SoundCategory.BLOCKS, (blockSoundGroup.getVolume() + 1.0F) / 2.0F, blockSoundGroup.getPitch() * 0.8F, false
            );
            player.getEntityWorld().addBlockBreakParticles(pos, state);
            brokenStates.remove(pos);
        }
        player.swingHand(Hand.MAIN_HAND);
        return true;
    }
    
    private static void addBroken(World world, BlockPos pos, LinkedHashSet<BlockPos> posesToUpdate, LinkedHashMap<BlockPos, BlockState> brokenStates) {
        if(brokenStates.containsKey(pos)) return;
        var oldState = world.getBlockState(pos);
        if(oldState.isAir()) return;
        brokenStates.put(pos.toImmutable(), oldState);
        world.setBlockState(pos, Blocks.AIR.getDefaultState());
        for(var direction : Direction.values()) posesToUpdate.add(pos.offset(direction).toImmutable());
        if(oldState.getBlock() instanceof DoorBlock && oldState.get(DoorBlock.HALF) == DoubleBlockHalf.UPPER) addBroken(world, pos.down(), posesToUpdate, brokenStates);
        else if(oldState.getBlock() instanceof TallPlantBlock && oldState.get(TallPlantBlock.HALF) == DoubleBlockHalf.UPPER) addBroken(world, pos.down(), posesToUpdate, brokenStates);
        else if(oldState.getBlock() instanceof PistonHeadBlock) addBroken(world, pos.offset(oldState.get(PistonHeadBlock.FACING).getOpposite()), posesToUpdate, brokenStates);
    }
    
    private static LinkedHashMap<BlockPos, BlockState> buildBrokenBlockPoses(BlockPos startPos, World world) {
        LinkedHashSet<BlockPos> posesToUpdate = new LinkedHashSet<>();
        LinkedHashMap<BlockPos, BlockState> brokenStates = new LinkedHashMap<>();
        addBroken(world, startPos, posesToUpdate, brokenStates);
        while (!posesToUpdate.isEmpty()) {
            BlockPos pos = posesToUpdate.removeFirst();
            var state = world.getBlockState(pos);
            if(!((AbstractBlockAccessor)state.getBlock()).invokeCanPlaceAt(state, world, pos))
                addBroken(world, pos, posesToUpdate, brokenStates);
        }
        return brokenStates;
    }
}
