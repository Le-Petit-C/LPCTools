package lpctools.util;

import net.minecraft.util.math.ChunkSectionPos;

@SuppressWarnings("unused")
public class Packed {
	
	public static int getSectionCoord(int coord) { return ChunkSectionPos.getSectionCoord(coord); }
	public static int getSectionCoord(double coord) { return ChunkSectionPos.getSectionCoord(coord); }
	public static int getBlockCoord(int coord) { return ChunkSectionPos.getBlockCoord(coord); }
	
	public static class ChunkLocal {
		public static int pack(int x, int y, int z) { return (x & 15) | (y << 8 >>> 4) | (z << 28); }
		public static int unpackX(int packed) { return packed & 15; }
		public static int unpackY(int packed) { return packed << 4 >> 8; }
		public static int unpackZ(int packed) { return packed >>> 28; }
		public static int packedFromBlockPos(long packedBlockPos) {
			return pack(
				BlockPos.unpackX(packedBlockPos),
				BlockPos.unpackY(packedBlockPos),
				BlockPos.unpackZ(packedBlockPos)
			);
		}
	}
	public static class BlockPos {
		public static long pack(int x, int y, int z) { return net.minecraft.util.math.BlockPos.asLong(x, y, z); }
		public static int unpackX(long packed) { return net.minecraft.util.math.BlockPos.unpackLongX(packed); }
		public static int unpackY(long packed) { return net.minecraft.util.math.BlockPos.unpackLongY(packed); }
		public static int unpackZ(long packed) { return net.minecraft.util.math.BlockPos.unpackLongZ(packed); }
		public static long packedFromChunkLocal(long packedChunkPos, int packedChunkLocalPos){
			return pack(
				getBlockCoord(ChunkPos.unpackX(packedChunkPos)) | ChunkLocal.unpackX(packedChunkLocalPos),
				ChunkLocal.unpackY(packedChunkLocalPos),
				getBlockCoord(ChunkPos.unpackZ(packedChunkPos)) | ChunkLocal.unpackZ(packedChunkLocalPos)
			);
		}
	}
	public static class ChunkPos {
		public static long pack(int x, int z) { return net.minecraft.util.math.ChunkPos.toLong(x, z); }
		public static int unpackX(long packed) { return net.minecraft.util.math.ChunkPos.getPackedX(packed); }
		public static int unpackZ(long packed) { return net.minecraft.util.math.ChunkPos.getPackedZ(packed); }
		public static long packCoords(int x, int z) { return pack(getSectionCoord(x), getSectionCoord(z)); }
		public static long packCoords(double x, double z) { return pack(getSectionCoord(x), getSectionCoord(z)); }
		public static long packedFromBlockPos(long packedBlockPos)
		{ return pack(getSectionCoord(BlockPos.unpackX(packedBlockPos)), getSectionCoord(BlockPos.unpackZ(packedBlockPos))); }
	}
}
