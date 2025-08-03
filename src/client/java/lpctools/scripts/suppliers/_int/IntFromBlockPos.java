package lpctools.scripts.suppliers._int;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.WrappedThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import lpctools.scripts.utils.choosers.AxisSupplierChooser;
import lpctools.scripts.utils.choosers.BlockPosSupplierChooser;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public class IntFromBlockPos extends WrappedThirdListConfig implements IScriptIntSupplier {
	private final BlockPosSupplierChooser pos = addConfig(new BlockPosSupplierChooser(parent, "pos", this::onValueChanged));
	private final AxisSupplierChooser axis = addConfig(new AxisSupplierChooser(parent, "axis", this::onValueChanged));
	public IntFromBlockPos(ILPCConfigReadable parent) {super(parent, nameKey, null);}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		res.add(1, (button, mouseButton) -> pos.openChoose(), ()->fullKey + ".pos", buttonGenericAllocator);
		res.add(1, (button, mouseButton) -> axis.openChoose(), ()->fullKey + ".axis", buttonGenericAllocator);
	}
	@Override public @NotNull ToIntFunction<CompiledVariableList>
	compileToInt(VariableMap variableMap) throws CompileFailedException {
		BiConsumer<CompiledVariableList, BlockPos.Mutable> vec = this.pos.get().compileToBlockPos(variableMap);
		Function<CompiledVariableList, Direction.Axis> axis = this.axis.get().compile(variableMap);
		BlockPos.Mutable posBuf = new BlockPos.Mutable();
		return list->{
			vec.accept(list, posBuf);
			return posBuf.getComponentAlongAxis(axis.apply(list));
		};
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "fromBlockPos";
	public static final String fullKey = fullPrefix + nameKey;
}
