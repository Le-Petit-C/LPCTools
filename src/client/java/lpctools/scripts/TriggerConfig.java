package lpctools.scripts;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.MutableConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCUniqueConfigBase;
import org.jetbrains.annotations.NotNull;

import static lpctools.scripts.ScriptConfigData.triggerConfigs;

public class TriggerConfig extends MutableConfig<ILPCUniqueConfigBase> implements IScriptBase{
	public TriggerConfig(@NotNull ScriptConfig parent) {
		super(parent, "triggers", "lpctools.configs.scripts.triggers", triggerConfigs, null, parent::runScript);
	}
}
