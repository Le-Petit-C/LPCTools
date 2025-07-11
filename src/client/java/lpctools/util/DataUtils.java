package lpctools.util;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import lpctools.LPCTools;
import lpctools.lpcfymasaapi.LPCAPIInit;
import lpctools.util.javaex.Object2BooleanFunction;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL30;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

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
    public static <T> void notifyPlayerIf(T value, Function<T, String> converter, Object2BooleanFunction<T> condition, boolean overlay){
        if(condition.getBoolean(value)) notifyPlayer(converter.apply(value), overlay);
    }
    public static String getItemId(Item item){return Registries.ITEM.getEntry(item).getIdAsString();}
    public static String getBlockId(Block block){return Registries.BLOCK.getEntry(block).getIdAsString();}
    public static @NotNull ImmutableList<String> idListFromBlockList(@Nullable Iterable<Block> list){
        ArrayList<String> ret = new ArrayList<>();
        if(list != null) list.forEach(block->ret.add(getBlockId(block)));
        return ImmutableList.copyOf(ret);
    }
    public static @NotNull ImmutableList<BlockItem> blockItemListFromItemList(@Nullable Iterable<? extends Item> list, Consumer<? super Item> warn){
        ArrayList<BlockItem> ret = new ArrayList<>();
        if(list != null) list.forEach(item->{
            if(item instanceof BlockItem blockItem) ret.add(blockItem);
            else warn.accept(item);
        });
        return ImmutableList.copyOf(ret);
    }
    public static @NotNull ImmutableList<BlockItem> blockItemListFromItemList(@Nullable Iterable<? extends Item> list, Logger warn){
        return blockItemListFromItemList(list, item->warn.warn("{} not instanceof blockItem", item));
    }
    public static @NotNull ImmutableList<String> idListFromItemList(@Nullable Iterable<? extends Item> list){
        ArrayList<String> ret = new ArrayList<>();
        if(list != null) list.forEach(item->ret.add(getItemId(item)));
        return ImmutableList.copyOf(ret);
    }
    public interface ClassCaster<T, U>{U cast(T v) throws ClassCastException;}
    public static @Nullable <T, U> U getObjectFromId(@NotNull String loggerInfo, @NotNull String id, Registry<T> registry, @NotNull ClassCaster<T, U> caster, boolean notifies){
        try{
            T ret = registry.get(Identifier.of(id));
            Optional<RegistryEntry.Reference<T>> defOpt = registry.getDefaultEntry();
            RegistryEntry.Reference<T> defRef = defOpt.orElse(null);
            T def = defRef != null ? defRef.value() : null;
            break1:
            if(Objects.equals(def, ret)){
                if(defRef == null) throw new Exception();
                String defId = defRef.getIdAsString();
                if(id.equals(defId)) break break1;
                if(id.contains(":")) throw new Exception();
                String[] splitDef = defId.split(":");
                if(!id.trim().equals(splitDef[0].trim())) throw new Exception();
            }
            return caster.cast(ret);
        }catch (Exception e){
            if(notifies){
                notifyPlayer(String.format("§e%s(%s) failed.", loggerInfo, id), false);
                LPCAPIInit.LOGGER.warn("{}(\"{}\"): {}", loggerInfo, id, e.getMessage());
            }
            return null;
        }
    }
    public static @Nullable Block getBlockFromId(@NotNull String id, boolean notifies){
        return getObjectFromId("getBlockFromId", id, Registries.BLOCK, v->v, notifies);
    }
    public static @Nullable Item getItemFromId(@NotNull String id, boolean notifies){
        return getObjectFromId("getItemFromId", id, Registries.ITEM, v->v, notifies);
    }
    public static @Nullable BlockItem getBlockItemFromId(@NotNull String id, boolean notifies){
        return getObjectFromId("getItemFromId", id, Registries.ITEM, v->(BlockItem)v, notifies);
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
        String formatted = String.format("%s:%x:%s", pos, err, info);
        notifyPlayer(Text.of(formatted), false);
        LPCTools.LOGGER.info(formatted);
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
    public static Vector3i toVector3i(Vec3i vec){
        return new Vector3i(vec.getX(), vec.getY(), vec.getZ());
    }
    public static BlockPos toBlockPos(Vector3i vec){
        return new BlockPos(vec.x, vec.y, vec.z);
    }
    public static StringBuilder appendNodeIfNotEmpty(StringBuilder builder, String str){
        if(str.isEmpty()) return builder;
        return builder.append('.').append(str);
    }
}
