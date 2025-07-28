package lpctools.scripts.suppliers._boolean;

import fi.dy.masa.litematica.util.ToBooleanFunction;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.WrappedThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.utils.choosers.BlockPosSupplierChooser;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import lpctools.scripts.utils.comparator.Comparators;
import lpctools.scripts.utils.comparator.EqualComparatorConfig;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

public class BlockPosCompare extends WrappedThirdListConfig implements IScriptBooleanSupplier {
	private final BlockPosSupplierChooser pos1 = new BlockPosSupplierChooser(parent, "pos1", this::onValueChanged);
	private final EqualComparatorConfig comparator = new EqualComparatorConfig(this);
	private final BlockPosSupplierChooser pos2 = new BlockPosSupplierChooser(parent, "pos2", this::onValueChanged);
	public BlockPosCompare(ILPCConfigReadable parent) {
		super(parent, nameKey, null);
		addConfig(pos1);
		addConfig(comparator);
		addConfig(pos2);
	}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		res.add(1, (button, mouseButton) -> pos1.openChoose(), ()->fullKey + ".pos1", buttonGenericAllocator);
		res.add(1, (button, mouseButton) -> pos2.openChoose(), ()->fullKey + ".pos2", buttonGenericAllocator);
	}
	@Override
	public @NotNull ToBooleanFunction<CompiledVariableList>
	compileToBoolean(VariableMap variableMap) throws CompileFailedException {
		BiConsumer<CompiledVariableList, BlockPos.Mutable> pos1 = this.pos1.get().compileToBlockPos(variableMap);
		Comparators.IEqualComparable comparator = this.comparator.get();
		BiConsumer<CompiledVariableList, BlockPos.Mutable> pos2 = this.pos2.get().compileToBlockPos(variableMap);
		BlockPos.Mutable buf1 = new BlockPos.Mutable();
		BlockPos.Mutable buf2 = new BlockPos.Mutable();
		return list->{
			pos1.accept(list, buf1);
			pos2.accept(list, buf2);
			return comparator.compare(buf1, buf2);
		};
	}
	@Override public void onValueChanged() {
		getPage().markNeedUpdate();
		super.onValueChanged();
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "blockPosCompare";
	public static final String fullKey = fullPrefix + nameKey;
	
}
