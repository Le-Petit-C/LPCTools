package lpctools.scripts.suppliers._boolean;

import fi.dy.masa.litematica.util.ToBooleanFunction;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.WrappedThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.utils.choosers.BooleanSupplierChooser;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import lpctools.scripts.utils.comparator.Comparators;
import lpctools.scripts.utils.comparator.EqualComparatorConfig;
import org.jetbrains.annotations.NotNull;

public class BooleanCompare extends WrappedThirdListConfig implements IScriptBooleanSupplier {
	private final BooleanSupplierChooser b1 = new BooleanSupplierChooser(parent, "b1", this::onValueChanged);
	private final EqualComparatorConfig comparator = new EqualComparatorConfig(this);
	private final BooleanSupplierChooser b2 = new BooleanSupplierChooser(parent, "b2", this::onValueChanged);
	public BooleanCompare(ILPCConfigReadable parent) {
		super(parent, nameKey, null);
		addConfig(b1);
		addConfig(comparator);
		addConfig(b2);
	}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		res.add(1, (button, mouseButton) -> b1.openChoose(), ()->fullKey + ".b1", buttonGenericAllocator);
		res.add(1, (button, mouseButton) -> b2.openChoose(), ()->fullKey + ".b2", buttonGenericAllocator);
	}
	@Override public @NotNull ToBooleanFunction<CompiledVariableList>
	compileToBoolean(VariableMap variableMap) throws CompileFailedException {
		ToBooleanFunction<CompiledVariableList> b1 = this.b1.get().compileToBoolean(variableMap);
		Comparators.IEqualComparable comparator = this.comparator.get();
		ToBooleanFunction<CompiledVariableList> b2 = this.b1.get().compileToBoolean(variableMap);
		return list->comparator.compare(b1.applyAsBoolean(list), b2.applyAsBoolean(list));
	}
	
	@Override public void onValueChanged() {
		getPage().markNeedUpdate();
		super.onValueChanged();
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "booleanCompare";
	public static final String fullKey = fullPrefix + nameKey;
	
}
