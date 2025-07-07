package lpctools.mixin.client.blockBreakRestriction;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static lpctools.tools.blockBreakRestriction.BlockBreakRestriction.*;
import static lpctools.tools.blockBreakRestriction.BlockBreakRestrictionData.*;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
    @Shadow @Final private MinecraftClient client;
    @Inject(method = {"attackBlock", "updateBlockBreakingProgress"}, at = @At("HEAD"), cancellable = true)
    void beforeBlockBreaking(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir){
        if(testBlockBreaking(pos, direction)) cir.setReturnValue(false);
    }
    @SuppressWarnings("RedundantIfStatement")
    @Unique private boolean testBlockBreaking(BlockPos pos, Direction direction){
        if(client.world == null) return false;
        if(!blockBreakRestriction.getBooleanValue()) return false;
        BlockState state = client.world.getBlockState(pos);
        if(!blockTestMethod.getCurrentUserdata().canBreak(state.getBlock())) return true;
        if(!shapeList.testPos(pos)) return true;
        return false;
    }
}
