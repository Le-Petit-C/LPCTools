package lpctools.tools.liquidCleaner;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import static lpctools.util.BlockUtils.canAnyBucketPlaceAt;

public class LiquidCleanerData {
    public static final ImmutableList<BlockItem> defaultCleaningBlocks;
    @Nullable static LiquidCleanerRunner runner;
    static {
        ArrayList<BlockItem> _defaultCleaningBlocks = new ArrayList<>();
        BuiltInRegistries.BLOCK.forEach(block->{
            if(isDefaultCleaningBlock(block) && block.asItem() instanceof BlockItem blockItem)
                _defaultCleaningBlocks.add(blockItem);
        });
        defaultCleaningBlocks = ImmutableList.copyOf(_defaultCleaningBlocks);
    }
    private static boolean isDefaultCleaningBlock(Block block){
        if (block.defaultDestroyTime() != 0) return false;
        BlockState state = block.defaultBlockState();
        if (canAnyBucketPlaceAt(state)) return false;
        if (state.isAir()) return false;
        FluidState fluidState = state.getFluidState();
        if (fluidState.getAmount() != 0) return false;
        if (block instanceof SimpleWaterloggedBlock) return false;
        //noinspection RedundantIfStatement
        if (block instanceof VegetationBlock) return false;
        return true;
    }
}
