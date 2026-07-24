package lpctools.util.inGame;

import lpctools.lpcfymasaapi.Registries;
import lpctools.util.javaex.QuietAutoCloseable;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import org.jspecify.annotations.NonNull;

import java.util.ArrayDeque;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class ClientTickExecutor implements ClientTickEvents.EndTick, QuietAutoCloseable, Executor {
	public void schedule(Consumer<InGameManager> task) { operations.add(task); }
	public void schedule(Runnable task) { schedule(_->task.run()); }
	@Override public void execute(@NonNull Runnable command) { schedule(command); }
	public boolean isEmpty() { return operations.isEmpty(); }
	public boolean hasScheduled() { return !isEmpty(); }

	private final ArrayDeque<Consumer<InGameManager>> operations = new ArrayDeque<>();

	private final float tickSpeed;
	private float tickCounter = 0;

	public ClientTickExecutor(float tickSpeed) {
		this.tickSpeed = tickSpeed;
		Registries.END_CLIENT_TICK.register(this);
	}
	public ClientTickExecutor() { this(1); }

	@Override public void onEndTick(@NonNull Minecraft client) {
		if(client.isPaused()) return;
		InGameManager manager = InGameManager.get(client);
		if(manager == null) operations.clear();
		else {
			tickCounter += tickSpeed;
			while (tickCounter >= 1 && !operations.isEmpty()) {
				operations.poll().accept(manager);
				--tickCounter;
			}
		}
		if(tickCounter > 1) tickCounter = Math.max(0, 1 - tickSpeed);
	}

	@Override public void close() {
		Registries.END_CLIENT_TICK.unregister(this);
	}

}
