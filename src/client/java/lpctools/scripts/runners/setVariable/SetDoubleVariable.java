package lpctools.scripts.runners.setVariable;

import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.choosers.DoubleSupplierChooser;
import lpctools.scripts.runners.variables.*;
import lpctools.scripts.suppliers._double.IScriptDoubleSupplier;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.ToDoubleFunction;

public class SetDoubleVariable extends SetVariable<IScriptDoubleSupplier>{
	public SetDoubleVariable(@NotNull ILPCConfigReadable parent) {
		super(parent, nameKey, new DoubleSupplierChooser(parent, "chooser", null));
	}
	@Override protected VariableTestPack testPack() {return DoubleVariable.testPack;}
	@Override protected @NotNull Consumer<CompiledVariableList>
	setValue(VariableMap variableMap, IScriptDoubleSupplier src, int index) throws CompileFailedException {
		ToDoubleFunction<CompiledVariableList> func = src.compileToDouble(variableMap);
		return list->list.<MutableDouble>getVariable(index).setValue(func.applyAsDouble(list));
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "setDoubleVariable";
	public static final String fullKey = fullPrefix + nameKey;
}
