package lpctools.scripts.suppliers._int;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.WrappedThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import lpctools.scripts.utils.choosers.DoubleSupplierChooser;
import lpctools.scripts.utils.rounding.RoundingMethod;
import lpctools.scripts.utils.rounding.RoundingMethodConfig;
import org.jetbrains.annotations.NotNull;

import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

public class RoundingDouble extends WrappedThirdListConfig implements IScriptIntSupplier {
	private final DoubleSupplierChooser _double = addConfig(new DoubleSupplierChooser(parent, "double", null));
	private final RoundingMethodConfig method = addConfig(new RoundingMethodConfig(this));
	public RoundingDouble(ILPCConfigReadable parent) {super(parent, nameKey, null);}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		res.add(1, (button, mouseButton) -> _double.openChoose(), ()->fullKey + ".double", buttonGenericAllocator);
	}
	@Override public @NotNull ToIntFunction<CompiledVariableList>
	compileToInt(VariableMap variableMap) throws CompileFailedException {
		ToDoubleFunction<CompiledVariableList> _double = this._double.get().compileToDouble(variableMap);
		RoundingMethod method = this.method.get();
		return list->method.round(_double.applyAsDouble(list));
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "roundingDouble";
	public static final String fullKey = fullPrefix + nameKey;
}
