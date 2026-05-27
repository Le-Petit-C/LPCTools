package lpctools.util;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import lpctools.LPCTools;
import lpctools.lpcfymasaapi.LPCAPIInit;
import lpctools.mixin.client.accessors.ClientChunkAccessor;
import lpctools.mixin.client.accessors.ClientChunkMapAccessor;
import lpctools.util.javaex.Object2BooleanFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3i;
import org.joml.Vector4f;
import org.jspecify.annotations.NonNull;
import org.lwjgl.opengl.GL30;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class DataUtils {
    public static @Nullable String getTextFileResource(ResourceManager manager, Identifier resId){
        Optional<Resource> res = manager.getResource(resId);
        if(res.isEmpty()) return null;
        try(InputStream stream = res.get().open()) {
            return new String(stream.readAllBytes());
        } catch (IOException ignored) { return null; }
    }
    public static void clientMessage(String message, boolean overlay){Minecraft.getInstance().getChatListener().handleSystemMessage(Component.nullToEmpty(message), overlay);}
    public static void clientMessage(Component message, boolean overlay){Minecraft.getInstance().getChatListener().handleSystemMessage(message, overlay);}
    public static <T> void notifyPlayerIf(T value, Function<T, String> converter, Object2BooleanFunction<T> condition, boolean overlay){
        if(condition.getBoolean(value)) clientMessage(converter.apply(value), overlay);
    }
    public static String getItemId(Item item){return BuiltInRegistries.ITEM.getKey(item).toString();}
    public static String getBlockId(Block block){return BuiltInRegistries.BLOCK.getKey(block).toString();}
    public static String getEntityTypeId(EntityType<?> entityType){return BuiltInRegistries.ENTITY_TYPE.getKey(entityType).toString();}
    public static @NotNull ImmutableList<String> idListFromBlockList(@Nullable Iterable<Block> list){
        ImmutableList.Builder<String> builder = new ImmutableList.Builder<>();
        if(list != null) list.forEach(block->builder.add(getBlockId(block)));
        return builder.build();
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
    public static @NotNull ImmutableList<String> idListFromEntityTypeList(@Nullable Iterable<EntityType<?>> list){
        ImmutableList.Builder<String> builder = new ImmutableList.Builder<>();
        if(list != null) list.forEach(entityType->builder.add(getEntityTypeId(entityType)));
        return builder.build();
    }
    
    public static double chunkedCoord(double origin) {
        return origin / 16 - 0.5;
    }
    
    public interface ClassCaster<T, U>{U cast(T v) throws ClassCastException;}
    public static @Nullable <T, U> U getObjectFromId(@NotNull String loggerInfo, @NotNull String id, Registry<T> registry, @NotNull ClassCaster<T, U> caster, boolean notifies){
        try{
            T ret = registry.getValue(Identifier.parse(id));
            Optional<Holder.Reference<T>> defOpt = registry.getAny();
            Holder.Reference<T> defRef = defOpt.orElse(null);
            T def = defRef != null ? defRef.value() : null;
            break1:
            if(Objects.equals(def, ret)){
                if(defRef == null) throw new Exception();
                String defId = defRef.getRegisteredName();
                if(id.equals(defId)) break break1;
                if(id.contains(":")) throw new Exception();
                String[] splitDef = defId.split(":");
                if(!id.trim().equals(splitDef[0].trim())) throw new Exception();
            }
            return caster.cast(ret);
        }catch (Exception e){
            if(notifies){
                clientMessage(String.format("§e%s(%s) failed.", loggerInfo, id), false);
                LPCAPIInit.LOGGER.warn("{}(\"{}\"): {}", loggerInfo, id, e.getMessage());
            }
            return null;
        }
    }
    public static @Nullable Block getBlockFromId(@NotNull String id, boolean notifies){
        return getObjectFromId("getBlockFromId", id, BuiltInRegistries.BLOCK, v->v, notifies);
    }
    public static @Nullable Item getItemFromId(@NotNull String id, boolean notifies){
        return getObjectFromId("getItemFromId", id, BuiltInRegistries.ITEM, v->v, notifies);
    }
    public static @Nullable BlockItem getBlockItemFromId(@NotNull String id, boolean notifies){
        return getObjectFromId("getItemFromId", id, BuiltInRegistries.ITEM, v->(BlockItem)v, notifies);
    }
    public static @Nullable EntityType<?> getEntityTypeFromId(@NotNull String id, boolean notifies){
        return getObjectFromId("getEntityTypeFromId", id, BuiltInRegistries.ENTITY_TYPE, v->(EntityType<?>)v, notifies);
    }
    public static @NotNull ArrayList<@NotNull Block> blockListFromIds(Iterable<String> ids){
        ArrayList<Block> res = new ArrayList<>();
        for(String id : ids){
            Block block = getBlockFromId(id, true);
            if(block != null) res.add(block);
        }
        return res;
    }
    public static @NotNull HashSet<@NotNull Block> blockSetFromIds(Iterable<String> ids){
        HashSet<Block> res = new HashSet<>();
        for(String id : ids){
            Block block = getBlockFromId(id, true);
            if(block != null) res.add(block);
        }
        return res;
    }
    public static @NotNull ArrayList<@NotNull Item> itemListFromIds(Iterable<String> ids){
        ArrayList<@NotNull Item> res = new ArrayList<>();
        for(String id : ids){
            Item block = getItemFromId(id, true);
            if(block != null) res.add(block);
        }
        return res;
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
    public static @NotNull ArrayList<@NotNull EntityType<?>> entityTypeListFromIds(Iterable<String> ids){
        ArrayList<EntityType<?>> ret = new ArrayList<>();
        for(String id : ids){
            var entityType = getEntityTypeFromId(id, true);
            if(entityType != null) ret.add(entityType);
        }
        return ret;
    }
    private static final Object2DoubleOpenHashMap<String> lastTime = new Object2DoubleOpenHashMap<>();
    public static int putGlError(String pos){
        int err = GL30.glGetError();
        String info = ofGLError(err, null);
        if(info == null) return err;
        String formatted = String.format("%s:%x:%s", pos, err, info);
        clientMessage(Component.nullToEmpty(formatted), false);
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
    
    /**
     * Swaps red and blue channels in a 32-bit color integer.
     * Commonly used for ARGB <-> ABGR conversion.
     */
    public static int swapRedBlue(int color){
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
    
    public static String findMostSimilar(Collection<String> collection, String target) {
        if (collection == null || target == null) return null;
        
        String mostSimilar = null;
        int minDistance = Integer.MAX_VALUE;
        
        for (String str : collection) {
            int distance = StringUtils.getLevenshteinDistance(target, str);
            if (distance < minDistance) {
                minDistance = distance;
                mostSimilar = str;
            }
        }
        return mostSimilar;
    }
    
    public static <T> T findMostSimilar(Map<String, T> map, String target) {
        if (map.get(target) instanceof T directMatch) return directMatch;
        return map.get(findMostSimilar(map.keySet(), target));
    }
    
    @Contract(pure = true)
    public static long toPackedChunkSectionPos(Vector3d pos){
        return SectionPos.asLong(
            SectionPos.posToSectionCoord(pos.x),
            SectionPos.posToSectionCoord(pos.y),
            SectionPos.posToSectionCoord(pos.z)
        );
    }
    
    @Contract(pure = true)
    public static long toPackedChunkSectionPos(Vec3 pos){
        return SectionPos.asLong(
            SectionPos.posToSectionCoord(pos.x),
            SectionPos.posToSectionCoord(pos.y),
            SectionPos.posToSectionCoord(pos.z)
        );
    }
    
    // 仿照computeIfAbsent的static方法
    public static <T> T computeIfNull(T val, Supplier<T> supplier){ return val == null ? supplier.get() : val; }
    
    @SuppressWarnings("unchecked")
    public static <T> T[] newArrayLike(T[] template, int length) {
        return (T[]) java.lang.reflect.Array.newInstance(
            template.getClass().getComponentType(),
            length
        );
    }
    
    public static Iterable<LevelChunk> loadedChunks(ClientLevel world){
        ClientChunkCache chunkManager = world.getChunkSource();
        ClientChunkCache.Storage chunkMap = ((ClientChunkAccessor)chunkManager).getStorage();
        var chunks = ((ClientChunkMapAccessor)(Object)chunkMap).getChunks();
        int length = chunks.length();
        int startIndex = 0;
        while (startIndex < length && chunks.get(startIndex) == null) ++startIndex;
        int finalStartIndex = startIndex;
        return new Iterable<>() {
            @Override public @NonNull Iterator<LevelChunk> iterator() {
                return new Iterator<>() {
                    int nextIndex = finalStartIndex;
                    @Override public boolean hasNext() {
                        return nextIndex < length;
                    }
                    
                    @Override public LevelChunk next() {
                        var res = chunks.get(nextIndex++);
                        while (nextIndex < length && chunks.get(nextIndex) == null) ++nextIndex;
                        return res;
                    }
                };
            }
        };
    }
    /**
     * modified form {@link java.awt.Color#RGBtoHSB(int, int, int, float[])}
     */
    public static float[] fRGBtoHSB(float r, float g, float b, float[] hsbvals) {
        float hue, saturation, brightness;
        if (hsbvals == null) hsbvals = new float[3];
        float cmax = Math.max(r, g);
        if (b > cmax) cmax = b;
        float cmin = Math.min(r, g);
        if (b < cmin) cmin = b;
        
        brightness = cmax;
        if (cmax != 0)
            saturation = (cmax - cmin) / cmax;
        else
            saturation = 0;
        if (saturation == 0)
            hue = 0;
        else {
            float redc = (cmax - r) / (cmax - cmin);
            float greenc = (cmax - g) / (cmax - cmin);
            float bluec = (cmax - b) / (cmax - cmin);
            if (r == cmax)
                hue = bluec - greenc;
            else if (g == cmax)
                hue = 2.0f + redc - bluec;
            else
                hue = 4.0f + greenc - redc;
            hue = hue / 6.0f;
            if (hue < 0)
                hue = hue + 1.0f;
        }
        hsbvals[0] = hue;
        hsbvals[1] = saturation;
        hsbvals[2] = brightness;
        return hsbvals;
    }
    public static double[] dRGBtoHSB(double r, double g, double b, double[] hsbvals) {
        double hue, saturation, brightness;
        if (hsbvals == null) hsbvals = new double[3];
        double cmax = Math.max(r, g);
        if (b > cmax) cmax = b;
        double cmin = Math.min(r, g);
        if (b < cmin) cmin = b;
        
        brightness = cmax;
        if (cmax != 0)
            saturation = (cmax - cmin) / cmax;
        else
            saturation = 0;
        if (saturation == 0)
            hue = 0;
        else {
            double redc = (cmax - r) / (cmax - cmin);
            double greenc = (cmax - g) / (cmax - cmin);
            double bluec = (cmax - b) / (cmax - cmin);
            if (r == cmax)
                hue = bluec - greenc;
            else if (g == cmax)
                hue = 2.0f + redc - bluec;
            else
                hue = 4.0f + greenc - redc;
            hue = hue / 6.0f;
            if (hue < 0)
                hue = hue + 1.0f;
        }
        hsbvals[0] = hue;
        hsbvals[1] = saturation;
        hsbvals[2] = brightness;
        return hsbvals;
    }
    public static int dRGB2iRGB(double r, double g, double b) {
        return 0xff000000 | ((int) (r * 255 + 0.5)) | ((int) (g * 255 + 0.5) << 8) | ((int) (b * 255 + 0.5) << 16);
    }
    public static int fRGB2iRGB(float r, float g, float b) {
        return 0xff000000 | ((int) (r * 255 + 0.5f)) | ((int) (g * 255 + 0.5f) << 8) | ((int) (b * 255 + 0.5f) << 16);
    }
    
    public static ChunkPos getCenterChunkPos(ClientLevel world) {
        ClientChunkMapAccessor accessor = (ClientChunkMapAccessor)(Object)((ClientChunkAccessor)world.getChunkSource()).getStorage();
		//noinspection DataFlowIssue
		return new ChunkPos(accessor.getViewCenterX(), accessor.getViewCenterZ());
    }
    
    public interface CameraCenterPosConsumer {
        void acceptPos(double chunkedCenterX, double chunkedCenterZ);
    }
    
    public static void executeWithCameraCenterPos(CameraCenterPosConsumer consumer) {
        Vec3 camPos = Minecraft.getInstance().gameRenderer.getMainCamera().position();
        consumer.acceptPos(chunkedCoord(camPos.x), chunkedCoord(camPos.z));
    }
    
    public interface RenderCenterPosConsumer {
        void acceptPos(double chunkedCenterX, double chunkedCenterZ, double radius);
    }
    
    public static void executeWithRenderCenterPos(RenderCenterPosConsumer consumer, double expandRadius) {
        Minecraft mc = Minecraft.getInstance();
        Vec3 camPos = mc.gameRenderer.getMainCamera().position();
        double chunkedCamX = chunkedCoord(camPos.x);
        double chunkedCamZ = chunkedCoord(camPos.z);
        double chunkedX, chunkedZ, radius;
        if(mc.level instanceof ClientLevel world) {
            ChunkPos worldCenterChunkPos = getCenterChunkPos(world);
            chunkedX = (worldCenterChunkPos.x() + chunkedCamX) * 0.5;
            chunkedZ = (worldCenterChunkPos.z() + chunkedCamZ) * 0.5;
            double XOffset = chunkedX - chunkedCamX;
            double ZOffset = chunkedZ - chunkedCamZ;
            radius = Math.sqrt(XOffset * XOffset + ZOffset * ZOffset) + expandRadius;
        }
        else {
            chunkedX = chunkedCamX;
            chunkedZ = chunkedCamZ;
            radius = expandRadius;
        }
        consumer.acceptPos(chunkedX, chunkedZ, radius);
    }
}
