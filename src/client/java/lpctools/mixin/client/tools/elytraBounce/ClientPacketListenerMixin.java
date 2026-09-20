package lpctools.mixin.client.tools.elytraBounce;

import lpctools.tools.elytraBounce.ElytraBounce;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static lpctools.tools.elytraBounce.ElytraBounce.tryRestoreGlide;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
	@Inject(method = "handleSetEntityData", at = @At("HEAD"), cancellable = true)
	void handleSetEntityDataHead(ClientboundSetEntityDataPacket packet, CallbackInfo ci) {
		if(ElytraBounce.handlePacketOnHead((ClientPacketListener)(Object)this, packet)) ci.cancel();
	}
	@Inject(method = "handleSetEntityData", at = @At("TAIL"))
	void handleSetEntityDataTail(ClientboundSetEntityDataPacket packet, CallbackInfo ci) {
		tryRestoreGlide(packet);
	}
}
