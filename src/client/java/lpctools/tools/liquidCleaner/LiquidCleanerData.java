package lpctools.tools.liquidCleaner;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.Waterloggable;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import static lpctools.util.BlockUtils.canAnyBucketPlaceAt;

public class LiquidCleanerData {
    public static final ImmutableList<BlockItem> defaultCleaningBlocks;
    @Nullable static LiquidCleanerRunner runner;
    static {
        ArrayList<BlockItem> _defaultCleaningBlocks = new ArrayList<>();
        Registries.BLOCK.forEach(block->{
            if(isDefaultCleaningBlock(block) && block.asItem() instanceof BlockItem blockItem)
                _defaultCleaningBlocks.add(blockItem);
        });
        defaultCleaningBlocks = ImmutableList.copyOf(_defaultCleaningBlocks);
    }
    private static boolean isDefaultCleaningBlock(Block block){
        if (block.getHardness() != 0) return false;
        BlockState state = block.getDefaultState();
        if (canAnyBucketPlaceAt(state)) return false;
        if (state.isAir()) return false;
        FluidState fluidState = state.getFluidState();
        if (fluidState.getLevel() != 0) return false;
        if (block instanceof Waterloggable) return false;
        //noinspection RedundantIfStatement
        if (block instanceof PlantBlock) return false;
        return true;
    }
}
