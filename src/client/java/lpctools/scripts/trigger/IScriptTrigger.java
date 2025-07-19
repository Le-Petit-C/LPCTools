package lpctools.scripts.trigger;

import lpctools.lpcfymasaapi.interfaces.ILPCUniqueConfigBase;
import org.jetbrains.annotations.NotNull;

public interface IScriptTrigger extends ILPCUniqueConfigBase {
	@Override default @NotNull String getFullTranslationKey() {
		return "lpctools.configs.scripts.triggers." + getNameKey();
	}
}
