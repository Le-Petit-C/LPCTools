package lpctools.generic;

import it.unimi.dsi.fastutil.longs.*;
import lpctools.lpcfymasaapi.Registries;
import lpctools.util.Packed;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.mutable.MutableDouble;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static lpctools.tools.ToolUtils.clearMapDataOutOfRange;

public class ChunkedTaskInstance implements AutoCloseable {
	private final Long2ObjectOpenHashMap<RunningTask> runningTasks = new Long2ObjectOpenHashMap<>();
	// 套两层，第一层用于启动运行前的准备，第二层才是CompletableFuture真正并行运行的任务。Consumer通过设置参数对象成员模拟返回。callbackStatus传入时初始为CONTINUE
	private final Long2ObjectOpenHashMap<DelayedTask> delayedTasks = new Long2ObjectOpenHashMap<>();
	
	private final double negPriority;
	
	private boolean scheduled = false;
	
	public static class DelayedCallback {
		public CallbackStatus callbackStatus; // 准备的结果
		// 套两层，第一层是CompletableFuture异步运行的任务，第二层是任务完成后需要回到主线程中进行的最后处理
		public Supplier<Supplier<CallbackStatus>> task; // CompletableFuture的任务
		public DelayedCallback() {
			this.callbackStatus = CallbackStatus.CONTINUE;
			this.task = null;
		}
	}
	
	/**
	 * 回调状态枚举，用于控制分块任务处理流程
	 */
	public enum CallbackStatus {
		/**表示当前任务已<em>成功完成</em>，移除当前任务并继续执行下一项任务*/
		CONTINUE(false, true),
		/**表示需要中断处理流程，但是当前任务已<em>成功完成</em>，不需要再次处理，故立即完全移除当前任务*/
		BREAK_FULL_COMPLETED(true, true),
		/**表示需要中断处理流程，但是当前任务<em>未完成</em>，需要保留当前任务在下次处理周期中继续执行*/
		BREAK_NOT_FULL_COMPLETED(true, false);
		public final boolean shouldBreak, isCompleted;
		CallbackStatus(boolean shouldBreak, boolean isCompleted) {
			this.shouldBreak = shouldBreak;
			this.isCompleted = isCompleted;
		}
	}
	
	public ChunkedTaskInstance() { this(0); }
	public ChunkedTaskInstance(double priority) { negPriority = -priority; }
	
	public void scheduleTask(long packedChunkPos, Consumer<DelayedCallback> delayedTask) {
		delayedTasks.put(packedChunkPos, new DelayedTask(this, packedChunkPos, new MutableDouble(), delayedTask));
		HelperClass.instance.schedule(this);
	}
	
	public void clearTasks() {
		for (var task : runningTasks.values()) task.task.cancel(true);
		runningTasks.clear();
		delayedTasks.clear();
	}
	
	public void clearTasksOutOfRange(double chunkedCamX, double chunkedCamZ, double chunkDistanceLimitSquared) {
		clearMapDataOutOfRange(chunkedCamX, chunkedCamZ, chunkDistanceLimitSquared, delayedTasks, null, null);
		clearMapDataOutOfRange(chunkedCamX, chunkedCamZ, chunkDistanceLimitSquared, runningTasks, null, task->task.task.cancel(true));
	}
	
	@Override public void close() {
		clearTasks();
	}
	
	public LongCollection delayedTaskChunkPoses() {
		return delayedTasks.keySet();
	}
	
	static <T, U extends ChunkPriorityData> PriorityQueue<U>
	nearFirstTasks(Iterable<T> vals, Function<? super T, Collection<U>> func, double chunkedCamX, double chunkedCamZ) {
		var res = new PriorityQueue<U>(Comparator.comparingDouble(data->data.distanceCache().doubleValue()));
		for(var val : vals) {
			var tasks = func.apply(val);
			for(var task : tasks) task.updatePriority(chunkedCamX, chunkedCamZ);
			res.addAll(tasks);
		}
		return res;
	}
	
	private interface ChunkPriorityData {
		long packedChunkPos();
		ChunkedTaskInstance instance();
		MutableDouble distanceCache();
		default void updatePriority(double chunkedCamX, double chunkedCamZ) {
			long packedChunkPos = packedChunkPos();
			int chunkX = Packed.ChunkPos.unpackX(packedChunkPos);
			int chunkZ = Packed.ChunkPos.unpackZ(packedChunkPos);
			distanceCache().setValue(Math.sqrt(MathHelper.square(chunkX - chunkedCamX) + MathHelper.square(chunkZ - chunkedCamZ)) + instance().negPriority);
		}
	}
	private record RunningTask(ChunkedTaskInstance instance, long packedChunkPos, MutableDouble distanceCache, CompletableFuture<Supplier<CallbackStatus>> task) implements ChunkPriorityData {
		void remove() {
			if(instance.runningTasks.get(packedChunkPos) == this)
				instance.runningTasks.remove(packedChunkPos);
		}
	}
	private record DelayedTask(ChunkedTaskInstance instance, long packedChunkPos, MutableDouble distanceCache, Consumer<DelayedCallback> taskGenerator) implements ChunkPriorityData {
		void remove() {
			if(instance.delayedTasks.get(packedChunkPos) == this)
				instance.delayedTasks.remove(packedChunkPos);
		}
	}
	
	
	private static class HelperClass implements Registries.BetweenRenderFrames {
		static final HelperClass instance = new HelperClass();
		final ArrayList<ChunkedTaskInstance> tasks = new ArrayList<>();
		
		// TODO 或许可以让这个变成可配置的
		final int runningTasksLimit = Runtime.getRuntime().availableProcessors() * 2;
		
		boolean scheduled = false;
		
		void schedule(ChunkedTaskInstance taskInstance) {
			if(taskInstance.scheduled) return;
			taskInstance.scheduled = true;
			tasks.add(taskInstance);
			if(scheduled) return;
			scheduled = true;
			Registries.BETWEEN_RENDER_FRAMES.register(this);
		}
		
		@Override public void betweenFrames() {
			scheduled = false;
			Registries.BETWEEN_RENDER_FRAMES.unregister(this);
			Vec3d camPos = MinecraftClient.getInstance().gameRenderer.getCamera().getCameraPos();
			for(ChunkedTaskInstance taskInstance : tasks) { taskInstance.scheduled = false; }
			var cachedTasks = new ArrayList<>(tasks);
			tasks.clear();
			double chunkedCamX = camPos.x / 16 - 0.5, chunkedCamZ = camPos.z / 16 - 0.5;
			if(consumeRunningTasks(chunkedCamX, chunkedCamZ, cachedTasks))
				startDelayedTasks(chunkedCamX, chunkedCamZ, cachedTasks);
			for(ChunkedTaskInstance task : cachedTasks)
				if(!task.runningTasks.isEmpty() || !task.delayedTasks.isEmpty())
					schedule(task);
		}
		
		boolean consumeRunningTasks(double chunkedCamX, double chunkedCamZ, ArrayList<ChunkedTaskInstance> tasks) {
			var runningTasks = nearFirstTasks(tasks, task->task.runningTasks.values(), chunkedCamX, chunkedCamZ);
			for (RunningTask task : runningTasks) {
				if(!task.task.isDone()) continue;
				var status = task.task.join().get();
				if(status.isCompleted) task.remove();
				if(UpdateCounter.isTired() || status.shouldBreak) return false;
			}
			return true;
		}
		void startDelayedTasks(double chunkedCamX, double chunkedCamZ, ArrayList<ChunkedTaskInstance> tasks) {
			int availableTaskCount = runningTasksLimit;
			for(var task : tasks) availableTaskCount -= task.runningTasks.size();
			if(availableTaskCount <= 0) return;
			var delayedTasks = nearFirstTasks(tasks, task->task.delayedTasks.values(), chunkedCamX, chunkedCamZ);
			var it = delayedTasks.iterator();
			DelayedCallback delayedCallback = new DelayedCallback();
			while (it.hasNext() && availableTaskCount > 0) {
				var task = it.next();
				if(task.instance.runningTasks.containsKey(task.packedChunkPos)) continue;
				delayedCallback.task = null;
				task.taskGenerator.accept(delayedCallback);
				if(delayedCallback.callbackStatus.isCompleted){
					task.remove();
					if(delayedCallback.task != null) {
						var runningTask = new RunningTask(task.instance, task.packedChunkPos, task.distanceCache, GenericUtils.supplyAsync(delayedCallback.task));
						task.instance.runningTasks.put(task.packedChunkPos, runningTask);
						--availableTaskCount;
					}
				}
				if(UpdateCounter.isTired() || delayedCallback.callbackStatus.shouldBreak) break;
			}
		}
	}
}

