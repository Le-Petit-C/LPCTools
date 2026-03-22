package lpctools.tools;

import fi.dy.masa.malilib.config.IConfigBoolean;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.util.InfoUtils;
import fi.dy.masa.malilib.util.StringUtils;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import lpctools.generic.UpdateCounter;
import lpctools.lpcfymasaapi.interfaces.ILPCConfig;
import lpctools.util.LPCMathHelper;
import lpctools.util.Packed;
import lpctools.util.javaex.ToBooleanFunction;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings({"UnusedReturnValue", "unused"})
public class ToolUtils {
    //通过设置配置的热键回调函数设置一个Boolean配置的切换文本显示为LPCTools默认风格
    public static <T extends IConfigBoolean & IHotkey & ILPCConfig> void setLPCToolsToggleText(T config){
        config.getKeybind().setCallback((action, key)->{
            config.toggleBooleanValue();
            displayToggleMessage(config.getBooleanValue(), config);
            return true;
        });
    }
    public static void displayDisableReason(@NotNull ILPCConfig tool, @Nullable String reasonKey){
        String reason = StringUtils.translate("lpctools.tools.disableNotification", tool.getNameTranslation());
        if(reasonKey != null)
            reason += " : " + StringUtils.translate("lpctools.tools.disableReason." + reasonKey);
        InfoUtils.sendVanillaMessage(Text.literal(reason));
    }
    public static void displayDisableMessage(@NotNull ILPCConfig tool){displayDisableReason(tool, null);}
    public static void displayEnableMessage(@NotNull ILPCConfig tool){
        InfoUtils.sendVanillaMessage(Text.translatable("lpctools.tools.enableNotification", tool.getNameTranslation()));
    }
    
    public static void displayToggleMessage(boolean b, ILPCConfig tool){
        if(b) displayEnableMessage(tool);
        else displayDisableMessage(tool);
    }
    
    public static <T, U extends Collection<T>> U recordCollection(U result, @Nullable Collection<? extends T> source){
        if(source != null) result.addAll(source);
        return result;
    }
    public static <T, U extends Collection<T>, V> U recordCollection(U result, @Nullable V source, Function<? super V, ? extends Collection<? extends T>> mappingFunction){
        if(source != null) {
            var collection = mappingFunction.apply(source);
            UpdateCounter.updated(collection.size() / 16);
            int sz = collection.size();
            result.addAll(collection);
        }
        return result;
    }
    public static <T, U, V extends Map<T, U>> V recordMap(V result, @Nullable Map<? extends T, ? extends U> source){
        if(source != null) {
            result.putAll(source);
            UpdateCounter.updated(source.size() / 16);
        }
        return result;
    }
    
    public static <T, U extends Collection<T>> U combineCollections(@NotNull U collection1, @Nullable U collection2) {
        if(collection2 == null) return collection1;
        if(collection1.size() >= collection2.size()) {
            collection1.addAll(collection2);
            UpdateCounter.updated(collection2.size() / 16);
            return collection1;
        }
        else {
            collection2.addAll(collection1);
            UpdateCounter.updated(collection1.size() / 16);
            return collection2;
        }
    }
    
    public static <T> T chunkedGet(Long2ObjectMap<? extends Int2ObjectMap<T>> map, int x, int y, int z) {
        var chunk = map.get(Packed.ChunkPos.packCoords(x, z));
        if(chunk == null) return null;
        else return chunk.get(Packed.ChunkLocal.pack(x, y, z));
    }
    public static <T> T chunkedGet(Long2ObjectMap<? extends Int2ObjectOpenHashMap<T>> map, long packedBlockPos) {
        return chunkedGet(map, Packed.BlockPos.unpackX(packedBlockPos), Packed.BlockPos.unpackY(packedBlockPos), Packed.BlockPos.unpackZ(packedBlockPos));
    }
    public static boolean chunkedContainsKey(Long2ObjectMap<? extends Int2ObjectMap<?>> map, int x, int y, int z) {
        var chunk = map.get(Packed.ChunkPos.packCoords(x, z));
        if(chunk == null) return false;
        else return chunk.containsKey(Packed.ChunkLocal.pack(x, y, z));
    }
    public static boolean chunkedContainsKey(Long2ObjectMap<? extends Int2ObjectMap<?>> map, long packedBlockPos) {
        return chunkedContainsKey(map, Packed.BlockPos.unpackX(packedBlockPos), Packed.BlockPos.unpackY(packedBlockPos), Packed.BlockPos.unpackZ(packedBlockPos));
    }
    public static <T> T chunkedPut(Long2ObjectMap<Int2ObjectOpenHashMap<T>> map, int x, int y, int z, T val) {
        return map.computeIfAbsent(Packed.ChunkPos.packCoords(x, z), k->new Int2ObjectOpenHashMap<>())
            .put(Packed.ChunkLocal.pack(x, y, z), val);
    }
    public static <T> T chunkedPut(Long2ObjectMap<Int2ObjectOpenHashMap<T>> map, long packedBlockPos, T val) {
        return chunkedPut(map, Packed.BlockPos.unpackX(packedBlockPos), Packed.BlockPos.unpackY(packedBlockPos), Packed.BlockPos.unpackZ(packedBlockPos), val);
    }
    
    public static <T> T chunkedRemoveKey(Long2ObjectMap<? extends Int2ObjectMap<T>> map, int x, int y, int z) {
        long packedChunkCoord = Packed.ChunkPos.packCoords(x, z);
        var chunk = map.get(Packed.ChunkPos.packCoords(x, z));
        if(chunk == null) return null;
        else {
            var res = chunk.remove(Packed.ChunkLocal.pack(x, y, z));
            if(chunk.isEmpty()) map.remove(packedChunkCoord);
            return res;
        }
    }
    public static <T> T chunkedRemoveKey(Long2ObjectMap<? extends Int2ObjectMap<T>> map, long packedBlockPos) {
        return chunkedRemoveKey(map, Packed.BlockPos.unpackX(packedBlockPos), Packed.BlockPos.unpackY(packedBlockPos), Packed.BlockPos.unpackZ(packedBlockPos));
    }
    
    public static boolean chunkedContains(Long2ObjectMap<? extends IntSet> set, int x, int y, int z) {
        var chunk = set.get(Packed.ChunkPos.packCoords(x, z));
        if(chunk == null) return false;
        else return chunk.contains(Packed.ChunkLocal.pack(x, y, z));
    }
    public static boolean chunkedContains(Long2ObjectMap<? extends IntSet> set, long packedBlockPos) {
        return chunkedContains(set, Packed.BlockPos.unpackX(packedBlockPos), Packed.BlockPos.unpackY(packedBlockPos), Packed.BlockPos.unpackZ(packedBlockPos));
    }
    
    public static boolean chunkedAdd(Long2ObjectMap<IntOpenHashSet> set, int x, int y, int z) {
        return set.computeIfAbsent(Packed.ChunkPos.packCoords(x, z), k->new IntOpenHashSet())
            .add(Packed.ChunkLocal.pack(x, y, z));
    }
    public static boolean chunkedAdd(Long2ObjectMap<IntOpenHashSet> set, long packedBlockPos) {
        return chunkedAdd(set, Packed.BlockPos.unpackX(packedBlockPos), Packed.BlockPos.unpackY(packedBlockPos), Packed.BlockPos.unpackZ(packedBlockPos));
    }
    
    public static boolean chunkedRemove(Long2ObjectMap<? extends IntSet> set, int x, int y, int z) {
        long packedChunkCoord = Packed.ChunkPos.packCoords(x, z);
        var chunk = set.get(Packed.ChunkPos.packCoords(x, z));
        if(chunk == null) return false;
        else {
            boolean res = chunk.remove(Packed.ChunkLocal.pack(x, y, z));
            if(chunk.isEmpty()) set.remove(packedChunkCoord);
            return res;
        }
    }
    public static boolean chunkedRemove(Long2ObjectMap<? extends IntSet> set, long packedBlockPos) {
        return chunkedRemove(set, Packed.BlockPos.unpackX(packedBlockPos), Packed.BlockPos.unpackY(packedBlockPos), Packed.BlockPos.unpackZ(packedBlockPos));
    }
    
    public static <T> void clearMapDataOutOfRange(double chunkedCamX, double chunkedCamZ, double chunkDistanceLimitSquared, Long2ObjectMap<T> mapToClean, ToBooleanFunction<T> emptyCheck, Consumer<T> cleaner) {
        var it = mapToClean.long2ObjectEntrySet().iterator();
        while (it.hasNext()) {
            var entry = it.next();
            var obj = entry.getValue();
            if(emptyCheck != null && emptyCheck.applyAsBoolean(obj)) {
                it.remove();
                continue;
            }
            long packedChunkPos = entry.getLongKey();
            double distanceSquared = LPCMathHelper.squaredLength(
                Packed.ChunkPos.unpackX(packedChunkPos) - chunkedCamX,
                Packed.ChunkPos.unpackZ(packedChunkPos) - chunkedCamZ
            );
            if(distanceSquared >= chunkDistanceLimitSquared) {
                it.remove();
                if(cleaner != null) cleaner.accept(obj);
            }
        }
    }
}
