package lpctools.generic;

import com.google.common.collect.ImmutableList;
import lpctools.util.javaex.PriorityThreadPoolExecutor;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

public class GenericData {
    public static final ImmutableList<Block> defaultExtraSpawnBlocks = ImmutableList.of();
    public static final ImmutableList<Block> defaultExtraNoSpawnBlocks = ImmutableList.of(
        Blocks.BEDROCK
    );
    static PriorityThreadPoolExecutor threadPool;
}
