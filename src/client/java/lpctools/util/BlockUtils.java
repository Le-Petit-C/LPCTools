package lpctools.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

@SuppressWarnings("unused")
public class BlockUtils {
    public static boolean isReplaceable(BlockState state){
        return state.canBeReplaced() || state.getBlock().equals(Blocks.SCULK_VEIN);
    }
    public static boolean isReplaceable(BlockPos pos){
        ClientLevel world = Minecraft.getInstance().level;
        if(world == null) return false;
        return isReplaceable(world.getBlockState(pos));
    }
    public static boolean isContainingLiquid(BlockState state){
        return state.getFluidState().getAmount() != 0;
    }
    public static boolean isReplaceableLiquid(BlockState state){
        return isReplaceable(state) && isContainingLiquid(state);
    }
    public static boolean isZeroHardBlock(BlockState state){
        return state.getBlock().defaultDestroyTime() == 0 || state.getBlock() == Blocks.KELP || state.getBlock() == Blocks.KELP_PLANT;
    }
    //检测能不能被水冲掉
    public static boolean canAnyBucketPlaceAt(BlockState state){
        if(state.getBlock() instanceof SimpleWaterloggedBlock) return false;
        for(Fluid fluid : BuiltInRegistries.FLUID)
            if (state.canBeReplaced(fluid))
                return true;
        return false;
    }
    public static boolean canAnyBucketPlaceAt(Block block){
        return canAnyBucketPlaceAt(block.defaultBlockState());
    }
    public static boolean canAnyBucketPlaceAt(BlockItem item){
        return canAnyBucketPlaceAt(item.getBlock());
    }
    public static boolean canAnyBucketPlaceAt(Item item){
        if(item instanceof BlockItem blockItem)
            return canAnyBucketPlaceAt(blockItem);
        return false;
    }
    //检测是不是流体，原版的话只有水或者岩浆
    public static boolean isFluid(Block block){
        for(Fluid fluid : BuiltInRegistries.FLUID)
            if (fluid != Fluids.EMPTY && fluid.defaultFluidState().createLegacyBlock().getBlock().equals(block))
                return true;
        return false;
    }
    public static boolean isFluid(BlockState state){
        return isFluid(state.getBlock());
    }
    public static boolean canBreakInstantly(LocalPlayer player, BlockPos pos){
        return player.level().getBlockState(pos).getDestroyProgress(player, player.level(), pos) >= 1.0F;
    }
}
