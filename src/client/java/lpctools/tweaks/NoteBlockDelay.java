package lpctools.tweaks;

import com.google.common.primitives.Doubles;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lpctools.generic.GenericUtils;
import lpctools.lpcfymasaapi.Registries;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.BooleanHotkeyConfig;
import lpctools.util.Packed;
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

/**
 * 墙上告示牌会给它<b>贴着的那一串音符盒</b>加一点额外延迟：这段音符盒的声音会晚
 * 「告示牌正面第一行」写的时间再播放。
 * <p>
 * 标记范围是<b>牌背面射线方向连续的一段音符盒</b>，起点是牌所贴的那一格（牌的支撑方块）。
 * 所以牌要贴在串上（贴着其中某个音符盒的侧面），且射线是直的：串拐弯、或牌贴在行侧面
 * （垂直于行）时，只标得到它贴着的那一个音符盒为止。
 * <p>
 * 一行文本两种写法，单位都是游戏刻：{@code 分子/分母}（如 {@code 1/2}，分子分母必须为整数）
 * 或裸数字（如 {@code 1.5}）；结果 &lt;= 0 或 &gt;= {@link #MAX_TICKS} 视为无标记。
 * 同一个音符盒被多张牌覆盖到时取<b>最大</b>的那个值。
 * <p>
 * 实现见 {@link DelayHandler} 与
 * {@code lpctools.mixin.client.tweaks.noteBlockDelay.ClientLevelMixin}。
 */
public class NoteBlockDelay {
	public static final BooleanHotkeyConfig noteBlockDelay = new BooleanHotkeyConfig(TweakConfigs.tweaks, "noteBlockDelay", false, null);
	private static final double MAX_TICKS = 2.0;

	static { noteBlockDelay.setValueChangeCallback(()->DelayHandler.INSTANCE.registerAll(noteBlockDelay.getBooleanValue())); }

	public static boolean tryScheduleDelay(ClientLevel level, SoundInstance instance, double extraTicks) {
		if (!NoteBlockDelay.noteBlockDelay.getAsBoolean()) return false;
		return DelayHandler.INSTANCE.tryScheduleDelay(level, instance, extraTicks);
	}

	/**
	 * 解析标记文本：优先交给 commons-lang3 的 {@link Fraction#getFraction(String)}，
	 * 分数、小数、整数都能吃，且自带约分与分母 0 / 整数溢出的检查。
	 * 它不认的写法（科学计数法等）再退回 {@link Doubles#tryParse}（正则实现，开销更大）。
	 * 两者都<b>不会自己 trim</b>，所以先在这里 trim 一次。
	 *
	 * @return 延迟游戏刻数；解析失败或格式非法时返回 0
	 */
	private static double parseTicks(String text) {
		String trimmed = text.trim();
		try {
			return Fraction.getFraction(trimmed).doubleValue();
		} catch (NumberFormatException | ArithmeticException _) {
			Double ticks = Doubles.tryParse(trimmed);
			return ticks == null ? 0.0 : ticks;
		}
	}

	private static class DelayHandler implements Registries.BetweenRenderFrames, ClientLevelEvents.AfterClientLevelChange, Registries.ClientWorldChunkSetBlockState, ClientChunkEvents.Unload, ClientChunkEvents.Load {
		public static final double NANOS_PER_TICK = 50.0 * 1000 * 1000;
		private static final DelayHandler INSTANCE = new DelayHandler();
		/** 新样本在指数权重里占的比重，旧的按 (1 - 此值) 衰减 */
		private static final double FRAME_SAMPLE_WEIGHT = 0.1;

		private final PriorityQueue<Pending> pending = new PriorityQueue<>();
		/** 上次统计帧周期的时间；在 {@link #registerAll(boolean)} 里重置 */
		private long lastFrameAtNanos = 0;
		/** 帧周期估计的加权调和平均 Σw / Σ(w/x)，两项都能 O(1) 增量维护 */
		private double frameWeightSum = 0;
		private double frameInverseSum = 0;
		private long nextSeq = 0;

		private long nextTaskSeq = 0;
		private final Long2ObjectOpenHashMap<Int2DoubleOpenHashMap> delayCache = new Long2ObjectOpenHashMap<>();
		private final Object2ObjectOpenHashMap<ChunkPos, ChunkAsyncLoadTask> chunkLoadTasks = new Object2ObjectOpenHashMap<>();
		/** 惰性创建：异步任务会捕获创建时那个实例，见 {@link #computeNewTask} */
		private @Nullable ConcurrentLinkedQueue<ChunkAsyncLoadResult> completedChunkLoadTasks = null;

		boolean tryScheduleDelay(ClientLevel level, SoundInstance instance, double extraTicks) {
			double ticks = getDelayTicks(level, BlockPos.containing(instance.getX(), instance.getY(), instance.getZ()));
			if (ticks <= 0.0) return false;
			DelayHandler.scheduleByTicks(instance, ticks + extraTicks);
			return true;
		}

		/**
		 * 算出这个音符盒该额外延迟多少刻。
		 * <p>
		 * 先沿水平方向 BFS 出它所在的连通组（水平相邻的音符盒算一组），并顺手收集贴着这个组的
		 * 墙壁告示牌；再让每张牌从自己的位置沿 {@code FACING.getOpposite()}（也就是朝着牌所贴的
		 * 方块）逐格前进，把沿途仍属于该连通组的位置写成 {@code max(已有值, 牌上的值)}，
		 * 这样结果与遍历顺序无关。
		 * <p>
		 * 射线第一步必定是牌所贴的那个音符盒（牌只认自己贴着的那一格），所以贴着组就能标到东西。
		 * 判据是「这一格在不在 {@code queuedPoses} 里」，而 {@code queuedPoses} 里只有音符盒、
		 * <b>不含它们的邻居</b>：这样射线覆盖的正好是沿射线方向连续的一段音符盒。
		 * 别顺手把邻居也放进去——那会让射线跨过空隙继续标下去。
		 * <p>
		 * 结果按「区块 + 区块内局部坐标」缓存：组内每个位置都留一条记录，没被射线覆盖到的写 0。
		 *
		 * @return 延迟游戏刻数；没有有效标记时返回 0
		 */
		double getDelayTicks(BlockGetter level, BlockPos noteBlockPos) {
			if(!level.getBlockState(noteBlockPos).is(Blocks.NOTE_BLOCK)) return 0;
			double tempRes = chunkedGetOrDefault(delayCache, noteBlockPos, -1);
			if(tempRes >= 0) return tempRes;

			record SignValue(Direction direction, double delayTicks){}

			ObjectOpenHashSet<BlockPos> queuedPoses = new ObjectOpenHashSet<>();
			ArrayDeque<BlockPos> posesNeedCheck = new ArrayDeque<>();
			Object2ObjectOpenHashMap<BlockPos, SignValue> signValues = new Object2ObjectOpenHashMap<>();
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
							double ticks = parseTicks(sign.getFrontText().getMessage(0, false).getString());
							if (ticks > 0 && ticks < MAX_TICKS) signValues.put(nearbyPos.immutable(), new SignValue(facing, ticks));
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
			for(var entry : signValues.entrySet()) {
				mutablePos.set(entry.getKey());
				// 牌面朝外，所以「朝牌所贴的方块」走要用 FACING 的反方向；第一步正好是支撑方块本身
				Direction searchDirection = entry.getValue().direction.getOpposite();
				double delay = entry.getValue().delayTicks;
				// 从所贴的那一格（射线起点）开始逐格前进，覆盖沿射线连续的一段；走出连通组即停
				while (queuedPoses.contains(mutablePos.move(searchDirection)))
					chunkedComputeWithDefault(delayCache, mutablePos, (_, v) -> Math.max(v, delay), 0);
			}
			for(BlockPos pos : queuedPoses) chunkedPutIfAbsent(delayCache, pos, 0);
			return chunkedGetOrDefault(delayCache, noteBlockPos, -1);
		}

		@Override public void afterLevelChange(@NonNull Minecraft client, @NonNull ClientLevel level) {
			pending.clear();
			delayCache.clear();
		}

		@Override public void onClientWorldChunkSetBlockState(LevelChunk chunk, BlockPos pos, @Nullable BlockState lastState, @Nullable BlockState newState)
		{ removeConnectedPos(pos); }

		@Override public void onChunkUnload(@NonNull ClientLevel level, @NonNull LevelChunk chunk) {
			// 先把整张 map 从 delayCache 里摘出来再迭代：下面的 removeConnectedPos 会删 delayCache 的元素，
			// 边迭代边删会漏掉部分 key（fastutil 的迭代器不是 fail-fast，不抛异常，只会静默漏）
			Int2DoubleOpenHashMap map = delayCache.remove(chunk.getPos().pack());
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
			if(completedChunkLoadTasks == null) completedChunkLoadTasks = new ConcurrentLinkedQueue<>();
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
		 * 只删不重算，下次 {@link #getDelayTicks} 会按需重建。
		 */
		private void removeConnectedPos(BlockPos start) {
			BlockPos pos = start;
			ArrayDeque<BlockPos> removeQueue = null;
			while (pos != null) {
				if(chunkedRemoveKeyWithSuccess(delayCache, pos)) {
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
			// 启用时把帧计时起点拉到当前：上次禁用可能已过去很久，
			// 不重置的话启用后第一个 betweenFrames 会把「禁用期间的时长」当成一帧周期
			if(b) lastFrameAtNanos = System.nanoTime();
			else {
				// 禁用后下面这些钩子都不在跑，缓存不会再被失效，直接丢掉让它按需重建
				delayCache.clear();
				chunkLoadTasks.values().forEach(task -> task.cancellableTask.cancel(false));
				chunkLoadTasks.clear();
				completedChunkLoadTasks = null;
				frameWeightSum = 0;
				frameInverseSum = 0;
				SoundManager soundManager = Minecraft.getInstance().getSoundManager();
				long nowNanos = System.nanoTime();
				while (pending.poll() instanceof Pending instance) {
					int ticks = (int) Math.round((instance.dueNanos - nowNanos) / NANOS_PER_TICK);
					if(ticks <= 0) soundManager.play(instance.instance);
					else soundManager.playDelayed(instance.instance, ticks);
				}
			}
			Registries.BETWEEN_RENDER_FRAMES.register(this, b);
			Registries.AFTER_CLIENT_LEVEL_CHANGE.register(this, b);
			Registries.CLIENT_WORLD_CHUNK_SET_BLOCK_STATE.register(this, b);
			Registries.CLIENT_CHUNK_LOAD.register(this, b);
			Registries.CLIENT_CHUNK_UNLOAD.register(this, b);
		}

		static void scheduleByNanos(SoundInstance instance, long delayNanos) { INSTANCE.addSound(instance, delayNanos); }
		static void scheduleByTicks(SoundInstance instance, double delayTicks) { scheduleByNanos(instance, (long) (delayTicks * NANOS_PER_TICK)); }

		private void addSound(SoundInstance instance, long delayNanos) {
			pending.add(new Pending(instance, System.nanoTime() + delayNanos, nextSeq++));
			flushSound();
		}

		/**
		 * 帧周期的加权<b>调和</b>平均 {@code Σw / Σ(w/x)}，用作下一帧周期的预测值。
		 * <p>
		 * 用调和平均而不是算术平均：{@code x} 越大贡献越小，一次卡顿（进世界/GC/区块生成）
		 * 几乎不会把估计顶上去（16.7ms 的稳态遇到一帧 200ms 只涨到 ~18ms，而算术平均会涨到 ~35ms）；
		 * 又由 AM–HM 不等式恒有 {@code 调和平均 <= 算术平均}，且稳态下两者相等——
		 * 即帧率越稳越不保守，一有波动就自动缩回「偏晚」那一侧。
		 * <p>
		 * 还没采到有效样本时返回 0，提前窗口为 0，判据退化成 {@code due <= now}（只晚不早）。
		 */
		private double predictedFrameNanos() {
			if (frameWeightSum <= 0.0 || frameInverseSum <= 0.0) return 0.0;
			return frameWeightSum / frameInverseSum;
		}

		/** 播掉所有已到点（含提前窗口）的待播声音；窗口见 {@link #predictedFrameNanos()} */
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
			while (budget > 0
				&& completedChunkLoadTasks != null
				&& completedChunkLoadTasks.poll() instanceof ChunkAsyncLoadResult(long taskSeq, LevelChunk chunk, ObjectOpenHashSet<BlockPos> blockPosesToInvalidate)
				&& chunkLoadTasks.get(chunk.getPos()) instanceof ChunkAsyncLoadTask task
				&& task.taskSeq == taskSeq
			) {
				--budget;
				blockPosesToInvalidate.forEach(this::removeConnectedPos);
				chunkLoadTasks.remove(chunk.getPos());
			}
			flushSound();
		}
	}
}
