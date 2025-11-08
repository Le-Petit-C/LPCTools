package lpctools.script.trigger;

import lpctools.lpcfymasaapi.Registries;
import lpctools.script.Script;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

class ClientTickEndOption extends TriggerOptionBase implements ClientTickEvents.EndTick {
	private final Script script;
	ClientTickEndOption(ScriptTrigger trigger, TriggerOptionFactory factory) {
		super(trigger, factory);
		script = trigger.getScript();
	}
	@Override public void registerScript(boolean b) {Registries.END_CLIENT_TICK.register(this, b);}
	@Override public void onEndTick(MinecraftClient minecraftClient) {script.runScript();}
}
