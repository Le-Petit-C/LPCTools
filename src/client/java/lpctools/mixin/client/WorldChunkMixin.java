package lpctools.mixin.client;

import lpctools.tools.SlightXRay.SlightXRay;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldChunk.class)
public class WorldChunkMixin {
    @Inject(method = "setBlockState", at = @At("RETURN"))
    void SlightXRayTest(BlockPos pos, BlockState state, int flags, CallbackInfoReturnable<BlockState> cir){
        if(SlightXRay.slightXRay.getAsBoolean()){
            WorldChunk castedThis = (WorldChunk)(Object)this;
            World world = castedThis.getWorld();
            ChunkPos chunkPos = castedThis.getPos();
            int rx = (pos.getX() & 15) + chunkPos.getStartX();
            int rz = (pos.getZ() & 15) + chunkPos.getStartZ();
            SlightXRay.setBlockStateTest(world, new BlockPos(rx, pos.getY(), rz), cir.getReturnValue(), state);
        }
    }
}
