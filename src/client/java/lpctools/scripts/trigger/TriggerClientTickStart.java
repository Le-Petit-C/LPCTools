package lpctools.scripts.trigger;

import lpctools.lpcfymasaapi.Registries;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.UniqueBooleanHotkeyConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.NotNull;

public class TriggerClientTickStart extends UniqueBooleanHotkeyConfig implements IScriptTrigger, ClientTickEvents.StartTick{
	public final Runnable runScript;
	public TriggerClientTickStart(@NotNull ILPCConfigReadable parent, Runnable runScript) {
		super(parent, nameKey, false, null, null);
		this.runScript = runScript;
	}
	@Override public void onValueChanged() {
		Registries.START_CLIENT_TICK.register(this, booleanValue);
		super.onValueChanged();
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "clientTickStart";
	public static final String fullKey = fullPrefix + nameKey;
	@Override public void onStartTick(MinecraftClient mc) {runScript.run();}
}
