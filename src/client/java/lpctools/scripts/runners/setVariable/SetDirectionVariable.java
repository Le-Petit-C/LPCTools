package lpctools.scripts.runners.setVariable;

import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.runners.variables.*;
import lpctools.scripts.suppliers.direction.IScriptDirectionSupplier;
import lpctools.scripts.utils.choosers.DirectionSupplierChooser;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;

public class SetDirectionVariable extends SetVariable<IScriptDirectionSupplier>{
	public SetDirectionVariable(@NotNull ILPCConfigReadable parent) {
		super(parent, nameKey, new DirectionSupplierChooser(parent, "chooser", null));
	}
	@Override protected VariableTestPack testPack() {return DirectionVariable.testPack;}
	@Override protected @NotNull Consumer<CompiledVariableList>
	setValue(VariableMap variableMap, IScriptDirectionSupplier src, int index) throws CompileFailedException {
		Function<CompiledVariableList, Direction> func = src.compile(variableMap);
		return list->list.set(index, func.apply(list));
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "setDirectionVariable";
	public static final String fullKey = fullPrefix + nameKey;
}
