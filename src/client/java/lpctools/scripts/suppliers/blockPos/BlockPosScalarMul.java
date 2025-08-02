package lpctools.scripts.suppliers.blockPos;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.WrappedThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import lpctools.scripts.utils.choosers.BlockPosSupplierChooser;
import lpctools.scripts.utils.choosers.IntSupplierChooser;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.ToIntFunction;

public class BlockPosScalarMul extends WrappedThirdListConfig implements IScriptBlockPosSupplier {
	private final IntSupplierChooser scalar = addConfig(new IntSupplierChooser(parent, "scalar", this::onValueChanged));
	private final BlockPosSupplierChooser pos = addConfig(new BlockPosSupplierChooser(parent, "pos", this::onValueChanged));
	public BlockPosScalarMul(ILPCConfigReadable parent) {super(parent, nameKey, null);}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		res.add(1, (button, mouseButton) -> scalar.openChoose(), ()->fullKey + ".scalar", buttonGenericAllocator);
		res.add(1, (button, mouseButton) -> pos.openChoose(), ()->fullKey + ".pos", buttonGenericAllocator);
	}
	@Override public @NotNull BiConsumer<CompiledVariableList, BlockPos.Mutable>
	compileToBlockPos(VariableMap variableMap) throws CompileFailedException {
		ToIntFunction<CompiledVariableList> scalar = this.scalar.get().compileToInt(variableMap);
		BiConsumer<CompiledVariableList, BlockPos.Mutable> pos = this.pos.get().compileToBlockPos(variableMap);
		return (list, res)->{
			int k = scalar.applyAsInt(list);
			pos.accept(list, res);
			res.setX(res.getX() * k);
			res.setY(res.getY() * k);
			res.setZ(res.getZ() * k);
		};
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "scalar";
	public static final String fullKey = fullPrefix + nameKey;
}
