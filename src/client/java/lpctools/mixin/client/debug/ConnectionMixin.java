package lpctools.mixin.client.debug;

import io.netty.channel.ChannelFutureListener;
import lpctools.debugs.DebugConfigs;
import lpctools.util.DataUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class ConnectionMixin {
	@Inject(method = "sendPacket", at = @At("HEAD"))
	void onSendPacketHead(Packet<?> packet, @Nullable ChannelFutureListener listener, boolean flush, CallbackInfo ci) {
		if(Minecraft.getInstance().isSameThread() && DebugConfigs.displayPacketNames.getBooleanValue()) {
			String name = packet.getClass().getSimpleName();
			if(!DebugConfigs.cachedIgnoredPacketNames.get().contains(name))
				DataUtils.clientMessage(name, false);
		}
	}
}
