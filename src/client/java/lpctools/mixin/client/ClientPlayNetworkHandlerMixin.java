package lpctools.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import lpctools.lpcfymasaapi.Registries;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ClientPacketListener.class)
public class ClientPlayNetworkHandlerMixin {
    @Shadow private ClientLevel level;
    @ModifyArg(method = "handleLevelChunkWithLight", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;queueLightUpdate(Ljava/lang/Runnable;)V"))
    Runnable inject(Runnable updater, @Local(argsOnly = true) ClientboundLevelChunkWithLightPacket packet){
        int i = packet.getX();
        int j = packet.getZ();
        return ()->{
            updater.run();
            LevelChunk worldChunk = level.getChunkSource().getChunk(i, j, false);
            if (worldChunk != null)
                Registries.CLIENT_CHUNK_LIGHT_LOAD.runner().onClientWorldChunkLightUpdated(level, worldChunk);
        };
    }
}
