package lpctools.mixin.client.accessors;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.concurrent.atomic.AtomicReferenceArray;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.world.level.chunk.LevelChunk;

@Mixin(ClientChunkCache.Storage.class)
public interface ClientChunkMapAccessor {
	@Accessor AtomicReferenceArray<@Nullable LevelChunk> getChunks();
	@Accessor int getViewCenterX();
	@Accessor int getViewCenterZ();
}
