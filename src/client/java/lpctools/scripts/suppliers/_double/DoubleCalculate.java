package lpctools.scripts.suppliers._double;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.WrappedThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.utils.choosers.DoubleSupplierChooser;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import lpctools.scripts.utils.operators.BasicOperatorConfig;
import lpctools.scripts.utils.operators.Operators;
import org.jetbrains.annotations.NotNull;

import java.util.function.ToDoubleFunction;

public class DoubleCalculate extends WrappedThirdListConfig implements IScriptDoubleSupplier {
	private final DoubleSupplierChooser double1 = addConfig(new DoubleSupplierChooser(parent, "double1", this::onValueChanged));
	private final BasicOperatorConfig operator = addConfig(new BasicOperatorConfig(this));
	private final DoubleSupplierChooser double2 = addConfig(new DoubleSupplierChooser(parent, "double2", this::onValueChanged));
	public DoubleCalculate(ILPCConfigReadable parent) {super(parent, nameKey, null);}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		res.add(1, (button, mouseButton) -> double1.openChoose(), ()->fullKey + ".double1", buttonGenericAllocator);
		res.add(1, (button, mouseButton) -> double2.openChoose(), ()->fullKey + ".double2", buttonGenericAllocator);
	}
	@Override public @NotNull ToDoubleFunction<CompiledVariableList>
	compileToDouble(VariableMap variableMap) throws CompileFailedException {
		ToDoubleFunction<CompiledVariableList> double1 = this.double1.get().compileToDouble(variableMap);
		Operators.IBasicOperator operator = this.operator.get();
		ToDoubleFunction<CompiledVariableList> double2 = this.double2.get().compileToDouble(variableMap);
		return list->operator.operate(double1.applyAsDouble(list), double2.applyAsDouble(list));
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "calculate";
	public static final String fullKey = fullPrefix + nameKey;
}
