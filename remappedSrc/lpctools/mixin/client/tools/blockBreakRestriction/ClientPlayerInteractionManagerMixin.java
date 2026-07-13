package lpctools.mixin.client.tools.blockBreakRestriction;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static lpctools.tools.breakRestriction.BreakRestriction.*;
import static lpctools.tools.breakRestriction.BreakRestrictionData.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(MultiPlayerGameMode.class)
public class ClientPlayerInteractionManagerMixin {
    @Shadow @Final private Minecraft minecraft;
    @Inject(method = {"startDestroyBlock", "continueDestroyBlock"}, at = @At("HEAD"), cancellable = true)
    void beforeBlockBreaking(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir){
        if(testBlockBreaking(pos, direction)) cir.setReturnValue(false);
    }
    @SuppressWarnings("RedundantIfStatement")
    @Unique private boolean testBlockBreaking(BlockPos pos, Direction direction){
        if(minecraft.level == null) return false;
        if(!BRConfig.getBooleanValue()) return false;
        BlockState state = minecraft.level.getBlockState(pos);
        if(!blockTestMethod.getCurrentUserdata().right.applyAsBoolean(state.getBlock())) return true;
        if(!shapeList.testPos(pos)) return true;
        return false;
    }
}
