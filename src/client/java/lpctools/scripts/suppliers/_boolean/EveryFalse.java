package lpctools.scripts.suppliers._boolean;

import fi.dy.masa.litematica.util.ToBooleanFunction;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.MutableConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static lpctools.scripts.ScriptConfigData.booleanSupplierConfigs;
import static lpctools.scripts.ScriptConfigData.booleanSupplierConfigsTree;

public class EveryFalse extends MutableConfig<IScriptBooleanSupplier> implements IScriptBooleanSupplier{
	public EveryFalse(@NotNull ILPCConfigReadable parent) {
		super(parent, nameKey, IScriptBooleanSupplier.fullKey, booleanSupplierConfigs, booleanSupplierConfigsTree, null);
		setValueChangeCallback(this::notifyScriptChanged);
	}
	@Override public @NotNull ToBooleanFunction<CompiledVariableList>
	compileToBoolean(VariableMap variableMap) throws CompileFailedException {
		ArrayList<ToBooleanFunction<CompiledVariableList>> subCompiled = new ArrayList<>();
		for(IScriptBooleanSupplier subRunner : iterateConfigs())
			subCompiled.add(subRunner.compileToBoolean(variableMap));
		return list->{
			for(ToBooleanFunction<CompiledVariableList> func : subCompiled)
				if(func.applyAsBoolean(list)) return false;
			return true;
		};
	}
	
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "everyFalse";
	public static final String fullKey = fullPrefix + nameKey;
}
