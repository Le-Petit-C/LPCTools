package lpctools.scripts.suppliers._int;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.WrappedThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import lpctools.scripts.utils.choosers.BlockPosSupplierChooser;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.ToIntFunction;

public class BlockPosDotProduct extends WrappedThirdListConfig implements IScriptIntSupplier {
	private final BlockPosSupplierChooser pos1 = addConfig(new BlockPosSupplierChooser(parent, "pos1", this::onValueChanged));
	private final BlockPosSupplierChooser pos2 = addConfig(new BlockPosSupplierChooser(parent, "pos2", this::onValueChanged));
	public BlockPosDotProduct(ILPCConfigReadable parent) {super(parent, nameKey, null);}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		res.add(1, (button, mouseButton) -> pos1.openChoose(), ()->fullKey + ".pos1", buttonGenericAllocator);
		res.add(1, (button, mouseButton) -> pos2.openChoose(), ()->fullKey + ".pos2", buttonGenericAllocator);
	}
	@Override public @NotNull ToIntFunction<CompiledVariableList>
	compileToInt(VariableMap variableMap) throws CompileFailedException {
		BiConsumer<CompiledVariableList, BlockPos.Mutable> pos1 = this.pos1.get().compileToBlockPos(variableMap);
		BiConsumer<CompiledVariableList, BlockPos.Mutable> pos2 = this.pos2.get().compileToBlockPos(variableMap);
		BlockPos.Mutable buf1 = new BlockPos.Mutable();
		BlockPos.Mutable buf2 = new BlockPos.Mutable();
		return list->{
			pos1.accept(list, buf1);
			pos2.accept(list, buf2);
			return buf1.getX() * buf2.getX() + buf1.getY() * buf2.getY() + buf1.getZ() * buf2.getZ();
		};
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "dotProduct";
	public static final String fullKey = fullPrefix + nameKey;
}
