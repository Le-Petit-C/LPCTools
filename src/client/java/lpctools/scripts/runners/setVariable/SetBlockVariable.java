package lpctools.scripts.runners.setVariable;

import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.utils.choosers.BlockSupplierChooser;
import lpctools.scripts.runners.variables.*;
import lpctools.scripts.suppliers.block.IScriptBlockSupplier;
import net.minecraft.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;

public class SetBlockVariable extends SetVariable<IScriptBlockSupplier>{
	public SetBlockVariable(@NotNull ILPCConfigReadable parent) {
		super(parent, nameKey, new BlockSupplierChooser(parent, "chooser", null));
	}
	@Override protected VariableTestPack testPack() {return BlockVariable.testPack;}
	@Override protected @NotNull Consumer<CompiledVariableList>
	setValue(VariableMap variableMap, IScriptBlockSupplier src, int index) throws CompileFailedException {
		Function<CompiledVariableList, Block> func = src.compile(variableMap);
		return list->list.set(index, func.apply(list));
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "setBlockVariable";
	public static final String fullKey = fullPrefix + nameKey;
}
