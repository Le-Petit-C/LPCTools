package lpctools.scripts.suppliers.blockState;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.WrappedThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import lpctools.scripts.utils.choosers.BlockSupplierChooser;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class BlockDefaultState extends WrappedThirdListConfig implements IScriptBlockStateSupplier {
	private final BlockSupplierChooser block = addConfig(new BlockSupplierChooser(parent, "block", this::onValueChanged));
	public BlockDefaultState(ILPCConfigReadable parent) {super(parent, nameKey, null);}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		res.add(1, (button, mouseButton) -> block.openChoose(), ()->fullKey + ".block", buttonGenericAllocator);
	}
	@Override public @NotNull Function<CompiledVariableList, BlockState>
	compile(VariableMap variableMap) throws CompileFailedException {
		Function<CompiledVariableList, Block> block;
		block = this.block.get().compile(variableMap);
		return list->block.apply(list).getDefaultState();
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "blockDefaultState";
	public static final String fullKey = fullPrefix + nameKey;
}
