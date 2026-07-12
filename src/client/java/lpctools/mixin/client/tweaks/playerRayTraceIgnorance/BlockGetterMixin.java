package lpctools.mixin.client.tweaks.playerRayTraceIgnorance;

import lpctools.tweaks.PlayerCrosshairFilter;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static lpctools.tweaks.PlayerCrosshairFilter.isLocalPlayerGettingHitResult;

@Mixin(BlockGetter.class)
public interface BlockGetterMixin {
    @Inject(method = "clip", at = @At("HEAD"), cancellable = true)
    private void injectClipHead(ClipContext c, CallbackInfoReturnable<BlockHitResult> cir) {
        if (isLocalPlayerGettingHitResult && PlayerCrosshairFilter.passThroughBlocks.getBooleanValue() && Minecraft.getInstance().isSameThread()) {
            Vec3 to = c.getTo();
            Vec3 delta = c.getFrom().subtract(to);
            cir.setReturnValue(BlockHitResult.miss(to, Direction.getApproximateNearest(delta.x, delta.y, delta.z), BlockPos.containing(to)));
        }
    }
}
