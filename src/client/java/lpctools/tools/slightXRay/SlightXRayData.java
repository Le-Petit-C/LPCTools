package lpctools.tools.slightXRay;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class SlightXRayData {
    static @Nullable RenderInstance renderInstance;
    public static final @NotNull ImmutableList<Block> defaultXRayBlocks = ImmutableList.of(
        Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE,
        Blocks.DEEPSLATE_COAL_ORE, Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE,
        Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN, Blocks.ENDER_CHEST, Blocks.REINFORCED_DEEPSLATE,
        Blocks.BUDDING_AMETHYST, Blocks.CALCITE,
        Blocks.ANCIENT_DEBRIS
    );
    public static final @NotNull HashMap<Block, MutableInt> XRayBlocks;
    static {
        XRayBlocks = new HashMap<>();
        for(Block block : defaultXRayBlocks)
            XRayBlocks.put(block, new MutableInt(0));
    }
}
