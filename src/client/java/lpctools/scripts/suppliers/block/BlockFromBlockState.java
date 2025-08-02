package lpctools.scripts.suppliers.block;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.WrappedThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import lpctools.scripts.utils.choosers.BlockStateSupplierChooser;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class BlockFromBlockState extends WrappedThirdListConfig implements IScriptBlockSupplier {
	private final BlockStateSupplierChooser state = addConfig(new BlockStateSupplierChooser(parent, "state", this::onValueChanged));
	public BlockFromBlockState(ILPCConfigReadable parent) {
		super(parent, nameKey, null);
	}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		res.add(1, (button, mouseButton) -> state.openChoose(), ()->fullKey + ".pos", buttonGenericAllocator);
	}
	@Override public @NotNull Function<CompiledVariableList, Block>
	compile(VariableMap variableMap) throws CompileFailedException {
		Function<CompiledVariableList, BlockState> state;
		state = this.state.get().compile(variableMap);
		return list->state.apply(list).getBlock();
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "fromBlockState";
	public static final String fullKey = fullPrefix + nameKey;
}
