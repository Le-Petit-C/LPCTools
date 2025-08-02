package lpctools.scripts.runners.setVariable;

import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.runners.variables.*;
import lpctools.scripts.suppliers.blockState.IScriptBlockStateSupplier;
import lpctools.scripts.utils.choosers.BlockStateSupplierChooser;
import net.minecraft.block.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;

public class SetBlockStateVariable extends SetVariable<IScriptBlockStateSupplier>{
	public SetBlockStateVariable(@NotNull ILPCConfigReadable parent) {
		super(parent, nameKey, new BlockStateSupplierChooser(parent, "chooser", null));
	}
	@Override protected VariableTestPack testPack() {return BlockStateVariable.testPack;}
	@Override protected @NotNull Consumer<CompiledVariableList>
	setValue(VariableMap variableMap, IScriptBlockStateSupplier src, int index) throws CompileFailedException {
		Function<CompiledVariableList, BlockState> func = src.compile(variableMap);
		return list->list.set(index, func.apply(list));
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "setBlockStateVariable";
	public static final String fullKey = fullPrefix + nameKey;
}
