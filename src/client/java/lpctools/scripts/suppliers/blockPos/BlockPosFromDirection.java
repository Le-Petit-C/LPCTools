package lpctools.scripts.suppliers.blockPos;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.WrappedThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import lpctools.scripts.utils.choosers.DirectionSupplierChooser;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class BlockPosFromDirection extends WrappedThirdListConfig implements IScriptBlockPosSupplier {
	private final DirectionSupplierChooser direction = addConfig(new DirectionSupplierChooser(parent, "direction", this::onValueChanged));
	public BlockPosFromDirection(ILPCConfigReadable parent) {super(parent, nameKey, null);}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		res.add(1, (button, mouseButton) -> direction.openChoose(), ()->fullKey + ".direction", buttonGenericAllocator);
	}
	@Override public @NotNull BiConsumer<CompiledVariableList, BlockPos.Mutable>
	compileToBlockPos(VariableMap variableMap) throws CompileFailedException {
		Function<CompiledVariableList, Direction> direction = this.direction.get().compile(variableMap);
		return (list, res)-> res.set(direction.apply(list).getVector());
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "fromDirection";
	public static final String fullKey = fullPrefix + nameKey;
	
}
