package lpctools.scripts.suppliers._boolean;

import fi.dy.masa.litematica.util.ToBooleanFunction;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.WrappedThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.utils.choosers.BooleanSupplierChooser;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import org.jetbrains.annotations.NotNull;

public class Not extends WrappedThirdListConfig implements IScriptBooleanSupplier {
	private final BooleanSupplierChooser b = new BooleanSupplierChooser(parent, "b", this::onValueChanged);
	public Not(ILPCConfigReadable parent) {
		super(parent, nameKey, null);
		addConfig(b);
	}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		res.add(1, (button, mouseButton) -> b.openChoose(), ()->fullKey + ".b", buttonGenericAllocator);
	}
	@Override public @NotNull ToBooleanFunction<CompiledVariableList>
	compileToBoolean(VariableMap variableMap) throws CompileFailedException {
		ToBooleanFunction<CompiledVariableList> b1 = this.b.get().compileToBoolean(variableMap);
		return list->!b1.applyAsBoolean(list);
	}
	
	@Override public void onValueChanged() {
		getPage().markNeedUpdate();
		super.onValueChanged();
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "not";
	public static final String fullKey = fullPrefix + nameKey;
	
}
