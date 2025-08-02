package lpctools.scripts.suppliers.axis;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.WrappedThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import lpctools.scripts.utils.choosers.DirectionSupplierChooser;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class DirectionAxis extends WrappedThirdListConfig implements IScriptAxisSupplier {
	private final DirectionSupplierChooser vec = addConfig(new DirectionSupplierChooser(parent, "direction", this::onValueChanged));
	public DirectionAxis(ILPCConfigReadable parent) {super(parent, nameKey, null);}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		res.add(1, (button, mouseButton) -> vec.openChoose(), ()->fullKey + ".direction", buttonGenericAllocator);
	}
	@Override public @NotNull Function<CompiledVariableList, Direction.Axis>
	compile(VariableMap variableMap) throws CompileFailedException {
		Function<CompiledVariableList, Direction> vec = this.vec.get().compile(variableMap);
		return list->vec.apply(list).getAxis();
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "directionAxis";
	public static final String fullKey = fullPrefix + nameKey;
}
