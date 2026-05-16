package lpctools.util;


@SuppressWarnings("unused")
public interface Packed {
	
	static int getSectionCoord(int coord) { return net.minecraft.core.SectionPos.blockToSectionCoord(coord); }
	static int getSectionCoord(double coord) { return net.minecraft.core.SectionPos.posToSectionCoord(coord); }
	static int getBlockCoord(int coord) { return net.minecraft.core.SectionPos.sectionToBlockCoord(coord); }
	
	interface ChunkLocal {
		static int pack(int x, int y, int z) { return (x & 15) | (y << 8 >>> 4) | (z << 28); }
		static int pack(net.minecraft.core.BlockPos blockPos) { return pack(blockPos.getX(), blockPos.getY(), blockPos.getZ()); }
		static int unpackX(int packed) { return packed & 15; }
		static int unpackY(int packed) { return packed << 4 >> 8; }
		static int unpackZ(int packed) { return packed >>> 28; }
		static int packedFromBlockPos(long packedBlockPos) {
			return pack(
				BlockPos.unpackX(packedBlockPos),
				BlockPos.unpackY(packedBlockPos),
				BlockPos.unpackZ(packedBlockPos)
			);
		}
	}
	interface BlockPos {
		static long pack(int x, int y, int z) { return net.minecraft.core.BlockPos.asLong(x, y, z); }
		static int unpackX(long packed) { return net.minecraft.core.BlockPos.getX(packed); }
		static int unpackY(long packed) { return net.minecraft.core.BlockPos.getY(packed); }
		static int unpackZ(long packed) { return net.minecraft.core.BlockPos.getZ(packed); }
		static long packedFromChunkLocal(long packedChunkPos, int packedChunkLocalPos){
			return pack(
				getBlockCoord(ChunkPos.unpackX(packedChunkPos)) | ChunkLocal.unpackX(packedChunkLocalPos),
				ChunkLocal.unpackY(packedChunkLocalPos),
				getBlockCoord(ChunkPos.unpackZ(packedChunkPos)) | ChunkLocal.unpackZ(packedChunkLocalPos)
			);
		}
		static long packedFromChunkLocal(int chunkX, int chunkZ, int packedChunkLocalPos){
			return pack(
				getBlockCoord(chunkX) | ChunkLocal.unpackX(packedChunkLocalPos),
				ChunkLocal.unpackY(packedChunkLocalPos),
				getBlockCoord(chunkZ) | ChunkLocal.unpackZ(packedChunkLocalPos)
			);
		}
	}
	interface ChunkPos {
		static long pack(int x, int z) { return net.minecraft.world.level.ChunkPos.pack(x, z); }
		static int unpackX(long packed) { return net.minecraft.world.level.ChunkPos.getX(packed); }
		static int unpackZ(long packed) { return net.minecraft.world.level.ChunkPos.getZ(packed); }
		static long packCoords(int x, int z) { return pack(getSectionCoord(x), getSectionCoord(z)); }
		static long packCoords(double x, double z) { return pack(getSectionCoord(x), getSectionCoord(z)); }
		static long packedFromBlockPos(long packedBlockPos)
		{ return pack(getSectionCoord(BlockPos.unpackX(packedBlockPos)), getSectionCoord(BlockPos.unpackZ(packedBlockPos))); }
	}
	interface ChunkSectionPos {
		static long pack(int x, int y, int z) { return net.minecraft.core.SectionPos.asLong(x, y, z); }
		static int unpackX(long packed) { return net.minecraft.core.SectionPos.x(packed); }
		static int unpackY(long packed) { return net.minecraft.core.SectionPos.y(packed); }
		static int unpackZ(long packed) { return net.minecraft.core.SectionPos.z(packed); }
	}
}
