package lpctools.scripts.runners.setVariable;

import fi.dy.masa.litematica.util.ToBooleanFunction;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.choosers.BooleanSupplierChooser;
import lpctools.scripts.runners.variables.*;
import lpctools.scripts.suppliers._boolean.IScriptBooleanSupplier;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class SetBooleanVariable extends SetVariable<IScriptBooleanSupplier>{
	public SetBooleanVariable(@NotNull ILPCConfigReadable parent) {
		super(parent, nameKey, new BooleanSupplierChooser(parent, "chooser", null));
	}
	@Override protected VariableTestPack testPack() {return BooleanVariable.testPack;}
	@Override protected @NotNull Consumer<CompiledVariableList>
	setValue(VariableMap variableMap, IScriptBooleanSupplier src, int index) throws CompileFailedException {
		ToBooleanFunction<CompiledVariableList> func = src.compileToBoolean(variableMap);
		return list->list.<MutableBoolean>getVariable(index).setValue(func.applyAsBoolean(list));
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "setBooleanVariable";
	public static final String fullKey = fullPrefix + nameKey;
}
