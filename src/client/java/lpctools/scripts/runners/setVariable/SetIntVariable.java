package lpctools.scripts.runners.setVariable;

import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.utils.choosers.IntSupplierChooser;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.IntVariable;
import lpctools.scripts.runners.variables.VariableMap;
import lpctools.scripts.runners.variables.VariableTestPack;
import lpctools.scripts.suppliers._int.IScriptIntSupplier;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.ToIntFunction;

public class SetIntVariable extends SetVariable<IScriptIntSupplier>{
	public SetIntVariable(@NotNull ILPCConfigReadable parent) {
		super(parent, nameKey, new IntSupplierChooser(parent, "chooser", null));
	}
	@Override protected VariableTestPack testPack() {return IntVariable.testPack;}
	@Override protected @NotNull Consumer<CompiledVariableList>
	setValue(VariableMap variableMap, IScriptIntSupplier src, int index) throws CompileFailedException {
		ToIntFunction<CompiledVariableList> func = src.compileToInt(variableMap);
		return list->list.<MutableInt>getVariable(index).setValue(func.applyAsInt(list));
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "setIntVariable";
	public static final String fullKey = fullPrefix + nameKey;
}
