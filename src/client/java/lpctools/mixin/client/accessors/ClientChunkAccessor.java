package lpctools.mixin.client.accessors;

import net.minecraft.client.multiplayer.ClientChunkCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientChunkCache.class)
public interface ClientChunkAccessor {
	@Accessor ClientChunkCache.Storage getStorage();
}
