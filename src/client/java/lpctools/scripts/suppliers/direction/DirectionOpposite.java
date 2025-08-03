package lpctools.scripts.suppliers.direction;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.WrappedThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import lpctools.scripts.utils.choosers.DirectionSupplierChooser;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class DirectionOpposite extends WrappedThirdListConfig implements IScriptDirectionSupplier {
	private final DirectionSupplierChooser direction = addConfig(new DirectionSupplierChooser(parent, "direction", this::onValueChanged));
	public DirectionOpposite(ILPCConfigReadable parent) {super(parent, nameKey, null);}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		res.add(1, (button, mouseButton) -> direction.openChoose(), ()->fullKey + ".direction", buttonGenericAllocator);
	}
	@Override public @NotNull Function<CompiledVariableList, Direction>
	compile(VariableMap variableMap) throws CompileFailedException {
		Function<CompiledVariableList, Direction> direction = this.direction.get().compile(variableMap);
		return list->direction.apply(list).getOpposite();
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "opposite";
	public static final String fullKey = fullPrefix + nameKey;
}
