package lpctools.scripts.suppliers._double;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.WrappedThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.utils.choosers.IntSupplierChooser;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import org.jetbrains.annotations.NotNull;

import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

public class FromInt extends WrappedThirdListConfig implements IScriptDoubleSupplier {
	private final IntSupplierChooser _int = addConfig(new IntSupplierChooser(parent, "int", this::onValueChanged));
	public FromInt(ILPCConfigReadable parent) {super(parent, nameKey, null);}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		res.add(1, (button, mouseButton) -> _int.openChoose(), ()->fullKey + ".int", buttonGenericAllocator);
	}
	@Override public @NotNull ToDoubleFunction<CompiledVariableList>
	compileToDouble(VariableMap variableMap) throws CompileFailedException {
		ToIntFunction<CompiledVariableList> _int = this._int.get().compileToInt(variableMap);
		return list->(double)_int.applyAsInt(list);
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "fromInt";
	public static final String fullKey = fullPrefix + nameKey;
}
