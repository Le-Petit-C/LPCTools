package lpctools.scripts.runner;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.UniqueStringConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.runner.variables.CompiledVariableList;
import lpctools.scripts.runner.variables.VariableMap;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static lpctools.util.DataUtils.*;

public class RunnerMessage extends UniqueStringConfig implements IScriptRunner {
	public RunnerMessage(@NotNull ILPCConfigReadable parent, @NotNull String nameKey) {
		super(parent, nameKey, null, null);
	}
	@Override public Consumer<CompiledVariableList> compile(VariableMap variableMap) {
		return list->notifyPlayer(getStringValue(), false);
	}
}
