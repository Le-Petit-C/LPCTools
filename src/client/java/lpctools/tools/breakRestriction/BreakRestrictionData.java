package lpctools.tools.breakRestriction;

import com.google.common.collect.ImmutableList;
import lpctools.compact.derived.ShapeList;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BreakRestrictionData {
    public static @NotNull ShapeList shapeList = BreakRestriction.rangeLimit.buildShapeList();
    public static final ImmutableList<Block> defaultBlockWhitelist;
    public static final ImmutableList<Block> defaultBlockBlacklist;
    static{
        defaultBlockWhitelist = ImmutableList.copyOf(List.of(
            Blocks.AMETHYST_BLOCK,
            Blocks.SMALL_AMETHYST_BUD,
            Blocks.MEDIUM_AMETHYST_BUD,
            Blocks.LARGE_AMETHYST_BUD,
            Blocks.AMETHYST_CLUSTER,
            Blocks.CALCITE
        ));
        defaultBlockBlacklist = ImmutableList.copyOf(List.of(
            Blocks.BUDDING_AMETHYST
        ));
    }
}
