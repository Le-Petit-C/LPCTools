package lpctools.scripts.trigger;

import lpctools.lpcfymasaapi.Registries;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.UniqueBooleanHotkeyConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.NotNull;

public class TriggerClientTickEnd extends UniqueBooleanHotkeyConfig implements IScriptTrigger, ClientTickEvents.EndTick{
	public final Runnable runScript;
	public TriggerClientTickEnd(@NotNull ILPCConfigReadable parent, Runnable runScript) {
		super(parent, nameKey, false, null, null);
		this.runScript = runScript;
	}
	private void registerAll(boolean b){Registries.END_CLIENT_TICK.register(this, b);}
	@Override public void onValueChanged() {
		registerAll(booleanValue);
		super.onValueChanged();
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "clientTickEnd";
	public static final String fullKey = fullPrefix + nameKey;
	@Override public void onEndTick(MinecraftClient mc) {runScript.run();}
	@Override public void close() {
		super.close();
		registerAll(false);
	}
}
