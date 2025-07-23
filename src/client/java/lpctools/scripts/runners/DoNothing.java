package lpctools.scripts.runners;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.ButtonConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class DoNothing extends ButtonConfig implements IScriptRunner{
	public DoNothing(ILPCConfigReadable parent) {super(parent, nameKey, null);}
	@Override public @NotNull Consumer<CompiledVariableList> compile(VariableMap variableMap){return list->{};}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "doNothing";
	public static final String fullKey = fullPrefix + nameKey;
}
