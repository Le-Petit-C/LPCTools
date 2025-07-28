package lpctools.scripts.suppliers._boolean;

import fi.dy.masa.litematica.util.ToBooleanFunction;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.WrappedThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.utils.choosers.IntSupplierChooser;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import lpctools.scripts.utils.comparator.AllComparatorConfig;
import lpctools.scripts.utils.comparator.Comparators;
import org.jetbrains.annotations.NotNull;

import java.util.function.ToIntFunction;

public class IntCompare extends WrappedThirdListConfig implements IScriptBooleanSupplier {
	private final IntSupplierChooser double1 = new IntSupplierChooser(parent, "int1", this::onValueChanged);
	private final AllComparatorConfig comparator = new AllComparatorConfig(this);
	private final IntSupplierChooser double2 = new IntSupplierChooser(parent, "int2", this::onValueChanged);
	public IntCompare(ILPCConfigReadable parent) {
		super(parent, nameKey, null);
		addConfig(double1);
		addConfig(comparator);
		addConfig(double2);
	}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		res.add(1, (button, mouseButton) -> double1.openChoose(), ()->fullKey + ".int1", buttonGenericAllocator);
		res.add(1, (button, mouseButton) -> double2.openChoose(), ()->fullKey + ".int2", buttonGenericAllocator);
	}
	@Override public @NotNull ToBooleanFunction<CompiledVariableList>
	compileToBoolean(VariableMap variableMap) throws CompileFailedException {
		ToIntFunction<CompiledVariableList> double1 = this.double1.get().compileToInt(variableMap);
		Comparators.IAllComparable comparator = this.comparator.get();
		ToIntFunction<CompiledVariableList> double2 = this.double2.get().compileToInt(variableMap);
		return list->comparator.compare(double1.applyAsInt(list), double2.applyAsInt(list));
	}
	@Override public void onValueChanged() {
		getPage().markNeedUpdate();
		super.onValueChanged();
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "intCompare";
	public static final String fullKey = fullPrefix + nameKey;
	
}
