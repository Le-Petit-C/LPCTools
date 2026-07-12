package lpctools.tools.antiSpawner;

import com.google.common.collect.ImmutableList;
import lpctools.util.BlockUtils;
import lpctools.util.HandRestock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import java.util.ArrayList;

import static lpctools.tools.antiSpawner.AntiSpawner.*;
import static lpctools.util.DataUtils.getBlockId;

public class AntiSpawnerData {
    public static final HandRestock.IRestockTest restockTest = item -> item.getItem() instanceof BlockItem blockItem && placeableItems.contains(blockItem);
    public static final ImmutableList<BlockItem> defaultPlaceableItems;
    public static final AntiSpawnerRunner runner = new AntiSpawnerRunner();
    static {
        ArrayList<BlockItem> placeableItems = new ArrayList<>();
        for(Block block : BuiltInRegistries.BLOCK){
            BlockItem item;
            try{item = (BlockItem) block.asItem();}
            catch (Exception ignored){continue;}
            if(BlockUtils.canAnyBucketPlaceAt(block)) continue;
            if(block.defaultBlockState().ignitedByLava()) continue;
            String idPath = getBlockId(block);
            if(idPath.contains("rail")) placeableItems.add(item);
            else if(idPath.contains("slab")) placeableItems.add(item);
        }
        defaultPlaceableItems = ImmutableList.copyOf(placeableItems);
    }
}
