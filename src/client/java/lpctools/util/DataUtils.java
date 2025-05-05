package lpctools.util;

import com.google.common.collect.ImmutableList;
import lpctools.lpcfymasaapi.LPCAPIInit;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;

public class DataUtils {
    public static void notifyPlayer(String message, boolean overlay){
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if(player != null) player.sendMessage(Text.of(message), overlay);
    }
    public static String getItemId(Item item){return Registries.ITEM.getEntry(item).getIdAsString();}
    public static String getBlockId(Block block){return Registries.BLOCK.getEntry(block).getIdAsString();}
    public static @NotNull ImmutableList<String> idListFromBlockList(@Nullable Iterable<Block> list){
        ArrayList<String> ret = new ArrayList<>();
        if(list != null)
            for(Block block : list)
                ret.add(getBlockId(block));
        return ImmutableList.copyOf(ret);
    }
    public static @NotNull ImmutableList<String> idListFromItemList(@Nullable Iterable<Item> list){
        ArrayList<String> ret = new ArrayList<>();
        if(list != null)
            for(Item item : list)
                ret.add(getItemId(item));
        return ImmutableList.copyOf(ret);
    }
    public static @Nullable Block getBlockFromId(@NotNull String id, boolean notifies){
        try{
            return Registries.BLOCK.get(Identifier.of(id));
        }catch (Exception e){
            if(notifies){
                notifyPlayer(String.format("§egetBlockFromId(%s) failed.", id), false);
                LPCAPIInit.LOGGER.warn("getBlockFromId(\"{}\"): {}", id, e.getMessage());
            }
            return null;
        }
    }
    public static @Nullable Item getItemFromId(@NotNull String id, boolean notifies){
        try{
            return Registries.ITEM.get(Identifier.of(id));
        }catch (Exception e){
            if(notifies){
                notifyPlayer(String.format("§egetItemFromId(%s) failed.", id), false);
                LPCAPIInit.LOGGER.warn("getItemFromId(\"{}\"): {}", id, e.getMessage());
            }
            return null;
        }
    }
    public static @NotNull HashSet<@NotNull Block> blockSetFromIds(Iterable<String> ids){
        HashSet<Block> ret = new HashSet<>();
        for(String id : ids){
            Block block = getBlockFromId(id, true);
            if(block != null) ret.add(block);
        }
        return ret;
    }
    public static @NotNull HashSet<@NotNull Item> itemSetFromIds(Iterable<String> ids){
        HashSet<Item> ret = new HashSet<>();
        for(String id : ids){
            Item block = getItemFromId(id, true);
            if(block != null) ret.add(block);
        }
        return ret;
    }
}
