package lpctools.scripts.trigger;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.UniqueHotkeyConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import org.jetbrains.annotations.NotNull;

public class TriggerHotkey extends UniqueHotkeyConfig implements IScriptTrigger{
	public TriggerHotkey(@NotNull ILPCConfigReadable parent, Runnable runScript) {
		super(parent, nameKey, null, null);
		getKeybind().setCallback(((action, key) -> {runScript.run();return true;}));
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "hotkey";
	public static final String fullKey = fullPrefix + nameKey;
}
