package lpctools.mixin.client.recorders;

import lpctools.mixinData.MultiPlayerGameModeExtraData;
import lpctools.util.GameTime;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin implements MultiPlayerGameModeExtraData.Getter {
	@Unique MultiPlayerGameModeExtraData.Mutable data = new MultiPlayerGameModeExtraData.Mutable();

	@Override public MultiPlayerGameModeExtraData lpctools$getMultiPlayerGameModeExtraData() { return data; }

	@Inject(method = "continueDestroyBlock", at = @At("HEAD"))
	void injectContinueDestroyBlockHead(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
		data.setLastContinueBreakTick(GameTime.getClientTickCount());
	}
}
