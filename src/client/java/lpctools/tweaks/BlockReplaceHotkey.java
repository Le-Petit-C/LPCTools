package lpctools.tweaks;

import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.HotkeyConfig;
import lpctools.mixin.client.BlockReplaceAction;
import lpctools.mixin.client.accessors.BlockItemAccessor;
import lpctools.util.DataUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.DefaultPermissions;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;

import java.util.Locale;

import static lpctools.generic.GenericConfigs.maxCommandLength;

public class BlockReplaceHotkey {
    public static final HotkeyConfig blockReplaceHotkey = new HotkeyConfig(TweakConfigs.tweaks, "blockReplaceHotkey", null, BlockReplaceHotkey::blockReplaceHotkeyCallback);
    public static final HotkeyConfig setBlockReplaceHotkey = new HotkeyConfig(TweakConfigs.tweaks, "setBlockReplaceHotkey", null, BlockReplaceHotkey::setBlockReplaceHotkeyCallback);
    
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
    private static boolean setBlockReplaceHotkeyCallback(KeyAction keyAction, IKeybind iKeybind) {
        var client = MinecraftClient.getInstance();
        var player = client.player;
        if(player == null) return false;
        if(!player.getPermissions().hasPermission(DefaultPermissions.GAMEMASTERS)) return normalReplacePair();
        if(!player.isInCreativeMode()) return normalReplacePair();
        if(!(MinecraftClient.getInstance().crosshairTarget instanceof BlockHitResult blockHitResult)) return normalReplacePair();
        for(Hand hand : Hand.values()){
            ItemStack itemStack = player.getStackInHand(hand);
            BlockItem blockItem;
            if(itemStack.getItem() instanceof BlockItem _blockItem) blockItem = _blockItem;
            else continue;
            BlockItemAccessor accessor = (BlockItemAccessor)blockItem;
            var world = player.getEntityWorld();
            var pos = blockHitResult.getBlockPos();
            BlockState oldState = world.getBlockState(pos);
            world.setBlockState(pos, Blocks.AIR.getDefaultState());
            ItemPlacementContext context = new ItemPlacementContext(player, hand, itemStack, blockHitResult);
            BlockState rawState = accessor.invokeGetPlacementState(context);
            if(rawState == null) {
                world.setBlockState(pos, oldState);
                return normalReplacePair();
            }
            BlockState state = accessor.invokePlaceFromNbt(pos, world, itemStack, rawState);
            world.setBlockState(pos, state);
            StringBuilder blockData = new StringBuilder(BlockArgumentParser.stringifyBlockState(state));
            BlockEntity blockEntity = world.getBlockEntity(pos);
			if (blockEntity != null) {
                blockEntity.readComponents(itemStack);
                blockData.append(blockEntity.createNbt(player.getRegistryManager()));
            }
            String command = String.format(Locale.ROOT, "setblock %d %d %d %s", pos.getX(), pos.getY(), pos.getZ(), blockData);
            // DataUtils.clientMessage(command, false);
            if(command.length() > maxCommandLength.getIntegerValue()) {
                DataUtils.clientMessage(Text.translatable("lpctools.configs.tweaks.setBlockReplaceHotkey.commandTooLong", command.length(), maxCommandLength.getIntegerValue()), false);
                normalReplacePair();
            }
            else player.networkHandler.sendChatCommand(command);
            player.swingHand(hand);
            BlockSoundGroup blockSoundGroup = state.getSoundGroup();
            BlockSoundGroup oldBlockSoundGroup = oldState.getSoundGroup();
            world.playSoundAtBlockCenterClient(pos, oldBlockSoundGroup.getBreakSound(), SoundCategory.BLOCKS,
                (blockSoundGroup.getVolume() + 1.0F) / 2.0F, blockSoundGroup.getPitch() * 0.8F, false);
            world.addBlockBreakParticles(pos, oldState);
            world.playSound(player, pos, accessor.invokeGetPlaceSound(state), SoundCategory.BLOCKS,
                (blockSoundGroup.getVolume() + 1.0F) / 2.0F, blockSoundGroup.getPitch() * 0.8F);
            return true;
        }
        return normalReplacePair();
    }
}
