package lpctools.scripts.suppliers._boolean;

import fi.dy.masa.litematica.util.ToBooleanFunction;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.WrappedThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.choosers.BlockSupplierChooser;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import net.minecraft.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class BlockEquals extends WrappedThirdListConfig implements IScriptBooleanSupplier {
	private final BlockSupplierChooser block1, block2;
	public BlockEquals(ILPCConfigReadable parent) {
		super(parent, nameKey, null);
		block1 = new BlockSupplierChooser(parent, "block1", this::onValueChanged);
		block2 = new BlockSupplierChooser(parent, "block2", this::onValueChanged);
		addConfig(block1);
		addConfig(block2);
	}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		res.add(1, (button, mouseButton) -> block1.openChoose(), ()->fullKey + ".block1", buttonGenericAllocator);
		res.add(1, (button, mouseButton) -> block2.openChoose(), ()->fullKey + ".block2", buttonGenericAllocator);
	}
	@Override
	public @NotNull ToBooleanFunction<CompiledVariableList>
	compileToBoolean(VariableMap variableMap) throws CompileFailedException {
		Function<CompiledVariableList, Block> pos1, pos2;
		pos1 = this.block1.get().compile(variableMap);
		pos2 = this.block2.get().compile(variableMap);
		return list->pos1.apply(list) == pos2.apply(list);
	}
	
	@Override public void onValueChanged() {
		getPage().markNeedUpdate();
		super.onValueChanged();
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "blockEquals";
	public static final String fullKey = fullPrefix + nameKey;
	
}
