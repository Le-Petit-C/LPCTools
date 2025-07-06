package lpctools.tools.fillingAssistant;

import com.google.common.collect.ImmutableList;
import lpctools.LPCTools;
import lpctools.util.DataUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static lpctools.tools.fillingAssistant.FillingAssistant.*;

public class FillingAssistantData {
    public static final @NotNull ImmutableList<BlockItem> defaultPlaceableItemList = DataUtils.blockItemListFromItemList(initDefaultPlaceableItemList(), LPCTools.LOGGER);
    public static final @NotNull ImmutableList<Block> defaultPassableBlockList = ImmutableList.of();
    public static final @NotNull ImmutableList<Block> defaultRequiredBlockWhiteList = initDefaultRequiredBlockWhiteList();
    public static final Map<String, OuterRangeBlockMethod> outerRangeBlockMethods = Map.of(
        "lpctools.configs.tools.outerRangeBlockMethods.asUnpassable", block -> true,
        "lpctools.configs.tools.outerRangeBlockMethods.asPassable", block -> false,
        "lpctools.configs.tools.outerRangeBlockMethods.asOrigin", FillingAssistant::isBlockUnpassable
    );
    private static @NotNull ImmutableList<Item> initDefaultPlaceableItemList(){
        return ImmutableList.of(
                Items.STONE,
                Items.COBBLESTONE,
                Items.DEEPSLATE,
                Items.COBBLED_DEEPSLATE,
                Items.TUFF,
                Items.DIORITE,
                Items.GRANITE,
                Items.ANDESITE,
                Items.DIRT,
                Items.COARSE_DIRT,
                Items.ROOTED_DIRT,
                Items.GRASS_BLOCK,
                Items.MYCELIUM,
                Items.PODZOL,
                Items.CLAY,
                Items.MOSS_BLOCK,
                Items.SANDSTONE,
                Items.NETHERRACK,
                Items.MAGMA_BLOCK,
                Items.BASALT,
                Items.SMOOTH_BASALT,
                Items.BLACKSTONE
        );
    }
    private static @NotNull ImmutableList<Block> initDefaultRequiredBlockWhiteList(){
        return ImmutableList.of(
                Blocks.COAL_ORE,
                Blocks.DEEPSLATE_COAL_ORE,
                Blocks.IRON_ORE,
                Blocks.DEEPSLATE_IRON_ORE,
                Blocks.COPPER_ORE,
                Blocks.DEEPSLATE_COPPER_ORE,
                Blocks.GOLD_ORE,
                Blocks.DEEPSLATE_GOLD_ORE,
                Blocks.REDSTONE_ORE,
                Blocks.DEEPSLATE_REDSTONE_ORE,
                Blocks.EMERALD_ORE,
                Blocks.DEEPSLATE_EMERALD_ORE,
                Blocks.LAPIS_ORE,
                Blocks.DEEPSLATE_LAPIS_ORE,
                Blocks.DIAMOND_ORE,
                Blocks.DEEPSLATE_DIAMOND_ORE,
                Blocks.NETHER_GOLD_ORE,
                Blocks.NETHER_QUARTZ_ORE,
                Blocks.ANCIENT_DEBRIS,
                Blocks.BUDDING_AMETHYST
        );
    }
}
