package lpctools.mixin.client;

import lpctools.lpcfymasaapi.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static lpctools.lpcfymasaapi.Registries.CLIENT_WORLD_CHUNK_SET_BLOCK_STATE;

@Mixin(WorldChunk.class)
public class WorldChunkMixin {
    @Inject(method = "setBlockState", at = @At("RETURN"))
    void chunkSetBlockState(BlockPos pos, BlockState state, boolean moved, CallbackInfoReturnable<BlockState> cir){
        WorldChunk castedThis = (WorldChunk)(Object)this;
        if(!(castedThis.getWorld() instanceof ClientWorld)) return;
        ChunkPos chunkPos = castedThis.getPos();
        int rx = (pos.getX() & 15) + chunkPos.getStartX();
        int rz = (pos.getZ() & 15) + chunkPos.getStartZ();
        if(!Registry.isClientChunkSetBlockStateCallbackEmpty()){
            Registry.runClientWorldChunkSetBlockState(castedThis, new BlockPos(rx, pos.getY(), rz), cir.getReturnValue(), state);
        }
        if(!CLIENT_WORLD_CHUNK_SET_BLOCK_STATE.isEmpty())
            CLIENT_WORLD_CHUNK_SET_BLOCK_STATE.run().onClientWorldChunkSetBlockState(castedThis, new BlockPos(rx, pos.getY(), rz), cir.getReturnValue(), state);
    }
}
