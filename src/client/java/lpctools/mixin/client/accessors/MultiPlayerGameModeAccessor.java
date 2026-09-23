package lpctools.mixin.client.accessors;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MultiPlayerGameMode.class)
public interface MultiPlayerGameModeAccessor {
	@Accessor int getDestroyDelay();
	@Accessor void setDestroyDelay(int ticks);
	@Accessor @Nullable ClientPacketListener getConnection();
	@Accessor BlockPos getDestroyBlockPos();
}
