package lpctools.tools.antiSpawner;

import com.google.common.collect.ImmutableList;
import lpctools.util.HandRestock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;

import java.util.ArrayList;

import static lpctools.tools.antiSpawner.AntiSpawner.*;
import static lpctools.util.BlockUtils.canBeReplacedByFluid;
import static lpctools.util.DataUtils.getBlockId;

public class AntiSpawnerData {
    public static final HandRestock.IRestockTest restockTest = item -> item.getItem() instanceof BlockItem blockItem && placeableItems.set.contains(blockItem);
    public static final ImmutableList<BlockItem> defaultPlaceableItems;
    public static final AntiSpawnerRunner runner = new AntiSpawnerRunner();
    static {
        ArrayList<BlockItem> placeableItems = new ArrayList<>();
        for(Block block : Registries.BLOCK){
            BlockItem item;
            try{item = (BlockItem) block.asItem();}
            catch (Exception ignored){continue;}
            if(canBeReplacedByFluid(block)) continue;
            if(block.getDefaultState().isBurnable()) continue;
            String idPath = getBlockId(block);
            if(idPath.contains("rail")) placeableItems.add(item);
            else if(idPath.contains("slab")) placeableItems.add(item);
        }
        defaultPlaceableItems = ImmutableList.copyOf(placeableItems);
    }
}
