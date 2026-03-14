package lpctools.util.mixin;

import lpctools.util.javaex.QuietAutoCloseable;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.Packet;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class PacketRecorder implements QuietAutoCloseable {
	private static @Nullable PacketRecorder recorder = null;
	private final PacketRecorder parent;
	private final ArrayList<Packet<?>> interceptedPackets;
	private final int startPosition;
	
	public static PacketRecorder startInterceptedPackets() {
		return recorder = new PacketRecorder(recorder);
	}
	
	public void clear() {
		while (interceptedPackets.size() > startPosition) interceptedPackets.removeLast();
	}
	@Override public void close() {
		if(recorder != this) throw new IllegalStateException();
		recorder = parent;
		if(parent == null) {
			var networkHandler = MinecraftClient.getInstance().getNetworkHandler();
			if(networkHandler == null) return;
			for(var packet : interceptedPackets)
				networkHandler.sendPacket(packet);
		}
	}
	
	public static boolean tryRecordPacket(Packet<?> packet) {
		if(recorder == null) return false;
		return recorder.interceptedPackets.add(packet);
	}
	
	private PacketRecorder(@Nullable PacketRecorder parent) {
		this.parent = parent;
		if(parent == null) interceptedPackets = new ArrayList<>();
		else interceptedPackets = parent.interceptedPackets;
		startPosition = interceptedPackets.size();
	}
}
