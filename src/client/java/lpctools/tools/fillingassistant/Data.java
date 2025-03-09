package lpctools.tools.fillingassistant;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Data {
    @NotNull public static final ImmutableList<Item> defaultPlaceableItemList = initDefaultPlaceableItemList();
    @NotNull public static ImmutableList<String> defaultPlaceableItemIdList = idListFromItemList(defaultPlaceableItemList);
    @NotNull public static ImmutableList<Block> defaultPassableBlockList = ImmutableList.of();
    @NotNull public static ImmutableList<String> defaultPassableBlockIdList = idListFromBlockList(defaultPassableBlockList);
    @NotNull public static ImmutableList<Block> defaultRequiredBlockWhiteList = initDefaultRequiredBlockWhiteList();
    @NotNull public static ImmutableList<String> defaultRequiredBlockIdList = idListFromBlockList(defaultRequiredBlockWhiteList);

    public static String getItemId(Item item){return Registries.ITEM.getId(item).toString();}
    public static String getBlockId(Block block){return Registries.BLOCK.getId(block).toString();}
    @NotNull private static ImmutableList<Item> initDefaultPlaceableItemList(){
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
    @NotNull private static ImmutableList<Block> initDefaultRequiredBlockWhiteList(){
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
    @NotNull private static ImmutableList<String> idListFromBlockList(@Nullable List<Block> list){
        ArrayList<String> ret = new ArrayList<>();
        if(list != null)
            for(Block block : list)
                ret.add(getBlockId(block));
        return ImmutableList.copyOf(ret);
    }
    @NotNull private static ImmutableList<String> idListFromItemList(@Nullable List<Item> list){
        ArrayList<String> ret = new ArrayList<>();
        if(list != null)
            for(Item item : list)
                ret.add(getItemId(item));
        return ImmutableList.copyOf(ret);
    }
}
