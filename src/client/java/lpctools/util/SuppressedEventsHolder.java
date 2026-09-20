package lpctools.util;

import lpctools.lpcfymasaapi.Registries;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import org.jspecify.annotations.NonNull;

import java.util.LinkedList;

public class SuppressedEventsHolder implements ClientTickEvents.EndTick {
	public interface SuppressedEvent { boolean tryHandle(); }

	public static void addSuppressedEvent(SuppressedEvent event) {
		Minecraft.getInstance().execute(()->getInstance()._addSuppressedEvent(event));
	}

	private static SuppressedEventsHolder instance;
	private static SuppressedEventsHolder getInstance() {
		// if(!Minecraft.getInstance().isSameThread()) throw new RuntimeException();
		if(instance == null) Registries.END_CLIENT_TICK.register(instance = new SuppressedEventsHolder());
		return instance;
	}

	private final LinkedList<SuppressedEvent> suppressedEvents = new LinkedList<>();
	private int emptyTicks = 0;

	private SuppressedEventsHolder(){}
	private void _addSuppressedEvent(SuppressedEvent event) { suppressedEvents.add(event); }

	@Override public void onEndTick(@NonNull Minecraft client) {
		suppressedEvents.removeIf(SuppressedEvent::tryHandle);
		if(suppressedEvents.isEmpty() && ++emptyTicks >= 200) {
			assert instance == this;
			Registries.END_CLIENT_TICK.unregister(this);
			instance = null;
		}
		else emptyTicks = 0;
	}
}
