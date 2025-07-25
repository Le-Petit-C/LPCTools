package lpctools.scripts.runners.setVariable;

import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.choosers.DoubleSupplierChooser;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.DoubleVariable;
import lpctools.scripts.runners.variables.VariableMap;
import lpctools.scripts.runners.variables.VariableTestPack;
import lpctools.scripts.suppliers._double.IScriptDoubleSupplier;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.ToDoubleFunction;

public class SetVector3dVariable extends SetVariable<IScriptDoubleSupplier>{
	public SetVector3dVariable(@NotNull ILPCConfigReadable parent) {
		super(parent, nameKey, new DoubleSupplierChooser(parent, "chooser", null));
	}
	@Override protected VariableTestPack testPack() {return DoubleVariable.testPack;}
	@Override protected @NotNull Consumer<CompiledVariableList>
	setValue(VariableMap variableMap, IScriptDoubleSupplier src, int index) throws CompileFailedException {
		ToDoubleFunction<CompiledVariableList> func = src.compileToDouble(variableMap);
		return list->list.<MutableDouble>getVariable(index).setValue(func.applyAsDouble(list));
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "setVector3dVariable";
	public static final String fullKey = fullPrefix + nameKey;
}
