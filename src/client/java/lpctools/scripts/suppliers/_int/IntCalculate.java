package lpctools.scripts.suppliers._int;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.WrappedThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.utils.choosers.IntSupplierChooser;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import lpctools.scripts.utils.operators.BasicOperatorConfig;
import lpctools.scripts.utils.operators.Operators;
import org.jetbrains.annotations.NotNull;

import java.util.function.ToIntFunction;

public class IntCalculate extends WrappedThirdListConfig implements IScriptIntSupplier {
	private final IntSupplierChooser int1 = addConfig(new IntSupplierChooser(parent, "int1", null));
	private final BasicOperatorConfig operator = addConfig(new BasicOperatorConfig(this));
	private final IntSupplierChooser int2 = addConfig(new IntSupplierChooser(parent, "int2", null));
	public IntCalculate(ILPCConfigReadable parent) {super(parent, nameKey, null);}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		res.add(1, (button, mouseButton) -> int1.openChoose(), ()->fullKey + ".int1", buttonGenericAllocator);
		res.add(1, (button, mouseButton) -> int2.openChoose(), ()->fullKey + ".int2", buttonGenericAllocator);
	}
	@Override public @NotNull ToIntFunction<CompiledVariableList>
	compileToInt(VariableMap variableMap) throws CompileFailedException {
		ToIntFunction<CompiledVariableList> int1 = this.int1.get().compileToInt(variableMap);
		Operators.IBasicOperator operator = this.operator.get();
		ToIntFunction<CompiledVariableList> int2 = this.int2.get().compileToInt(variableMap);
		return list->operator.operate(int1.applyAsInt(list), int2.applyAsInt(list));
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "calculate";
	public static final String fullKey = fullPrefix + nameKey;
}
