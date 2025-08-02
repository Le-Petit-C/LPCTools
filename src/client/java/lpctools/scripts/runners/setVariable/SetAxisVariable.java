package lpctools.scripts.runners.setVariable;

import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.DirectionVariable;
import lpctools.scripts.runners.variables.VariableMap;
import lpctools.scripts.runners.variables.VariableTestPack;
import lpctools.scripts.suppliers.axis.IScriptAxisSupplier;
import lpctools.scripts.utils.choosers.AxisSupplierChooser;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;

public class SetAxisVariable extends SetVariable<IScriptAxisSupplier>{
	public SetAxisVariable(@NotNull ILPCConfigReadable parent) {
		super(parent, nameKey, new AxisSupplierChooser(parent, "chooser", null));
	}
	@Override protected VariableTestPack testPack() {return DirectionVariable.testPack;}
	@Override protected @NotNull Consumer<CompiledVariableList>
	setValue(VariableMap variableMap, IScriptAxisSupplier src, int index) throws CompileFailedException {
		Function<CompiledVariableList, Direction.Axis> func = src.compile(variableMap);
		return list->list.set(index, func.apply(list));
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "setAxisVariable";
	public static final String fullKey = fullPrefix + nameKey;
}
