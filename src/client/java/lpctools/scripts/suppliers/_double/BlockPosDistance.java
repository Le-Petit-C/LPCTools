package lpctools.scripts.suppliers._double;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.WrappedThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import lpctools.scripts.utils.choosers.BlockPosSupplierChooser;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.ToDoubleFunction;

public class BlockPosDistance extends WrappedThirdListConfig implements IScriptDoubleSupplier {
	private final BlockPosSupplierChooser vec1 = addConfig(new BlockPosSupplierChooser(parent, "vec1", this::onValueChanged));
	private final BlockPosSupplierChooser vec2 = addConfig(new BlockPosSupplierChooser(parent, "vec2", this::onValueChanged));
	public BlockPosDistance(ILPCConfigReadable parent) {super(parent, nameKey, null);}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		res.add(1, (button, mouseButton) -> vec1.openChoose(), ()->fullKey + ".vec1", buttonGenericAllocator);
		res.add(1, (button, mouseButton) -> vec2.openChoose(), ()->fullKey + ".vec2", buttonGenericAllocator);
	}
	@Override public @NotNull ToDoubleFunction<CompiledVariableList>
	compileToDouble(VariableMap variableMap) throws CompileFailedException {
		BiConsumer<CompiledVariableList, BlockPos.Mutable> vec1 = this.vec1.get().compileToBlockPos(variableMap);
		BiConsumer<CompiledVariableList, BlockPos.Mutable> vec2 = this.vec2.get().compileToBlockPos(variableMap);
		BlockPos.Mutable buf1 = new BlockPos.Mutable(), buf2 = new BlockPos.Mutable();
		return list->{
			vec1.accept(list, buf1);
			vec2.accept(list, buf2);
			return Math.sqrt(buf1.getSquaredDistance(buf2));
		};
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "blockPosDistance";
	public static final String fullKey = fullPrefix + nameKey;
}
