package lpctools.tweaks;

import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.BooleanConfig;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.HotkeyConfig;
import lpctools.mixin.client.BlockReplaceAction;
import lpctools.mixin.client.accessors.AbstractBlockAccessor;
import lpctools.util.DataUtils;
import lpctools.util.mixin.PacketRecorder;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import java.util.*;

import static lpctools.generic.GenericConfigs.maxCommandLength;

public class VanillaBlockInteractionModifier {
    public static final HotkeyConfig blockReplaceHotkey = new HotkeyConfig(TweakConfigs.tweaks, "blockReplaceHotkey", null, VanillaBlockInteractionModifier::blockReplaceHotkeyCallback);
    public static final HotkeyConfig setBlockReplaceHotkey = new HotkeyConfig(TweakConfigs.tweaks, "setBlockReplaceHotkey", null, VanillaBlockInteractionModifier::setBlockReplaceHotkeyCallback);
    public static final HotkeyConfig quietBlockBreakHotkey = new HotkeyConfig(TweakConfigs.tweaks, "quietBlockBreakHotkey", null, VanillaBlockInteractionModifier::quietBlockBreakHotkeyCallback);
    public static final BooleanConfig useQSetBlock = new BooleanConfig(TweakConfigs.tweaks, "useQSetBlock", false);
    private static boolean shouldModifyClientTest = false;
    public static boolean shouldModifyClientTest() { return shouldModifyClientTest; }
    
    private static String setBlockPrefix() {
        return useQSetBlock.getAsBoolean() ? "qsetblock" : "setblock";
    }
    
    private static boolean normalReplacePair() {
        ((BlockReplaceAction) Minecraft.getInstance()).invokeDoAttack();
        ((BlockReplaceAction)Minecraft.getInstance()).invokeDoItemUse();
        return true;
    }
    private static boolean normalAttack() {
        ((BlockReplaceAction) Minecraft.getInstance()).invokeDoAttack();
        return true;
    }
    private static boolean blockReplaceHotkeyCallback(KeyAction keyAction, IKeybind iKeybind) {
        if(blockReplaceHotkey.getKeybind().getKeys().equals(setBlockReplaceHotkey.getKeybind().getKeys()))
            return false;
        return normalReplacePair();
    }
    private static boolean setBlockReplaceHotkeyCallback(KeyAction keyAction, IKeybind iKeybind){
        var client = Minecraft.getInstance();
        var player = client.player;
        if(player == null) return false;
        if(!player.isCreative()
            || !player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)
            || !(client.hitResult instanceof BlockHitResult hitResult))
            return normalReplacePair();
        var world = player.level();
        var pos = hitResult.getBlockPos();
        try (var recorder = PacketRecorder.startInterceptedPackets()) {
            ((BlockReplaceAction) Minecraft.getInstance()).invokeDoAttack();
            shouldModifyClientTest = true;
            ((BlockReplaceAction)Minecraft.getInstance()).invokeDoItemUse();
            shouldModifyClientTest = false;
            BlockState state = world.getBlockState(pos);
            BlockEntity blockEntity = world.getBlockEntity(pos);
            StringBuilder blockData = new StringBuilder(BlockStateParser.serialize(state));
            if (blockEntity != null) blockData.append(blockEntity.saveWithoutMetadata(player.registryAccess()));
            String command = String.format(Locale.ROOT, setBlockPrefix() + " %d %d %d %s", pos.getX(), pos.getY(), pos.getZ(), blockData);
            if(command.length() > maxCommandLength.getIntegerValue()) {
                DataUtils.clientMessage(Component.translatable("lpctools.configs.tweaks.setBlockReplaceHotkey.commandTooLong", command.length(), maxCommandLength.getIntegerValue()), false);
                return true;
            }
            recorder.clear();
            player.connection.sendCommand(command);
        }
        return true;
    }
    
    private static boolean quietBlockBreakHotkeyCallback(KeyAction keyAction, IKeybind iKeybind) {
        var client = Minecraft.getInstance();
        var player = client.player;
        if(player == null) return false;
        if(!player.isCreative() || !player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)
            || !(client.hitResult instanceof BlockHitResult hitResult))
            return normalAttack();
        LinkedHashMap<BlockPos, BlockState> brokenStates = buildBrokenBlockPoses(hitResult.getBlockPos(), player.level());
        while (!brokenStates.isEmpty()) {
            var entry = brokenStates.lastEntry();
            var pos = entry.getKey();
            var state = entry.getValue();
            boolean shouldCommand;
            // 一些特判，有空应该想办法把它改为自动的或可配置的，或许可以暂时设为TODO
            if(state.getBlock() instanceof PistonHeadBlock) shouldCommand = false;
            else if(state.getBlock() instanceof DoublePlantBlock && state.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER) shouldCommand = false;
            else if(state.getBlock() instanceof DoorBlock && state.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER) shouldCommand = false;
            else shouldCommand = true;
            if(shouldCommand) {
                String command = String.format(setBlockPrefix() + " %d %d %d minecraft:air", pos.getX(), pos.getY(), pos.getZ());
                player.connection.sendCommand(command);
            }
            var blockSoundGroup = state.getSoundType();
            player.level().playLocalSound(
                pos, blockSoundGroup.getBreakSound(), SoundSource.BLOCKS, (blockSoundGroup.getVolume() + 1.0F) / 2.0F, blockSoundGroup.getPitch() * 0.8F, false
            );
            player.level().addDestroyBlockEffect(pos, state);
            brokenStates.remove(pos);
        }
        player.swing(InteractionHand.MAIN_HAND);
        return true;
    }
    
    private static void addBroken(Level world, BlockPos pos, LinkedHashSet<BlockPos> posesToUpdate, LinkedHashMap<BlockPos, BlockState> brokenStates) {
        if(brokenStates.containsKey(pos)) return;
        var oldState = world.getBlockState(pos);
        if(oldState.isAir()) return;
        brokenStates.put(pos.immutable(), oldState);
        world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        for(var direction : Direction.values()) posesToUpdate.add(pos.relative(direction).immutable());
        if(oldState.getBlock() instanceof DoorBlock && oldState.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER) addBroken(world, pos.below(), posesToUpdate, brokenStates);
        else if(oldState.getBlock() instanceof DoublePlantBlock && oldState.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER) addBroken(world, pos.below(), posesToUpdate, brokenStates);
        else if(oldState.getBlock() instanceof PistonHeadBlock) addBroken(world, pos.relative(oldState.getValue(PistonHeadBlock.FACING).getOpposite()), posesToUpdate, brokenStates);
    }
    
    private static LinkedHashMap<BlockPos, BlockState> buildBrokenBlockPoses(BlockPos startPos, Level world) {
        LinkedHashSet<BlockPos> posesToUpdate = new LinkedHashSet<>();
        LinkedHashMap<BlockPos, BlockState> brokenStates = new LinkedHashMap<>();
        addBroken(world, startPos, posesToUpdate, brokenStates);
        while (!posesToUpdate.isEmpty()) {
            BlockPos pos = posesToUpdate.removeFirst();
            var state = world.getBlockState(pos);
            if(!((AbstractBlockAccessor)state.getBlock()).invokeCanSurvive(state, world, pos))
                addBroken(world, pos, posesToUpdate, brokenStates);
        }
        return brokenStates;
    }
}
