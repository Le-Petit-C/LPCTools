package lpctools.scripts.suppliers._boolean.blockBoolean;

import fi.dy.masa.litematica.util.ToBooleanFunction;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.WrappedThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import lpctools.scripts.suppliers._boolean.IScriptBooleanSupplier;
import lpctools.scripts.utils.choosers.BlockSupplierChooser;
import net.minecraft.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public abstract class BlockBoolean extends WrappedThirdListConfig implements IScriptBooleanSupplier {
	private final BlockSupplierChooser block = new BlockSupplierChooser(parent, "block", this::onValueChanged);
	public BlockBoolean(ILPCConfigReadable parent, String nameKey) {
		super(parent, nameKey, null);
		addConfig(block);
	}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		res.add(1, (button, mouseButton) -> block.openChoose(), ()->fullKey + ".block", buttonGenericAllocator);
	}
	@Override public @NotNull ToBooleanFunction<CompiledVariableList>
	compileToBoolean(VariableMap variableMap) throws CompileFailedException {
		Function<CompiledVariableList, Block> block = this.block.get().compile(variableMap);
		return list->getBoolean(block.apply(list));
	}
	protected abstract boolean getBoolean(Block block);
	@Override public void onValueChanged() {
		getPage().markNeedUpdate();
		super.onValueChanged();
	}
	public static final String nameKey = "blockBoolean";
	public static final String fullKey = IScriptBooleanSupplier.fullPrefix + nameKey;
	public static final String fullPrefix = fullKey + '.';
}
