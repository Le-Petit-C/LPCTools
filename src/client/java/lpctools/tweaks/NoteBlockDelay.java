package lpctools.tweaks;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import lpctools.lpcfymasaapi.Registries;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.BooleanHotkeyConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;

import java.util.Optional;
import java.util.PriorityQueue;

/**
 * 被墙上告示牌标记的音符盒，其声音额外延后「告示牌正面第一行」指定的一小段时间再播放。
 * <p>
 * 实现见 {@link DelayedSoundScheduler} 与
 * {@code lpctools.mixin.client.tweaks.noteBlockDelay.ClientLevelMixin}。
 */
public class NoteBlockDelay {
	public static final BooleanHotkeyConfig noteBlockDelay = new BooleanHotkeyConfig(TweakConfigs.tweaks, "noteBlockDelay", false, null);
	private static final double MAX_TICKS = 2.0;

	/**
	 * @return 延迟分数（单位：游戏刻）；该音符盒没有有效标记时返回 0
	 */
	public static double getDelayTicks(BlockGetter level, BlockPos noteBlockPos) {
		if(!level.getBlockState(noteBlockPos).is(Blocks.NOTE_BLOCK)) return 0;
		// 复用一个可变 BlockPos 避免每轮分配。注意 signPos 就是它本身（move 返回 this），
		// 每轮都会被重写，只能在本轮内立即消耗掉——别存下来、别跨轮使用、别传给异步逻辑
		BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
		for (Direction facing : Direction.Plane.HORIZONTAL) {
			BlockPos signPos = mutablePos.set(noteBlockPos).move(facing);
			BlockState signState = level.getBlockState(signPos);
			if (!(signState.getBlock() instanceof WallSignBlock)) continue;
			// 墙上告示牌正面朝外，所以吸附它的那一格在 FACING 的反方向
			Optional<Direction> direction = signState.getOptionalValue(WallSignBlock.FACING);
			if(direction.isEmpty() || direction.get() != facing) continue;
			if (!(level.getBlockEntity(signPos) instanceof SignBlockEntity sign)) continue;
			double ticks = parseTicks(sign.getFrontText().getMessage(0, false).getString());
			if (ticks > 0 && ticks < MAX_TICKS) return ticks;
		}
		return 0.0;
	}

	/**
	 * 解析标记文本。两种模式分工解析器：
	 * 分数模式用 {@link Ints#tryParse}（手写循环、不走正则、不抛异常，且自带溢出保护），
	 * 只有小数模式才用 {@link Doubles#tryParse}（正则实现，开销更大）。
	 * 注意 {@code tryParse} 系列<b>不像 {@code parseDouble} 那样自动 trim</b>，所以每段都要自己 trim。
	 *
	 * @return 延迟游戏刻数；解析失败或格式非法时返回 0
	 */
	private static double parseTicks(String text) {
		int slash = text.indexOf('/');
		if (slash < 0) {                                            // 小数模式：裸数字，如 "1.5"
			Double ticks = Doubles.tryParse(text.trim());
			return ticks == null ? 0.0 : ticks;
		}
		if (slash == 0 || slash == text.length() - 1) return 0.0;   // "1/"、"/2" 这类残废分数直接作废
		Integer numerator = Ints.tryParse(text.substring(0, slash).trim());
		Integer denominator = Ints.tryParse(text.substring(slash + 1).trim());
		if (numerator == null || denominator == null || denominator == 0) return 0.0;
		return (double)numerator / denominator;
	}

	public static boolean tryScheduleDelay(ClientLevel level, SoundInstance instance, double extraTicks) {
		if (!NoteBlockDelay.noteBlockDelay.getAsBoolean()) return false;
		double ticks = NoteBlockDelay.getDelayTicks(level, BlockPos.containing(instance.getX(), instance.getY(), instance.getZ()));
		if (ticks <= 0.0) return false;
		DelayedSoundScheduler.scheduleByTicks(instance, ticks + extraTicks);
		return true;
	}

	/**
	 * 按「渲染帧」精度播放被延迟的音符盒声音。
	 * <p>
	 * 为什么不直接用 vanilla 的 {@code SoundManager.playDelayed}：它的底层是
	 * {@code SoundEngine.queuedSounds}（{@code Map<SoundInstance, Integer>}，整数游戏刻），
	 * 而且只在 {@code Minecraft.tick()} 里每 50ms 推进一次，<b>表达不了「半个游戏刻」</b>这种延迟。
	 * 这里改用纳秒计时，在 {@code BETWEEN_RENDER_FRAMES}（每渲染帧一次）里检查到点，
	 * 精度约为 1 帧（120~240fps 下 4~8ms）。
	 * <p>
	 * 并用「最近若干帧周期的最小值」预测下一帧周期，把判据从 {@code due <= now}
	 * （ceil 量化，只会偏晚）放宽成 {@code due <= now + T̂/2}（round 量化）：期望误差从 T/2 降到 T/4，
	 * <b>更重要的是消掉了「被标记的音符盒统统系统性偏晚 T/2」这个偏差</b>
	 * （只有标记过的盒子有这偏差，所以它和旁边未标记盒子的相对时序本来就是错的）。
	 * 代价是声音最多可能比请求时间早半帧。
	 * <p>
	 * {@code pending} 用优先队列按到点时间排序，顺序定义在 {@code Pending#compareTo} 里（自然序）；
	 * {@code seq} 做次级比较键，保证到点时间相同的条目仍按入队顺序播放
	 * （{@code PriorityQueue} 对相等的 key 不保证顺序）。
	 * 优先队列底层同样是 {@code Object[]}，内存占用与 {@code ArrayList} 同量级。
	 */
	private static class DelayedSoundScheduler implements Registries.BetweenRenderFrames, ClientLevelEvents.AfterClientLevelChange {
		public static final double NANOS_PER_TICK = 50.0 * 1000 * 1000;
		private static final DelayedSoundScheduler INSTANCE = new DelayedSoundScheduler();
		/** 新样本在指数权重里占的比重，旧的按 (1 - 此值) 衰减 */
		private static final double FRAME_SAMPLE_WEIGHT = 0.1;

		private final PriorityQueue<Pending> pending = new PriorityQueue<>();
		/** 上次统计帧周期的时间；在 {@link #registerAll(boolean)} 里重置 */
		private long lastFrameAtNanos = 0;
		/** 帧周期估计的加权调和平均 Σw / Σ(w/x)，两项都能 O(1) 增量维护 */
		private double frameWeightSum = 0;
		private double frameInverseSum = 0;
		private boolean registered = false;
		private long nextSeq = 0;

		@Override public void afterLevelChange(@NonNull Minecraft client, @NonNull ClientLevel level) {
			pending.clear();
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
			if(registered == b) return;
			// 重新注册时把帧计时起点拉到当前：两次注册之间可能空闲了很久（队列空了就 unregister），
			// 不重置的话下一个 betweenFrames 会把「空闲时长」当成一帧周期
			if(b) lastFrameAtNanos = System.nanoTime();
			Registries.BETWEEN_RENDER_FRAMES.register(this, b);
			Registries.AFTER_CLIENT_LEVEL_CHANGE.register(this, b);
			registered = b;
		}

		static void scheduleByNanos(SoundInstance instance, long delayNanos) { INSTANCE.add(instance, delayNanos); }
		static void scheduleByTicks(SoundInstance instance, double delayTicks) { scheduleByNanos(instance, (long) (delayTicks * NANOS_PER_TICK)); }

		private void add(SoundInstance instance, long delayNanos) {
			registerAll(true);
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

		void flushSound() {
			SoundManager soundManager = Minecraft.getInstance().getSoundManager();
			long deadlineNanos = (long) (System.nanoTime() + predictedFrameNanos() * 0.5);
			Pending next;
			while ((next = pending.peek()) != null) {
				if (next.dueNanos() - deadlineNanos > 0) break;
				soundManager.play(next.instance());
				pending.poll();
			}
			if(pending.isEmpty()) registerAll(false);
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
			flushSound();
		}
	}
}
