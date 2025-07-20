package lpctools.scripts.runners;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.UniqueStringConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigBase;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static lpctools.util.DataUtils.*;

public class RunnerMessage extends UniqueStringConfig implements IScriptRunner {
	public RunnerMessage(@NotNull ILPCConfigBase parent) {
		super(parent, nameKey, null, null);
	}
	@Override public Consumer<CompiledVariableList> compile(VariableMap variableMap) {
		return list->notifyPlayer(getStringValue(), false);
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "message";
	public static final String fullKey = IScriptRunner.fullPrefix + nameKey;
}
