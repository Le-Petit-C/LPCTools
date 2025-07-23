package lpctools.scripts.suppliers.blockPos;

import lpctools.scripts.CompileFailedException;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import lpctools.scripts.suppliers.IScriptSupplier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Function;

public interface IScriptBlockPosSupplier extends IScriptSupplier<BlockPos> {
	String fullPrefix = IScriptSupplier.fullPrefix + "blockPos.";
	@Override @Deprecated @NotNull default Function<CompiledVariableList, BlockPos>
	compile(VariableMap variableMap) throws CompileFailedException {
		BiConsumer<CompiledVariableList, BlockPos.Mutable> func = compileToBlockPos(variableMap);
		BlockPos.Mutable mutable = new BlockPos.Mutable();
		return list->{
			func.accept(list, mutable);
			return mutable;
		};
	}
	@NotNull BiConsumer<CompiledVariableList, BlockPos.Mutable>
	compileToBlockPos(VariableMap variableMap) throws CompileFailedException;
}
