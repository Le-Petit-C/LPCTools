package lpctools.scripts.suppliers._boolean;

import fi.dy.masa.litematica.util.ToBooleanFunction;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.WrappedThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.utils.choosers.DoubleSupplierChooser;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import lpctools.scripts.utils.comparator.AllComparatorConfig;
import lpctools.scripts.utils.comparator.Comparators;
import org.jetbrains.annotations.NotNull;

import java.util.function.ToDoubleFunction;

public class DoubleCompare extends WrappedThirdListConfig implements IScriptBooleanSupplier {
	private final DoubleSupplierChooser double1 = new DoubleSupplierChooser(parent, "double1", this::onValueChanged);
	private final AllComparatorConfig comparator = new AllComparatorConfig(this);
	private final DoubleSupplierChooser double2 = new DoubleSupplierChooser(parent, "double2", this::onValueChanged);
	public DoubleCompare(ILPCConfigReadable parent) {
		super(parent, nameKey, null);
		addConfig(double1);
		addConfig(comparator);
		addConfig(double2);
	}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		res.add(1, (button, mouseButton) -> double1.openChoose(), ()->fullKey + ".double1", buttonGenericAllocator);
		res.add(1, (button, mouseButton) -> double2.openChoose(), ()->fullKey + ".double2", buttonGenericAllocator);
	}
	@Override
	public @NotNull ToBooleanFunction<CompiledVariableList>
	compileToBoolean(VariableMap variableMap) throws CompileFailedException {
		ToDoubleFunction<CompiledVariableList> double1 = this.double1.get().compileToDouble(variableMap);
		Comparators.IAllComparable comparator = this.comparator.get();
		ToDoubleFunction<CompiledVariableList> double2 = this.double2.get().compileToDouble(variableMap);
		return list->comparator.compare(double1.applyAsDouble(list), double2.applyAsDouble(list));
	}
	
	@Override public void onValueChanged() {
		getPage().markNeedUpdate();
		super.onValueChanged();
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "doubleCompare";
	public static final String fullKey = fullPrefix + nameKey;
	
}
