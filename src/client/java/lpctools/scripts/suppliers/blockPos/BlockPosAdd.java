package lpctools.scripts.suppliers.blockPos;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.WrappedThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.choosers.BlockPosSupplierChooser;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

public class BlockPosAdd extends WrappedThirdListConfig implements IScriptBlockPosSupplier {
	private final BlockPosSupplierChooser pos1, pos2;
	public BlockPosAdd(ILPCConfigReadable parent) {
		super(parent, nameKey, null);
		pos1 = addConfig(new BlockPosSupplierChooser(parent, "pos1", this::onValueChanged));
		pos2 = addConfig(new BlockPosSupplierChooser(parent, "pos2", this::onValueChanged));
	}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		res.add(1, (button, mouseButton) -> pos1.openChoose(), ()->fullKey + ".pos1", buttonGenericAllocator);
		res.add(1, (button, mouseButton) -> pos2.openChoose(), ()->fullKey + ".pos2", buttonGenericAllocator);
	}
	@Override public @NotNull BiConsumer<CompiledVariableList, BlockPos.Mutable>
	compileToBlockPos(VariableMap variableMap) throws CompileFailedException {
		BiConsumer<CompiledVariableList, BlockPos.Mutable> pos1, pos2;
		pos1 = this.pos1.get().compileToBlockPos(variableMap);
		pos2 = this.pos2.get().compileToBlockPos(variableMap);
		BlockPos.Mutable buf = new BlockPos.Mutable();
		return (list, pos)->{
			pos1.accept(list, pos);
			pos2.accept(list, buf);
			pos.move(buf);
		};
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "blockPosAdd";
	public static final String fullKey = fullPrefix + nameKey;
}
