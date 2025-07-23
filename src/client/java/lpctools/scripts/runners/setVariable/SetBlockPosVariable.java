package lpctools.scripts.runners.setVariable;

import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.choosers.BlockPosSupplierChooser;
import lpctools.scripts.runners.variables.BlockPosVariable;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import lpctools.scripts.runners.variables.VariableTestPack;
import lpctools.scripts.suppliers.blockPos.IScriptBlockPosSupplier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SetBlockPosVariable extends SetVariable<IScriptBlockPosSupplier>{
	public SetBlockPosVariable(@NotNull ILPCConfigReadable parent) {
		super(parent, nameKey, new BlockPosSupplierChooser(parent, "chooser", null));
	}
	@Override protected VariableTestPack testPack() {return BlockPosVariable.testPack;}
	@Override protected @NotNull Consumer<CompiledVariableList>
	setValue(VariableMap variableMap, IScriptBlockPosSupplier src, int index) throws CompileFailedException {
		BiConsumer<CompiledVariableList, BlockPos.Mutable> func = src.compileToBlockPos(variableMap);
		return list->func.accept(list, list.getVariable(index));
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "setBlockPosVariable";
	public static final String fullKey = fullPrefix + nameKey;
}
