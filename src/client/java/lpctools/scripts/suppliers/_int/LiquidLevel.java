package lpctools.scripts.suppliers._int;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.WrappedThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import lpctools.scripts.utils.choosers.BlockStateSupplierChooser;
import net.minecraft.block.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.ToIntFunction;

public class LiquidLevel extends WrappedThirdListConfig implements IScriptIntSupplier {
	private final BlockStateSupplierChooser state = addConfig(new BlockStateSupplierChooser(parent, "state", this::onValueChanged));
	public LiquidLevel(ILPCConfigReadable parent) {super(parent, nameKey, null);}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		res.add(1, (button, mouseButton) -> state.openChoose(), ()->fullKey + ".state", buttonGenericAllocator);
	}
	@Override public @NotNull ToIntFunction<CompiledVariableList>
	compileToInt(VariableMap variableMap) throws CompileFailedException {
		Function<CompiledVariableList, BlockState> state = this.state.get().compile(variableMap);
		return list->state.apply(list).getFluidState().getLevel();
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "liquidLevel";
	public static final String fullKey = fullPrefix + nameKey;
}
