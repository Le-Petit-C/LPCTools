package lpctools.scripts.suppliers.blockPos;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.WrappedThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import lpctools.scripts.utils.choosers.IntSupplierChooser;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.ToIntFunction;

public class BlockPosFromInts extends WrappedThirdListConfig implements IScriptBlockPosSupplier {
	private final IntSupplierChooser x = addConfig(new IntSupplierChooser(parent, "x", this::onValueChanged));
	private final IntSupplierChooser y = addConfig(new IntSupplierChooser(parent, "y", this::onValueChanged));
	private final IntSupplierChooser z = addConfig(new IntSupplierChooser(parent, "z", this::onValueChanged));
	public BlockPosFromInts(ILPCConfigReadable parent) {super(parent, nameKey, null);}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		res.add(1, (button, mouseButton) -> x.openChoose(), ()->fullKey + ".x", buttonGenericAllocator);
		res.add(1, (button, mouseButton) -> y.openChoose(), ()->fullKey + ".y", buttonGenericAllocator);
		res.add(1, (button, mouseButton) -> z.openChoose(), ()->fullKey + ".z", buttonGenericAllocator);
	}
	@Override public @NotNull BiConsumer<CompiledVariableList, BlockPos.Mutable>
	compileToBlockPos(VariableMap variableMap) throws CompileFailedException {
		ToIntFunction<CompiledVariableList> x = this.x.get().compileToInt(variableMap);
		ToIntFunction<CompiledVariableList> y = this.y.get().compileToInt(variableMap);
		ToIntFunction<CompiledVariableList> z = this.z.get().compileToInt(variableMap);
		return (list, pos)->pos.set(x.applyAsInt(list), y.applyAsInt(list), z.applyAsInt(list));
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "fromInts";
	public static final String fullKey = fullPrefix + nameKey;
}
