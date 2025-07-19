package lpctools.scripts.trigger;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.UniqueHotkeyConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import org.jetbrains.annotations.NotNull;

public class TriggerHotkey extends UniqueHotkeyConfig implements IScriptTrigger{
	public TriggerHotkey(@NotNull ILPCConfigReadable parent, @NotNull String nameKey, Runnable runScript) {
		super(parent, nameKey, null, null);
		getKeybind().setCallback(((action, key) -> {runScript.run();return true;}));
	}
}
