package lpctools.util;

import com.google.common.collect.ImmutableList;
import lpctools.lpcfymasaapi.LPCAPIInit;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;

@SuppressWarnings("unused")
public class DataUtils {
    public static void notifyPlayer(String message, boolean overlay){
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if(player != null) player.sendMessage(Text.of(message), overlay);
    }
    public static void notifyPlayer(Text message, boolean overlay){
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if(player != null) player.sendMessage(message, overlay);
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
            Block block = Registries.BLOCK.get(Identifier.of(id));
            break1:
            if(block == Blocks.AIR){
                String[] processedIds = id.split(":");
                if(processedIds.length == 2){
                    if(processedIds[0].trim().equals("minecraft")
                        && processedIds[1].trim().equals("air"))
                        break break1;
                }
                else if(processedIds.length == 1){
                    if(processedIds[0].trim().equals("air"))
                        break break1;
                }
                throw new Exception();
            }
            return block;
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
            Item item = Registries.ITEM.get(Identifier.of(id));
            break1:
            if(item == Items.AIR){
                String[] processedIds = id.split(":");
                if(processedIds.length == 2){
                    if(processedIds[0].trim().equals("minecraft")
                        && processedIds[1].trim().equals("air"))
                        break break1;
                }
                else if(processedIds.length == 1){
                    if(processedIds[0].trim().equals("air"))
                        break break1;
                }
                throw new Exception();
            }
            return item;
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
