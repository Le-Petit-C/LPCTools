package lpctools.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static lpctools.lpcfymasaapi.Registries.CLIENT_WORLD_CHUNK_SET_BLOCK_STATE;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

@Mixin(LevelChunk.class)
public class WorldChunkMixin {
    @Inject(method = "setBlockState", at = @At("RETURN"))
    void chunkSetBlockState(BlockPos pos, BlockState state, int flags, CallbackInfoReturnable<BlockState> cir){
        if(CLIENT_WORLD_CHUNK_SET_BLOCK_STATE.isEmpty()) return;
        LevelChunk castedThis = (LevelChunk)(Object)this;
        if(!(castedThis.getLevel() instanceof ClientLevel)) return;
        ChunkPos chunkPos = castedThis.getPos();
        int rx = (pos.getX() & 15) + chunkPos.getMinBlockX();
        int rz = (pos.getZ() & 15) + chunkPos.getMinBlockZ();
        CLIENT_WORLD_CHUNK_SET_BLOCK_STATE.runner().onClientWorldChunkSetBlockState(castedThis, new BlockPos(rx, pos.getY(), rz), cir.getReturnValue(), state);
    }
}
