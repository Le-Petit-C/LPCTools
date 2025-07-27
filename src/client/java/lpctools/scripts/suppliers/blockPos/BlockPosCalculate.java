package lpctools.scripts.suppliers.blockPos;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.WrappedThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.choosers.BlockPosSupplierChooser;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import lpctools.scripts.utils.operators.ExtraOperatorConfig;
import lpctools.scripts.utils.operators.Operators;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

public class BlockPosCalculate extends WrappedThirdListConfig implements IScriptBlockPosSupplier {
	private final BlockPosSupplierChooser pos1 = addConfig(new BlockPosSupplierChooser(parent, "pos1", this::onValueChanged));
	private final ExtraOperatorConfig operator = addConfig(new ExtraOperatorConfig(this));
	private final BlockPosSupplierChooser pos2 = addConfig(new BlockPosSupplierChooser(parent, "pos2", this::onValueChanged));
	public BlockPosCalculate(ILPCConfigReadable parent) {super(parent, nameKey, null);}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		res.add(1, (button, mouseButton) -> pos1.openChoose(), ()->fullKey + ".pos1", buttonGenericAllocator);
		res.add(1, (button, mouseButton) -> pos2.openChoose(), ()->fullKey + ".pos2", buttonGenericAllocator);
	}
	@Override public @NotNull BiConsumer<CompiledVariableList, BlockPos.Mutable>
	compileToBlockPos(VariableMap variableMap) throws CompileFailedException {
		BiConsumer<CompiledVariableList, BlockPos.Mutable> pos1 = this.pos1.get().compileToBlockPos(variableMap);
		Operators.IExtraOperator operator = this.operator.get();
		BiConsumer<CompiledVariableList, BlockPos.Mutable> pos2 = this.pos2.get().compileToBlockPos(variableMap);
		BlockPos.Mutable buf1 = new BlockPos.Mutable();
		BlockPos.Mutable buf2 = new BlockPos.Mutable();
		return (list, pos)->{
			pos1.accept(list, buf1);
			pos2.accept(list, buf2);
			operator.operate(buf1, buf2, pos);
		};
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "blockPosCalculate";
	public static final String fullKey = fullPrefix + nameKey;
}
