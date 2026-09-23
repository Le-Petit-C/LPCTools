package lpctools.tweaks;

import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import lpctools.generic.GenericUtils;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.BooleanConfig;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.HotkeyConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.BooleanThirdListConfig;
import lpctools.mixin.client.accessors.AbstractBlockAccessor;
import lpctools.mixin.client.accessors.MinecraftAccessor;
import lpctools.util.DataUtils;
import lpctools.util.inGame.InGameManager;
import lpctools.util.mixin.PacketRecorder;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
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
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static lpctools.generic.GenericConfigs.maxCommandLength;
import static lpctools.lpcfymasaapi.LPCConfigStatics.*;

public class BlockInteractionEnhancements {
    public static final BooleanThirdListConfig blockInteractionEnhancements = new BooleanThirdListConfig(TweakConfigs.tweaks, "blockInteractionEnhancements", false, null);
    static { listStack.push(blockInteractionEnhancements); }
    public static final HotkeyConfig blockReplaceHotkey = addHotkeyConfig("blockReplaceHotkey", null, BlockInteractionEnhancements::blockReplaceHotkeyCallback);
    public static final HotkeyConfig setBlockReplaceHotkey = addHotkeyConfig("setBlockReplaceHotkey", null, BlockInteractionEnhancements::setBlockReplaceHotkeyCallback);
    public static final HotkeyConfig quietBlockBreakHotkey = addHotkeyConfig("quietBlockBreakHotkey", null, BlockInteractionEnhancements::quietBlockBreakHotkeyCallback);
    public static final BooleanConfig preferQSetBlock = addBooleanConfig("preferQSetBlock", false);
    static { listStack.pop(); }
    private static boolean shouldModifyClientTest = false;
    public static boolean shouldModifyClientTest() { return shouldModifyClientTest; }
    /** 总开关：关闭时本组的三个热键一律什么都不做 */
    private static boolean enabled() { return blockInteractionEnhancements.getBooleanValue(); }

    private static boolean replaceKeysEquals() { return Objects.equals(blockReplaceHotkey.getKeybind().getKeys(), setBlockReplaceHotkey.getKeybind().getKeys()); }
    private static void warnOperationFailed() { DataUtils.clientMessage(Component.translatable("lpctools.configs.tweaks.operationFailed"), true); }

    private static @Nullable String setBlockPrefix(InGameManager manager) {
        if(preferQSetBlock.getBooleanValue() && GenericUtils.hasCommand(manager, "qsetblock", true)) return "qsetblock";
        else if(GenericUtils.hasCommand(manager, "setblock", true)) return "setblock";
        else return null;
    }

    private static boolean normalReplacePair() {
        ((MinecraftAccessor) Minecraft.getInstance()).invokeStartAttack();
        ((MinecraftAccessor)Minecraft.getInstance()).invokeStartUseItem();
        return true;
    }

    private static boolean blockReplaceHotkeyCallback(KeyAction keyAction, IKeybind iKeybind) {
        if(!enabled()) return false;
        if(replaceKeysEquals()) return false;
        else return normalReplacePair();
    }
    private static boolean setBlockReplaceHotkeyCallback(KeyAction keyAction, IKeybind iKeybind){
        if(!enabled()) return false;
        InGameManager manager = InGameManager.get();
        var client = Minecraft.getInstance();
		if (manager != null && client.hitResult instanceof BlockHitResult hitResult) {
            String commandPrefix = setBlockPrefix(manager);
            if (commandPrefix != null) {
                var pos = hitResult.getBlockPos();
                try (var recorder = PacketRecorder.startInterceptedPackets()) {
                    ((MinecraftAccessor) Minecraft.getInstance()).invokeStartAttack();
                    shouldModifyClientTest = true;
                    ((MinecraftAccessor) Minecraft.getInstance()).invokeStartUseItem();
                    shouldModifyClientTest = false;
                    BlockState state = manager.getBlockState(pos);
                    BlockEntity blockEntity = manager.getBlockEntity(pos);
                    StringBuilder blockData = new StringBuilder(BlockStateParser.serialize(state));
                    if (blockEntity != null)
                        blockData.append(blockEntity.saveWithoutMetadata(manager.player.registryAccess()));
                    String command = String.format(Locale.ROOT, commandPrefix + " %d %d %d %s", pos.getX(), pos.getY(), pos.getZ(), blockData);
					recorder.clear();
                    if (command.length() > maxCommandLength.getIntegerValue())
						DataUtils.clientMessage(Component.translatable("lpctools.configs.tweaks.blockInteractionEnhancements.setBlockReplaceHotkey.commandTooLong", command.length(), maxCommandLength.getIntegerValue()), false);
					else manager.sendCommand(command);
                }
                return true;
            }
        }
        if(replaceKeysEquals()) return normalReplacePair();
        else {
            warnOperationFailed();
            return true;
        }
	}
    
    private static boolean quietBlockBreakHotkeyCallback(KeyAction keyAction, IKeybind iKeybind) {
        if(!enabled()) return false;
        var client = Minecraft.getInstance();
        InGameManager manager = InGameManager.get();
		if (manager != null && client.hitResult instanceof BlockHitResult hitResult) {
			String commandPrefix = setBlockPrefix(manager);
			if (commandPrefix != null) {
				LinkedHashMap<BlockPos, BlockState> brokenStates = buildBrokenBlockPoses(hitResult.getBlockPos(), manager.level);
				while (!brokenStates.isEmpty()) {
					var entry = brokenStates.lastEntry();
					var pos = entry.getKey();
					var state = entry.getValue();
					boolean shouldCommand;
					// 一些特判，有空应该想办法把它改为自动的或可配置的，或许可以暂时设为TODO
					if (state.getBlock() instanceof PistonHeadBlock) shouldCommand = false;
					else if (state.getBlock() instanceof DoublePlantBlock && state.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER)
						shouldCommand = false;
					else if (state.getBlock() instanceof DoorBlock && state.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER)
						shouldCommand = false;
					else shouldCommand = true;
					if (shouldCommand) {
						String command = String.format(commandPrefix + " %d %d %d minecraft:air", pos.getX(), pos.getY(), pos.getZ());
						manager.sendCommand(command);
					}
					var blockSoundGroup = state.getSoundType();
					manager.playLocalSound(pos, blockSoundGroup.getBreakSound(), SoundSource.BLOCKS, (blockSoundGroup.getVolume() + 1.0F) / 2.0F, blockSoundGroup.getPitch() * 0.8F, false);
					manager.addDestroyBlockEffect(pos, state);
					brokenStates.remove(pos);
				}
				manager.swing(InteractionHand.MAIN_HAND);
				return true;
			}
		}
        warnOperationFailed();
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
