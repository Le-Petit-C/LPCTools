package lpctools.util;

import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import lpctools.util.javaex.functions.ToDoubleObjectDoubleBiFunction;
import net.minecraft.core.BlockPos;

public class ChunkedUtils {
	public static <T> T chunkedGet(Long2ObjectMap<? extends Int2ObjectMap<T>> map, int x, int y, int z) {
		var chunk = map.get(Packed.packChunkPosFromCoords(x, z));
		if(chunk == null) return null;
		else return chunk.get(Packed.packChunkLocal(x, y, z));
	}

	public static <T> T chunkedGet(Long2ObjectMap<? extends Int2ObjectOpenHashMap<T>> map, long packedBlockPos) {
		return chunkedGet(map, Packed.unpackBlockPosX(packedBlockPos), Packed.unpackBlockPosY(packedBlockPos), Packed.unpackBlockPosZ(packedBlockPos));
	}

	public static double chunkedGetOrDefault(Long2ObjectMap<? extends Int2DoubleMap> map, BlockPos pos, double def) {
		Int2DoubleMap chunk = map.get(Packed.packChunkPos(pos));
		if(chunk == null) return def;
		else return chunk.getOrDefault(Packed.packChunkLocal(pos), def);
	}

	public static boolean chunkedContainsKey(Long2ObjectMap<? extends Int2ObjectMap<?>> map, int x, int y, int z) {
		var chunk = map.get(Packed.packChunkPosFromCoords(x, z));
		if(chunk == null) return false;
		else return chunk.containsKey(Packed.packChunkLocal(x, y, z));
	}

	public static boolean chunkedContainsKey(Long2ObjectMap<? extends Int2ObjectMap<?>> map, long packedBlockPos) {
		return chunkedContainsKey(map, Packed.unpackBlockPosX(packedBlockPos), Packed.unpackBlockPosY(packedBlockPos), Packed.unpackBlockPosZ(packedBlockPos));
	}

	public static <T> T chunkedPut(Long2ObjectMap<Int2ObjectOpenHashMap<T>> map, int x, int y, int z, T val) {
		return map.computeIfAbsent(Packed.packChunkPosFromCoords(x, z), _ ->new Int2ObjectOpenHashMap<>())
			.put(Packed.packChunkLocal(x, y, z), val);
	}

	public static <T> T chunkedPut(Long2ObjectMap<Int2ObjectOpenHashMap<T>> map, long packedBlockPos, T val) {
		return chunkedPut(map, Packed.unpackBlockPosX(packedBlockPos), Packed.unpackBlockPosY(packedBlockPos), Packed.unpackBlockPosZ(packedBlockPos), val);
	}

	public static double chunkedPutIfAbsent(Long2ObjectMap<Int2DoubleOpenHashMap> map, BlockPos pos, double value) {
		Int2DoubleMap chunk = map.computeIfAbsent(Packed.packChunkPos(pos), _ -> new Int2DoubleOpenHashMap());
		return chunk.putIfAbsent(Packed.packChunkLocal(pos), value);
	}

	public static <T> T chunkedRemoveKey(Long2ObjectMap<? extends Int2ObjectMap<T>> map, int x, int y, int z) {
		long packedChunkCoord = Packed.packChunkPosFromCoords(x, z);
		var chunk = map.get(Packed.packChunkPosFromCoords(x, z));
		if(chunk == null) return null;
		else {
			var res = chunk.remove(Packed.packChunkLocal(x, y, z));
			if(chunk.isEmpty()) map.remove(packedChunkCoord);
			return res;
		}
	}

	public static <T> T chunkedRemoveKey(Long2ObjectMap<? extends Int2ObjectMap<T>> map, long packedBlockPos) {
		return chunkedRemoveKey(map, Packed.unpackBlockPosX(packedBlockPos), Packed.unpackBlockPosY(packedBlockPos), Packed.unpackBlockPosZ(packedBlockPos));
	}

	public static boolean chunkedRemoveKeyWithSuccess(Long2ObjectMap<? extends Int2DoubleMap> map, BlockPos pos) {
		long chunkPos = Packed.packChunkPos(pos);
		Int2DoubleMap chunk = map.getOrDefault(chunkPos, null);
		if(chunk == null) return false;
		int chunkLocal = Packed.packChunkLocal(pos);
		if(!chunk.containsKey(chunkLocal)) return false;
		chunk.remove(chunkLocal);
		if(chunk.isEmpty()) map.remove(chunkPos);
		return true;
	}

	public static double chunkedComputeWithDefault(Long2ObjectMap<Int2DoubleOpenHashMap> map, BlockPos pos, ToDoubleObjectDoubleBiFunction<BlockPos> fn, double def) {
		Int2DoubleMap chunk = map.computeIfAbsent(Packed.packChunkPos(pos), _ -> new Int2DoubleOpenHashMap());
		return chunk.compute(Packed.packChunkLocal(pos), (_, old) -> fn.applyAsDouble(pos, old == null ? def : old));
	}

	public static boolean chunkedContains(Long2ObjectMap<? extends IntSet> set, int x, int y, int z) {
		var chunk = set.get(Packed.packChunkPosFromCoords(x, z));
		if(chunk == null) return false;
		else return chunk.contains(Packed.packChunkLocal(x, y, z));
	}

	public static boolean chunkedContains(Long2ObjectMap<? extends IntSet> set, long packedBlockPos) {
		return chunkedContains(set, Packed.unpackBlockPosX(packedBlockPos), Packed.unpackBlockPosY(packedBlockPos), Packed.unpackBlockPosZ(packedBlockPos));
	}

	public static boolean chunkedAdd(Long2ObjectMap<IntOpenHashSet> set, int x, int y, int z) {
		return set.computeIfAbsent(Packed.packChunkPosFromCoords(x, z), _ ->new IntOpenHashSet())
			.add(Packed.packChunkLocal(x, y, z));
	}

	public static boolean chunkedAdd(Long2ObjectMap<IntOpenHashSet> set, long packedBlockPos) {
		return chunkedAdd(set, Packed.unpackBlockPosX(packedBlockPos), Packed.unpackBlockPosY(packedBlockPos), Packed.unpackBlockPosZ(packedBlockPos));
	}

	public static boolean chunkedRemove(Long2ObjectMap<? extends IntSet> set, int x, int y, int z) {
		long packedChunkCoord = Packed.packChunkPosFromCoords(x, z);
		var chunk = set.get(Packed.packChunkPosFromCoords(x, z));
		if(chunk == null) return false;
		else {
			boolean res = chunk.remove(Packed.packChunkLocal(x, y, z));
			if(chunk.isEmpty()) set.remove(packedChunkCoord);
			return res;
		}
	}

	public static boolean chunkedRemove(Long2ObjectMap<? extends IntSet> set, long packedBlockPos) {
		return chunkedRemove(set, Packed.unpackBlockPosX(packedBlockPos), Packed.unpackBlockPosY(packedBlockPos), Packed.unpackBlockPosZ(packedBlockPos));
	}
}
