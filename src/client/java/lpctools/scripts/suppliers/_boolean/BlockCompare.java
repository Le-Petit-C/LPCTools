package lpctools.scripts.suppliers._boolean;

import fi.dy.masa.litematica.util.ToBooleanFunction;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.WrappedThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.choosers.BlockSupplierChooser;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import lpctools.scripts.suppliers._boolean.comparator.Comparators;
import lpctools.scripts.suppliers._boolean.comparator.EqualComparatorConfig;
import net.minecraft.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class BlockCompare extends WrappedThirdListConfig implements IScriptBooleanSupplier {
	private final BlockSupplierChooser block1 = new BlockSupplierChooser(parent, "block1", this::onValueChanged);
	private final EqualComparatorConfig comparator = new EqualComparatorConfig(this);
	private final BlockSupplierChooser block2 = new BlockSupplierChooser(parent, "block2", this::onValueChanged);
	public BlockCompare(ILPCConfigReadable parent) {
		super(parent, nameKey, null);
		addConfig(block1);
		addConfig(comparator);
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
		Function<CompiledVariableList, Block> block1 = this.block1.get().compile(variableMap);
		Comparators.IEqualComparable comparator = this.comparator.get();
		Function<CompiledVariableList, Block> block2 = this.block2.get().compile(variableMap);
		return list->comparator.compare(block1.apply(list), block2.apply(list));
	}
	
	@Override public void onValueChanged() {
		getPage().markNeedUpdate();
		super.onValueChanged();
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "blockCompare";
	public static final String fullKey = fullPrefix + nameKey;
	
}
