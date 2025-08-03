package lpctools.scripts.runners;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.MutableConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.function.Consumer;

import static lpctools.scripts.ScriptConfigData.*;

public class SubRunners extends MutableConfig<IScriptRunner> implements IScriptRunner{
	public SubRunners(@NotNull ILPCConfigReadable parent) {
		super(parent, nameKey, IScriptRunner.fullKey, runnerConfigs, runnerConfigsTree, null);
		setValueChangeCallback(this::notifyScriptChanged);
	}
	@Override public @NotNull Consumer<CompiledVariableList>
	compile(VariableMap variableMap) throws CompileFailedException {
		ArrayList<Consumer<CompiledVariableList>> subCompiled = new ArrayList<>();
		variableMap.push();
		for(IScriptRunner subRunner : iterateConfigs())
			subCompiled.add(subRunner.compile(variableMap));
		variableMap.pop();
		return list->{
			list.push();
			subCompiled.forEach(consumer->consumer.accept(list));
			list.pop();
		};
	}
	
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "subRunners";
	public static final String fullKey = fullPrefix + nameKey;
}
