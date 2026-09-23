package lpctools.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;

@SuppressWarnings("unused")
public interface Packed {
	static int getSectionCoord(int coord) { return net.minecraft.core.SectionPos.blockToSectionCoord(coord); }
	static int getSectionCoord(double coord) { return net.minecraft.core.SectionPos.posToSectionCoord(coord); }
	static int getBlockCoord(int coord) { return net.minecraft.core.SectionPos.sectionToBlockCoord(coord); }

	static int packChunkLocal(int x, int y, int z) { return (x & 15) | (y << 8 >>> 4) | (z << 28); }
	static int packChunkLocal(BlockPos blockPos) { return packChunkLocal(blockPos.getX(), blockPos.getY(), blockPos.getZ()); }
	static int unpackChunkLocalX(int packed) { return packed & 15; }
	static int unpackChunkLocalY(int packed) { return packed << 4 >> 8; }
	static int unpackChunkLocalZ(int packed) { return packed >>> 28; }
	static BlockPos unpackChunkLocal(int packed) {
		return new BlockPos(
			unpackChunkLocalX(packed),
			unpackChunkLocalY(packed),
			unpackChunkLocalZ(packed)
		);
	}
	static BlockPos.MutableBlockPos unpackChunkLocal(int packed, BlockPos.MutableBlockPos res) {
		return res.set(
			unpackChunkLocalX(packed),
			unpackChunkLocalY(packed),
			unpackChunkLocalZ(packed)
		);
	}
	static int packChunkLocalFromBlockPos(long packedBlockPos) {
		return packChunkLocal(
			unpackBlockPosX(packedBlockPos),
			unpackBlockPosY(packedBlockPos),
			unpackBlockPosZ(packedBlockPos)
		);
	}

	static long packBlockPos(int x, int y, int z) { return BlockPos.asLong(x, y, z); }
	static long packBlockPos(BlockPos pos) { return pos.asLong(); }
	static int unpackBlockPosX(long packed) { return BlockPos.getX(packed); }
	static int unpackBlockPosY(long packed) { return BlockPos.getY(packed); }
	static int unpackBlockPosZ(long packed) { return BlockPos.getZ(packed); }
	static BlockPos unpackBlockPos(long packed) { return BlockPos.of(packed); }
	static BlockPos unpackBlockPosFromChunkLocal(long packedChunkPos, int packedChunkLocalPos){
		return new BlockPos(
			getBlockCoord(unpackChunkPosX(packedChunkPos)) | unpackChunkLocalX(packedChunkLocalPos),
			unpackChunkLocalY(packedChunkLocalPos),
			getBlockCoord(unpackChunkPosZ(packedChunkPos)) | unpackChunkLocalZ(packedChunkLocalPos)
		);
	}
	static BlockPos.MutableBlockPos unpackBlockPosFromChunkLocal(long packedChunkPos, int packedChunkLocalPos, BlockPos.MutableBlockPos res){
		return res.set(
			getBlockCoord(unpackChunkPosX(packedChunkPos)) | unpackChunkLocalX(packedChunkLocalPos),
			unpackChunkLocalY(packedChunkLocalPos),
			getBlockCoord(unpackChunkPosZ(packedChunkPos)) | unpackChunkLocalZ(packedChunkLocalPos)
		);
	}
	static long packBlockPosFromChunkLocal(long packedChunkPos, int packedChunkLocalPos){
		return packBlockPos(
			getBlockCoord(unpackChunkPosX(packedChunkPos)) | unpackChunkLocalX(packedChunkLocalPos),
			unpackChunkLocalY(packedChunkLocalPos),
			getBlockCoord(unpackChunkPosZ(packedChunkPos)) | unpackChunkLocalZ(packedChunkLocalPos)
		);
	}
	static long packBlockPosFromChunkLocal(int chunkX, int chunkZ, int packedChunkLocalPos){
		return packBlockPos(
			getBlockCoord(chunkX) | unpackChunkLocalX(packedChunkLocalPos),
			unpackChunkLocalY(packedChunkLocalPos),
			getBlockCoord(chunkZ) | unpackChunkLocalZ(packedChunkLocalPos)
		);
	}

	static long packChunkPos(int x, int z) { return ChunkPos.pack(x, z); }
	static long packChunkPos(ChunkPos pos) { return pos.pack(); }
	static long packChunkPos(BlockPos pos) { return packChunkPosFromCoords(pos.getX(), pos.getZ()); }
	static int unpackChunkPosX(long packed) { return ChunkPos.getX(packed); }
	static int unpackChunkPosZ(long packed) { return ChunkPos.getZ(packed); }
	static ChunkPos unpackChunkPos(long packed) { return ChunkPos.unpack(packed); }
	static long packChunkPosFromCoords(int x, int z) { return packChunkPos(getSectionCoord(x), getSectionCoord(z)); }
	static long packChunkPosFromCoords(double x, double z) { return packChunkPos(getSectionCoord(x), getSectionCoord(z)); }
	static long packedChunkPosFromBlockPos(long packedBlockPos)
	{ return packChunkPos(getSectionCoord(unpackBlockPosX(packedBlockPos)), getSectionCoord(unpackBlockPosZ(packedBlockPos))); }

	static long packSectionPos(int x, int y, int z) { return SectionPos.asLong(x, y, z); }
	static long packSectionPos(SectionPos pos) { return pos.asLong(); }
	static int unpackSectionPosX(long packed) { return SectionPos.x(packed); }
	static int unpackSectionPosY(long packed) { return SectionPos.y(packed); }
	static int unpackSectionPosZ(long packed) { return SectionPos.z(packed); }
	static SectionPos unpackSectionPos(long packed) { return SectionPos.of(packed); }
}
