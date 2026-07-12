package lpctools.util.data.minecraft;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import lpctools.util.Packed;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.FluidState;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class CombinedBlockGetters implements BlockGetter {
	public @NonNull BlockState blockStateOutOfRange = Blocks.VOID_AIR.defaultBlockState();
	public void putChunk(long packedChunkPos, BlockGetter chunk) {
		storedChunks.put(packedChunkPos, chunk);
		bottomY = Math.min(bottomY, chunk.getMinY());
		topY = Math.max(topY, chunk.getMinY() + chunk.getHeight());
	}
	public void putChunk(ChunkPos chunkPos, BlockGetter chunk){ putChunk(chunkPos.pack(), chunk); }
	public void putChunk(ChunkAccess chunk){ putChunk(chunk.getPos(), chunk); }
	
	@Override public @Nullable BlockEntity getBlockEntity(@NonNull BlockPos pos) {
		if(getChunk(pos) instanceof LevelChunk chunk) return chunk.getBlockEntity(pos);
		else return null;
	}
	
	@Override public @NonNull BlockState getBlockState(@NonNull BlockPos pos) {
		if(getChunk(pos) instanceof LevelChunk chunk) return chunk.getBlockState(pos);
		else return blockStateOutOfRange;
	}
	
	@Override public @NonNull FluidState getFluidState(@NonNull BlockPos pos) {
		if(getChunk(pos) instanceof LevelChunk chunk) return chunk.getFluidState(pos);
		else return blockStateOutOfRange.getFluidState();
	}
	
	@Override public int getHeight() { return topY - bottomY; }
	@Override public int getMinY() { return bottomY; }
	
	private int bottomY = 0, topY = 0;
	
	private @Nullable BlockGetter getChunk(BlockPos pos) {
		BlockGetter chunk = storedChunks.getOrDefault(Packed.ChunkPos.packCoords(pos.getX(), pos.getZ()), null);
		if(chunk == null) return null;
		if(chunk.getMinY() <= pos.getY() && pos.getY() <= chunk.getMaxY()) return chunk;
		else return null;
	}
	
	Long2ObjectOpenHashMap<BlockGetter> storedChunks = new Long2ObjectOpenHashMap<>();
}
