package lpctools.mixin.client.accessors;

import net.minecraft.client.world.ClientChunkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientChunkManager.class)
public interface ClientChunkAccessor {
	@Accessor ClientChunkManager.ClientChunkMap getChunks();
}
