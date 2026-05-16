package lpctools.mixin.client.utils;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class PacketRecorder {
	@Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), cancellable = true)
	void onSend(Packet<?> packet, CallbackInfo ci) {
		if(lpctools.util.mixin.PacketRecorder.tryRecordPacket(packet)) ci.cancel();
	}
}
