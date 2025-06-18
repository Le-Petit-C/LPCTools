package lpctools.util;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import lpctools.lpcfymasaapi.LPCAPIInit;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL30;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

@SuppressWarnings("unused")
public class DataUtils {
    public static @Nullable String getTextFileResource(ResourceManager manager, Identifier resId){
        Optional<Resource> res = manager.getResource(resId);
        if(res.isEmpty()) return null;
        try {return new String(res.get().getInputStream().readAllBytes());
        } catch (IOException ignored) {return null;}
    }
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
        return itemSetFromIds(ids, new HashSet<>(), false);
    }
    public static <T extends Collection<Item>> @NotNull T itemSetFromIds(Iterable<String> ids, T result, boolean clear){
        if(clear) result.clear();
        for(String id : ids){
            Item block = getItemFromId(id, true);
            if(block != null) result.add(block);
        }
        return result;
    }
    private static final Object2DoubleOpenHashMap<String> lastTime = new Object2DoubleOpenHashMap<>();
    public static int putGlError(String pos){
        int err = GL30.glGetError();
        String info = ofGLError(err, null);
        if(info == null) return err;
        notifyPlayer(Text.of(String.format("%s:%x:%s", pos, err, info)), false);
        return err;
    }
    public static int putGlError(String pos, double time){
        double current = System.currentTimeMillis() / 1000.0;
        if(current - lastTime.getDouble(pos) < time) return GL30.glGetError();
        lastTime.put(pos, current);
        return putGlError(pos);
    }
    public static String ofGLError(int glError, String def){
        return switch (glError) {
            case GL30.GL_INVALID_ENUM -> "GL_INVALID_ENUM";
            case GL30.GL_INVALID_VALUE -> "GL_INVALID_VALUE";
            case GL30.GL_INVALID_OPERATION -> "GL_INVALID_OPERATION";
            case GL30.GL_STACK_OVERFLOW -> "GL_STACK_OVERFLOW";
            case GL30.GL_STACK_UNDERFLOW -> "GL_STACK_UNDERFLOW";
            case GL30.GL_OUT_OF_MEMORY -> "GL_OUT_OF_MEMORY";
            default -> def;
        };
    }
    public static int argb2agbr(int color){
        int s = color & 0x00ff00ff;
        return (color & 0xff00ff00) | (s >> 16) | (s << 16);
    }
    public static Vector4f argb2VectorABGRf(int color){
        return new Vector4f(
            ((color >>> 16) & 0xff) / 255.0f,
            ((color >>> 8) & 0xff) / 255.0f,
            (color & 0xff) / 255.0f,
            ((color >>> 24) & 0xff) / 255.0f
        );
    }
    public static double squaredDistance(Vec3d pos, ChunkPos chunkPos){
        return pos.squaredDistanceTo(chunkPos.x * 16 + 8.0, pos.y, chunkPos.z * 16 + 8.0);
    }
}
