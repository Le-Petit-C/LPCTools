package lpctools.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import lpctools.lpcfymasaapi.Registries;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Shadow private ClientWorld world;
    @ModifyArg(method = "onChunkData", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;enqueueChunkUpdate(Ljava/lang/Runnable;)V"))
    Runnable inject(Runnable updater, @Local(argsOnly = true) ChunkDataS2CPacket packet){
        int i = packet.getX();
        int j = packet.getZ();
        return ()->{
            updater.run();
            WorldChunk worldChunk = world.getChunkManager().getWorldChunk(i, j, false);
            if (worldChunk != null)
                Registries.CLIENT_CHUNK_LIGHT_LOAD.run().onClientWorldChunkLightUpdated(world, worldChunk);
        };
    }
}
