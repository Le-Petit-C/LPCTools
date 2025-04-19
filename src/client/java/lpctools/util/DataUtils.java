package lpctools.util;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DataUtils {
    public static String getItemId(Item item){return Registries.ITEM.getEntry(item).getIdAsString();}
    public static String getBlockId(Block block){return Registries.BLOCK.getEntry(block).getIdAsString();}
    @NotNull public static ImmutableList<String> idListFromBlockList(@Nullable List<Block> list){
        ArrayList<String> ret = new ArrayList<>();
        if(list != null)
            for(Block block : list)
                ret.add(getBlockId(block));
        return ImmutableList.copyOf(ret);
    }
    @NotNull public static ImmutableList<String> idListFromItemList(@Nullable List<Item> list){
        ArrayList<String> ret = new ArrayList<>();
        if(list != null)
            for(Item item : list)
                ret.add(getItemId(item));
        return ImmutableList.copyOf(ret);
    }
}
