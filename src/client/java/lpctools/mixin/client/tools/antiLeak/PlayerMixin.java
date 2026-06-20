package lpctools.mixin.client.tools.antiLeak;

import lpctools.tools.leakPreventer.LeakPreventer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerMixin {
	@Inject(method = "blockActionRestricted", at = @At("RETURN"), cancellable = true)
	void injectBlockActionRestrictedReturn(Level level, BlockPos pos, GameType gameType, CallbackInfoReturnable<Boolean> cir) {
		if(cir.getReturnValue()) return;
		if(LeakPreventer.testLeak(level, pos)) cir.setReturnValue(true);
	}
}
