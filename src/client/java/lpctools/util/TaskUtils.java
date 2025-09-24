package lpctools.util;

import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;

//TODO 可能需要测试一下ClientModInitializer初始化的时候MinecraftClient中的send是不是已经能用了
public class TaskUtils {
	public static void queueATaskInRenderThread(Runnable runnable){
		MinecraftClient.getInstance().send(runnable);
	}
	public static TaskKey registerTask(Runnable runnable){
		TaskKey key;
		if(reservedKeys.isEmpty()) key = new TaskKey();
		else {
			key = reservedKeys.iterator().next();
			reservedKeys.remove(key);
		}
		registeredTasks.put(key, runnable);
		return key;
	}
	public static Runnable unregisterTask(TaskKey key){
		reservedKeys.add(key);
		return registeredTasks.remove(key);
	}
	public static void markRegisteredTask(TaskKey key){
		if(queuedTasks.isEmpty()) queueATaskInRenderThread(TaskUtils::runQueuedTasks);
		queuedTasks.add(key);
	}
	public static class TaskKey{
		private final int key;
		private static int sKey = 0;
		private TaskKey(){this.key = ++sKey;}
		@Override public int hashCode() {return key;}
	}
	private static void runQueuedTasks(){
		var v = queuedTasks;
		queuedTasks = queuedTasksBackup;
		queuedTasksBackup = v;
		while(!queuedTasksBackup.isEmpty()){
			TaskKey key = queuedTasksBackup.iterator().next();
			queuedTasksBackup.remove(key);
			registeredTasks.get(key).run();
		}
	}
	private static @NotNull HashSet<TaskKey> queuedTasks = new HashSet<>();
	private static @NotNull HashSet<TaskKey> queuedTasksBackup = new HashSet<>();
	private static final HashMap<TaskKey, Runnable> registeredTasks = new HashMap<>();
	private static final HashSet<TaskKey> reservedKeys = new HashSet<>();
}
