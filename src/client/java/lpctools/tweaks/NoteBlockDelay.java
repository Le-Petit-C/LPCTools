package lpctools.tweaks;

import com.google.common.primitives.Doubles;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lpctools.generic.GenericUtils;
import lpctools.lpcfymasaapi.Registries;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.BooleanConfig;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.BooleanHotkeyConfig;
import lpctools.mixin.client.accessors.AbstractSoundInstanceAccessor;
import lpctools.util.Packed;
import lpctools.util.javaex.QuietAutoCloseable;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayDeque;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

import static lpctools.util.ChunkedUtils.*;

public class NoteBlockDelay {
	public static final BooleanHotkeyConfig noteBlockDelay = new BooleanHotkeyConfig(TweakConfigs.tweaks, "noteBlockDelay", false, null);
	public static final BooleanHotkeyConfig noteBlockPitch = new BooleanHotkeyConfig(TweakConfigs.tweaks, "noteBlockPitch", false, null);
	public static final BooleanConfig disablePitchClamp = new BooleanConfig(TweakConfigs.tweaks, "disablePitchClamp", false);
	private static final double MAX_TICKS = 2.0;

	static {
		noteBlockDelay.setValueChangeCallback(NoteBlockDelay::refreshRegistration);
		noteBlockPitch.setValueChangeCallback(NoteBlockDelay::refreshRegistration);
	}

	private static void refreshRegistration() {
		if(noteBlockDelay.getAsBoolean() || noteBlockPitch.getAsBoolean()) {
			if(DelayHandler.INSTANCE == null) {
				DelayHandler.INSTANCE = new DelayHandler();
				DelayHandler.INSTANCE.registerAll(true);
			}
		}
		else {
			if(DelayHandler.INSTANCE != null) {
				DelayHandler.INSTANCE.registerAll(false);
				DelayHandler.INSTANCE = null;
			}
		}
	}

	public static boolean tryScheduleDelay(ClientLevel level, SoundInstance instance, double extraTicks) {
		if (DelayHandler.INSTANCE == null || !NoteBlockDelay.noteBlockDelay.getAsBoolean()) return false;
		return DelayHandler.INSTANCE.tryScheduleDelay(level, instance, extraTicks);
	}

	public static void tryApplyPitch(ClientLevel level, SoundInstance instance) {
		if (DelayHandler.INSTANCE == null || !NoteBlockDelay.noteBlockPitch.getAsBoolean()) return;
		DelayHandler.INSTANCE.tryApplyPitch(level, instance);
	}

	private static double parseNumber(String text) {
		String trimmed = text.trim();
		try {
			return Fraction.getFraction(trimmed).doubleValue();
		} catch (NumberFormatException | ArithmeticException _) {
			Double value = Doubles.tryParse(trimmed);
			return value == null ? 0.0 : value;
		}
	}

	private static class DelayHandler implements
		Registries.BetweenRenderFrames,
		ClientLevelEvents.AfterClientLevelChange,
		Registries.ClientWorldChunkSetBlockState,
		ClientChunkEvents.Unload,
		ClientChunkEvents.Load,
		QuietAutoCloseable {
		public static final double NANOS_PER_TICK = 50.0 * 1000 * 1000;
		private static @Nullable DelayHandler INSTANCE;

		/** 新样本在指数权重里占的比重，旧的按 (1 - 此值) 衰减 */
		private static final double FRAME_SAMPLE_WEIGHT = 0.1;

		private final PriorityQueue<Pending> pending = new PriorityQueue<>();
		private long lastFrameAtNanos = System.nanoTime();

		private double frameWeightSum = 0;
		private double frameInverseSum = 0;
		private long nextSeq = 0;

		private long nextTaskSeq = 0;

		@Override public void close() {
			registerAll(false);
			SoundManager soundManager = Minecraft.getInstance().getSoundManager();
			long nowNanos = System.nanoTime();
			while (pending.poll() instanceof Pending instance) {
				int ticks = (int) Math.round((instance.dueNanos - nowNanos) / NANOS_PER_TICK);
				if(ticks <= 0) soundManager.play(instance.instance);
				else soundManager.playDelayed(instance.instance, ticks);
			}
		}

		private record NoteMark(double delayTicks, double pitchExponent) {
			static final NoteMark NONE = new NoteMark(0.0, 0.0);
			NoteMark max(NoteMark other) { return new NoteMark(Math.max(delayTicks, other.delayTicks), Math.max(pitchExponent, other.pitchExponent)); }
		}

		private record SignMark(Direction direction, NoteMark mark) {}

		private final Long2ObjectOpenHashMap<Int2ObjectOpenHashMap<NoteMark>> markCache = new Long2ObjectOpenHashMap<>();
		private final Object2ObjectOpenHashMap<ChunkPos, ChunkAsyncLoadTask> chunkLoadTasks = new Object2ObjectOpenHashMap<>();
		private final ConcurrentLinkedQueue<ChunkAsyncLoadResult> completedChunkLoadTasks = new ConcurrentLinkedQueue<>();

		boolean tryScheduleDelay(ClientLevel level, SoundInstance instance, double extraTicks) {
			double ticks = getMark(level, soundPos(instance)).delayTicks();
			if (ticks <= 0.0) return false;
			scheduleByTicks(instance, ticks + extraTicks);
			return true;
		}

		void tryApplyPitch(ClientLevel level, SoundInstance instance) {
			// 先判类型再取标记：非 AbstractSoundInstance 的实现（别的 mod）连访问器都转不过去，
			// 而取标记可能触发一次扫描
			if (!(instance instanceof AbstractSoundInstanceAccessor accessor)) return;
			double exponent = getMark(level, soundPos(instance)).pitchExponent();
			if (exponent == 0.0) return;
			accessor.lpctools$setPitch(accessor.lpctools$getPitch() * (float)Math.pow(2.0, exponent));
		}

		private static BlockPos soundPos(SoundInstance instance) {
			return BlockPos.containing(instance.getX(), instance.getY(), instance.getZ());
		}

		NoteMark getMark(BlockGetter level, BlockPos noteBlockPos) {
			if(!level.getBlockState(noteBlockPos).is(Blocks.NOTE_BLOCK)) return NoteMark.NONE;
			NoteMark cached = chunkedGetOrDefault(markCache, noteBlockPos, null);
			if(cached != null) return cached;

			ObjectOpenHashSet<BlockPos> queuedPoses = new ObjectOpenHashSet<>();
			ArrayDeque<BlockPos> posesNeedCheck = new ArrayDeque<>();
			Object2ObjectOpenHashMap<BlockPos, SignMark> signMarks = new Object2ObjectOpenHashMap<>();
			queuedPoses.add(noteBlockPos);
			posesNeedCheck.add(noteBlockPos);
			// 复用可变 BlockPos 避免每轮分配。nearbyPos 就是 mutablePos 本身（move 返回 this），
			// 每轮都会被重写，只能在本轮内立即消耗（getBlockState / getBlockEntity）
			BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
			while (posesNeedCheck.poll() instanceof BlockPos pos) {
				for (Direction facing : Direction.Plane.HORIZONTAL) {
					BlockPos nearbyPos = mutablePos.set(pos).move(facing);
					BlockState nearbyState = level.getBlockState(nearbyPos);
					switch (nearbyState.getBlock()) {
						case WallSignBlock ignored -> {
							// 牌面朝外，而吸附牌的方块在 FACING 的反方向（WallSignBlock.canSurvive），
							// 所以 direction.get() == facing 等价于「这一格正是牌的支撑方块」——
							// 也就是牌必须是贴在这一串音符盒上的
							Optional<Direction> direction = nearbyState.getOptionalValue(WallSignBlock.FACING);
							if(direction.isEmpty() || direction.get() != facing) continue;
							if (!(level.getBlockEntity(nearbyPos) instanceof SignBlockEntity sign)) continue;
							// 第一行 = 延迟刻数，第二行 = 音高指数，两行各写各的，数字语法完全一样
							double ticks = parseNumber(sign.getFrontText().getMessage(0, false).getString());
							double exponent = parseNumber(sign.getFrontText().getMessage(1, false).getString());
							if (!(ticks > 0 && ticks < MAX_TICKS)) ticks = 0;
							if (!Double.isFinite(exponent)) exponent = 0;
							if (ticks != 0 || exponent != 0) signMarks.put(nearbyPos.immutable(), new SignMark(facing, new NoteMark(ticks, exponent)));
						}
						case NoteBlock ignored -> {
							if (!queuedPoses.contains(nearbyPos)) {
								BlockPos fixed = nearbyPos.immutable();
								queuedPoses.add(fixed);
								posesNeedCheck.add(fixed);
							}
						}
						default -> {}
					}
				}
			}
			for(var entry : signMarks.entrySet()) {
				mutablePos.set(entry.getKey());
				// 牌面朝外，所以「朝牌所贴的方块」走要用 FACING 的反方向；第一步正好是支撑方块本身
				Direction searchDirection = entry.getValue().direction().getOpposite();
				NoteMark mark = entry.getValue().mark();
				// 从所贴的那一格（射线起点）开始逐格前进，覆盖沿射线连续的一段；走出连通组即停
				while (queuedPoses.contains(mutablePos.move(searchDirection)))
					chunkedComputeWithDefault(markCache, mutablePos, (_, old) -> old == null ? mark : old.max(mark), null);
			}
			for(BlockPos pos : queuedPoses) chunkedPutIfAbsent(markCache, pos, NoteMark.NONE);
			return chunkedGetOrDefault(markCache, noteBlockPos, NoteMark.NONE);
		}

		@Override public void afterLevelChange(@NonNull Minecraft client, @NonNull ClientLevel level) {
			pending.clear();
			markCache.clear();
		}

		@Override public void onClientWorldChunkSetBlockState(LevelChunk chunk, BlockPos pos, @Nullable BlockState lastState, @Nullable BlockState newState)
		{ removeConnectedPos(pos); }

		@Override public void onChunkUnload(@NonNull ClientLevel level, @NonNull LevelChunk chunk) {
			// 先把整张 map 从 delayCache 里摘出来再迭代：下面的 removeConnectedPos 会删 delayCache 的元素，
			// 边迭代边删会漏掉部分 key（fastutil 的迭代器不是 fail-fast，不抛异常，只会静默漏）
			Int2ObjectOpenHashMap<NoteMark> map = markCache.remove(chunk.getPos().pack());
			if(map == null) return;
			var it = map.keySet().iterator();
			ChunkPos chunkPos = chunk.getPos();
			BlockPos.MutableBlockPos mutable1 = new BlockPos.MutableBlockPos();
			BlockPos.MutableBlockPos mutable2 = new BlockPos.MutableBlockPos();
			int chunkStartX = chunkPos.x() * 16, chunkStartZ = chunkPos.z() * 16;
			// 连通组可能跨区块：本区块卸载后，邻区块里与它相邻的那些位置的组会变，也要一并失效
			// （方向与 onChunkLoad 的边境扫描正好对称）
			while (it.hasNext()) {
				int chunkLocal = it.nextInt();
				Packed.unpackChunkLocal(chunkLocal, mutable1);
				if(mutable1.getX() == 0) removeConnectedPos(mutable2.set(mutable1.move(chunkStartX - 1, 0, chunkStartZ)));
				if(mutable1.getX() == 15) removeConnectedPos(mutable2.set(mutable1.move(chunkStartX + 1, 0, chunkStartZ)));
				if(mutable1.getZ() == 0) removeConnectedPos(mutable2.set(mutable1.move(chunkStartX, 0, chunkStartZ - 1)));
				if(mutable1.getZ() == 15) removeConnectedPos(mutable2.set(mutable1.move(chunkStartX, 0, chunkStartZ + 1)));
			}
		}

		@Override public void onChunkLoad(@NonNull ClientLevel level, @NonNull LevelChunk chunk) {
			// 同一区块可能被反复加载：旧任务直接取消，并用递增的 taskSeq 保证只有最新那次的结果被采纳
			long seq = nextTaskSeq++;
			chunkLoadTasks.compute(chunk.getPos(), (_, old) -> computeNewTask(old, seq, chunk));
		}

		private ChunkAsyncLoadTask computeNewTask(@Nullable ChunkAsyncLoadTask oldTask, long seq, LevelChunk chunk) {
			if(oldTask != null) oldTask.cancellableTask.cancel(false);
			return new ChunkAsyncLoadTask(seq, GenericUtils.runAsync(()-> asyncChunkLoad(seq, chunk, completedChunkLoadTasks)));
		}

		private record ChunkAsyncLoadTask(long taskSeq, CompletableFuture<Void> cancellableTask) {}
		private record ChunkAsyncLoadResult(long taskSeq, LevelChunk chunk, ObjectOpenHashSet<BlockPos> blockPosesToInvalidate) {}

		private void asyncChunkLoad(long taskSeq, LevelChunk chunk, ConcurrentLinkedQueue<ChunkAsyncLoadResult> uploadQueue) {
			// 只扫 4 个边界面：只有贴着区块边界、可能和邻区块音符盒相连的位置才需要让邻区块失效。
			// 注意这里是在异步线程上读 LevelChunk，与网格烘焙属于同一类做法
			ObjectOpenHashSet<BlockPos> set = new ObjectOpenHashSet<>();
			int top = chunk.getMinY() + chunk.getHeight();
			BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
			for(int y = chunk.getMinY(); y < top; ++y) {
				for(int a = 0; a < 16; ++a) {
					if(chunk.getBlockState(pos.set(a, y, 0)).is(Blocks.NOTE_BLOCK)) set.add(pos.offset(0, 0, -1));
					if(chunk.getBlockState(pos.set(a, y, 15)).is(Blocks.NOTE_BLOCK)) set.add(pos.offset(0, 0, 1));
					if(chunk.getBlockState(pos.set(0, y, a)).is(Blocks.NOTE_BLOCK)) set.add(pos.offset(-1, 0, 0));
					if(chunk.getBlockState(pos.set(15, y, a)).is(Blocks.NOTE_BLOCK)) set.add(pos.offset(1, 0, 0));
				}
			}
			uploadQueue.add(new ChunkAsyncLoadResult(taskSeq, chunk, set));
		}

		/**
		 * 从 {@code start} 出发，沿<b>缓存里存在的</b>连通组把记录删掉（不在缓存里的位置直接停下）。
		 * 只删不重算，下次 {@link #getMark} 会按需重建。
		 */
		private void removeConnectedPos(BlockPos start) {
			BlockPos pos = start;
			ArrayDeque<BlockPos> removeQueue = null;
			while (pos != null) {
				// 值永远非 null，所以「删到了东西」直接看返回值就行（值对象版没法像 double 那样
				// 再写一个同名的 chunkedRemoveKeyWithSuccess：泛型擦除后与 double 版签名相同）
				if(chunkedRemoveKey(markCache, pos.getX(), pos.getY(), pos.getZ()) != null) {
					if(removeQueue == null) removeQueue = new ArrayDeque<>();
					for(Direction direction : Direction.Plane.HORIZONTAL) removeQueue.add(pos.relative(direction));
				}
				if(removeQueue == null) break;
				pos = removeQueue.poll();
			}
		}

		private record Pending(SoundInstance instance, long dueNanos, long seq) implements Comparable<Pending> {
			@Override public int compareTo(@NonNull Pending o) {
				// 故意用差值比较而不是 Long.compare(a, b)：nanoTime() 起点任意、理论上会回绕，
				// 而队列里两条 dueNanos 的跨度只有 ≤100ms（远小于 2^63），差值比较在回绕后仍然正确，
				// 直接比较绝对大小则不然。别"顺手修"回去。
				int c1 = Long.compare(dueNanos - o.dueNanos, 0);
				if(c1 != 0) return c1;
				else return Long.compare(seq - o.seq, 0);
			}
		}

		void registerAll(boolean b) {
			Registries.BETWEEN_RENDER_FRAMES.register(this, b);
			Registries.AFTER_CLIENT_LEVEL_CHANGE.register(this, b);
			Registries.CLIENT_WORLD_CHUNK_SET_BLOCK_STATE.register(this, b);
			Registries.CLIENT_CHUNK_LOAD.register(this, b);
			Registries.CLIENT_CHUNK_UNLOAD.register(this, b);
		}

		void scheduleByNanos(SoundInstance instance, long delayNanos) { addSound(instance, delayNanos); }
		void scheduleByTicks(SoundInstance instance, double delayTicks) { scheduleByNanos(instance, (long) (delayTicks * NANOS_PER_TICK)); }

		private void addSound(SoundInstance instance, long delayNanos) {
			pending.add(new Pending(instance, System.nanoTime() + delayNanos, nextSeq++));
			flushSound();
		}

		private double predictedFrameNanos() {
			if (frameWeightSum <= 0.0 || frameInverseSum <= 0.0) return 0.0;
			return frameWeightSum / frameInverseSum;
		}

		void flushSound() {
			SoundManager soundManager = Minecraft.getInstance().getSoundManager();
			long deadlineNanos = (long) (System.nanoTime() + predictedFrameNanos() * 0.5);
			Pending next;
			while ((next = pending.peek()) != null) {
				if (next.dueNanos() - deadlineNanos > 0) break;
				soundManager.play(next.instance());
				pending.poll();
			}
		}

		@Override public void betweenFrames() {
			long now = System.nanoTime();
			long periodNanos = now - lastFrameAtNanos;
			// periodNanos <= 0 必须跳过：x 为 0 会让 Σ(w/x) 变成 +Infinity，
			// 而 Infinity * 权重 仍是 Infinity，估计值会【永久】变成 0（提前窗口恒 0，静默退化成只晚不早）
			if (periodNanos > 0) {
				double keep = 1.0 - FRAME_SAMPLE_WEIGHT;
				frameWeightSum = frameWeightSum * keep + FRAME_SAMPLE_WEIGHT;
				frameInverseSum = frameInverseSum * keep + FRAME_SAMPLE_WEIGHT / periodNanos;
			}
			lastFrameAtNanos = now;
			// 每帧最多消费 16 个，避免传送后成批失效卡住一帧；poll 先出队，
			// 过期的结果（taskSeq 对不上）被丢弃是对的，代价是它会让本轮提前结束、后面的多等一帧
			int budget = 16;
			while (budget > 0 && completedChunkLoadTasks.poll() instanceof ChunkAsyncLoadResult(
				long taskSeq, LevelChunk chunk, ObjectOpenHashSet<BlockPos> blockPosesToInvalidate
			) && chunkLoadTasks.get(chunk.getPos()) instanceof ChunkAsyncLoadTask task && task.taskSeq == taskSeq
			) {
				--budget;
				blockPosesToInvalidate.forEach(this::removeConnectedPos);
				chunkLoadTasks.remove(chunk.getPos());
			}
			flushSound();
		}
	}
}
