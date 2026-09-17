package lpctools.util.mixin;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import lpctools.mixin.client.accessors.BlockStatePredictionHandlerAccessor;
import lpctools.mixin.client.accessors.ClientLevelAccessor;
import lpctools.util.javaex.QuietAutoCloseable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.network.protocol.Packet;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class PacketRecorder implements QuietAutoCloseable {
	private static @Nullable PacketRecorder recorder = null;
	private final PacketRecorder parent;
	private final ArrayList<Packet<?>> interceptedPackets;
	private final int startPosition;
	/** 开始拦截时的方块状态预测快照；predictionHandler 为 null 表示当时不在世界里 */
	private final @Nullable PredictRollback rollback;

	public static PacketRecorder startInterceptedPackets() {
		return recorder = new PacketRecorder(recorder);
	}

	/**
	 * 丢弃本窗口拦截到的数据包，并把方块状态预测的记账一并回滚到开始拦截之前。<br>
	 * 原因：被丢掉的包永远到不了服务器，那些操作开的预测就永远等不到 ack；
	 * 而预测期间 ClientLevel.setBlock 会把“预测前的服务器状态”按当前序列号记进
	 * serverVerifiedStates，并且在该位置对应的记录被 ack 清掉之前，该位置收到的
	 * 服务器方块变化数据包会被扣住不应用（ClientLevel.setServerVerifiedBlockState
	 * → updateKnownServerState 返回 true）。不回滚的话，这些残留记录会一直扣住
	 * 该位置的方块更新，直到下次真的有操作被 ack 才同步。<br>
	 * 被 close() 重新发出去的包不受影响：那条路径不会调 clear()，预测完全按原版走。
	 */
	public void clear() {
		while (interceptedPackets.size() > startPosition) interceptedPackets.removeLast();
		if (rollback != null) rollback.rollbackPredictionState();
	}
	@Override public void close() {
		if(recorder != this) throw new IllegalStateException();
		recorder = parent;
		if(parent == null) {
			var networkHandler = Minecraft.getInstance().getConnection();
			if(networkHandler == null) return;
			for(var packet : interceptedPackets)
				networkHandler.send(packet);
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
		var level = Minecraft.getInstance().level;
		if(level == null) rollback = null;
		else rollback = new PredictRollback(((ClientLevelAccessor)level).invokeGetBlockStatePredictionHandler());
	}

	private static class PredictRollback {
		private final BlockStatePredictionHandlerAccessor handler;
		private final int sequenceBeforeIntercept;
		private final boolean predictingBeforeIntercept;
		private final MapBackup<?> retainedStatesBeforeIntercept;
		PredictRollback(BlockStatePredictionHandler predictionHandler) {
			this.handler = (BlockStatePredictionHandlerAccessor)predictionHandler;
			sequenceBeforeIntercept = handler.getCurrentSequenceNr();
			predictingBeforeIntercept = handler.getPredicting();
			retainedStatesBeforeIntercept = new MapBackup<>(handler.getServerVerifiedStates());
		}
		void rollbackPredictionState() {
			handler.setCurrentSequenceNr(sequenceBeforeIntercept);
			handler.setPredicting(predictingBeforeIntercept);
			retainedStatesBeforeIntercept.restore();
		}
	}

	private static class MapBackup<T> {
		private final Long2ObjectOpenHashMap<T> source, backup;
		private MapBackup(Long2ObjectOpenHashMap<T> source) {
			this.source = source;
			this.backup = new Long2ObjectOpenHashMap<>(source);
		}
		private void restore() {
			source.clear();
			source.putAll(backup);
		}
	}
}
