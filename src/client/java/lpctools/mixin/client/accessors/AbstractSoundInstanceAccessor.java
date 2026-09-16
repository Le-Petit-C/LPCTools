package lpctools.mixin.client.accessors;

import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractSoundInstance.class)
public interface AbstractSoundInstanceAccessor {
	@Accessor("pitch") float lpctools$getPitch();
	@Accessor("pitch") void lpctools$setPitch(float pitch);
}
