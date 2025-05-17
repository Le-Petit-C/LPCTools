package lpctools.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Waterloggable;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;

@SuppressWarnings("unused")
public class BlockUtils {
    public static boolean isReplaceable(BlockState state){
        return state.isReplaceable() || state.getBlock().equals(Blocks.SCULK_VEIN);
    }
    public static boolean isReplaceable(BlockPos pos){
        ClientWorld world = MinecraftClient.getInstance().world;
        if(world == null) return false;
        return isReplaceable(world.getBlockState(pos));
    }
    public static boolean isContainingLiquid(BlockState state){
        return state.getFluidState().getLevel() != 0;
    }
    public static boolean isReplaceableLiquid(BlockState state){
        return isReplaceable(state) && isContainingLiquid(state);
    }
    public static boolean isZeroHardBlock(BlockState state){
        return state.getBlock().getHardness() == 0 || state.getBlock() == Blocks.KELP || state.getBlock() == Blocks.KELP_PLANT;
    }
    public static boolean canBeReplacedByFluid(BlockState state){
        if(state.getBlock() instanceof Waterloggable) return false;
        for(Fluid fluid : Registries.FLUID)
            if (state.canBucketPlace(fluid))
                return true;
        return false;
    }
    public static boolean canBeReplacedByFluid(Block block){
        return canBeReplacedByFluid(block.getDefaultState());
    }
    public static boolean canBeReplacedByFluid(BlockItem item){
        return canBeReplacedByFluid(item.getBlock());
    }
    public static boolean canBeReplacedByFluid(Item item){
        if(item instanceof BlockItem blockItem)
            return canBeReplacedByFluid(blockItem);
        return false;
    }
}
