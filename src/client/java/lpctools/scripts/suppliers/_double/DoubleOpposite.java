package lpctools.scripts.suppliers._double;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.WrappedThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import lpctools.scripts.utils.choosers.DoubleSupplierChooser;
import org.jetbrains.annotations.NotNull;

import java.util.function.ToDoubleFunction;

public class DoubleOpposite extends WrappedThirdListConfig implements IScriptDoubleSupplier {
	private final DoubleSupplierChooser _double = addConfig(new DoubleSupplierChooser(parent, "double", this::onValueChanged));
	public DoubleOpposite(ILPCConfigReadable parent) {super(parent, nameKey, null);}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		res.add(1, (button, mouseButton) -> _double.openChoose(), ()->fullKey + ".double", buttonGenericAllocator);
	}
	@Override public @NotNull ToDoubleFunction<CompiledVariableList>
	compileToDouble(VariableMap variableMap) throws CompileFailedException {
		ToDoubleFunction<CompiledVariableList> _double = this._double.get().compileToDouble(variableMap);
		return list->-_double.applyAsDouble(list);
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "opposite";
	public static final String fullKey = fullPrefix + nameKey;
}
