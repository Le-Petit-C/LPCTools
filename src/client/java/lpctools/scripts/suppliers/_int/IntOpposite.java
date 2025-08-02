package lpctools.scripts.suppliers._int;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.WrappedThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import lpctools.scripts.utils.choosers.IntSupplierChooser;
import org.jetbrains.annotations.NotNull;

import java.util.function.ToIntFunction;

public class IntOpposite extends WrappedThirdListConfig implements IScriptIntSupplier {
	private final IntSupplierChooser _int = addConfig(new IntSupplierChooser(parent, "int", this::onValueChanged));
	public IntOpposite(ILPCConfigReadable parent) {super(parent, nameKey, null);}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		res.add(1, (button, mouseButton) -> _int.openChoose(), ()->fullKey + ".int", buttonGenericAllocator);
	}
	@Override public @NotNull ToIntFunction<CompiledVariableList>
	compileToInt(VariableMap variableMap) throws CompileFailedException {
		ToIntFunction<CompiledVariableList> _int = this._int.get().compileToInt(variableMap);
		return list->-_int.applyAsInt(list);
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "opposite";
	public static final String fullKey = fullPrefix + nameKey;
}
