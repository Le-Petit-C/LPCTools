package lpctools.scripts.suppliers._boolean.blockStateBoolean;

import fi.dy.masa.litematica.util.ToBooleanFunction;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.WrappedThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import lpctools.scripts.suppliers._boolean.IScriptBooleanSupplier;
import lpctools.scripts.utils.choosers.BlockStateSupplierChooser;
import net.minecraft.block.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public abstract class BlockStateBoolean extends WrappedThirdListConfig implements IScriptBooleanSupplier {
	private final BlockStateSupplierChooser state = new BlockStateSupplierChooser(parent, "state", this::onValueChanged);
	public BlockStateBoolean(ILPCConfigReadable parent, String nameKey) {
		super(parent, nameKey, null);
		addConfig(state);
	}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		res.add(1, (button, mouseButton) -> state.openChoose(), ()->fullKey + ".state", buttonGenericAllocator);
	}
	@Override public @NotNull ToBooleanFunction<CompiledVariableList>
	compileToBoolean(VariableMap variableMap) throws CompileFailedException {
		Function<CompiledVariableList, BlockState> block = this.state.get().compile(variableMap);
		return list->getBoolean(block.apply(list));
	}
	protected abstract boolean getBoolean(BlockState state);
	@Override public void onValueChanged() {
		getPage().markNeedUpdate();
		super.onValueChanged();
	}
	public static final String nameKey = "blockStateBoolean";
	public static final String fullKey = IScriptBooleanSupplier.fullPrefix + nameKey;
	public static final String fullPrefix = fullKey + '.';
}
