package lpctools.script.trigger;

import lpctools.lpcfymasaapi.Registries;
import lpctools.script.Script;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

class ClientTickStartOption extends TriggerOptionBase implements ClientTickEvents.StartTick {
	private final Script script;
	ClientTickStartOption(ScriptTrigger trigger, TriggerOptionFactory factory) {
		super(trigger, factory);
		script = trigger.getScript();
	}
	@Override public void registerScript(boolean b) {Registries.START_CLIENT_TICK.register(this, b);}
	@Override public void onStartTick(MinecraftClient minecraftClient) {script.runScript();}
}
