package lpctools.scripts.suppliers.direction;

import fi.dy.masa.litematica.util.ToBooleanFunction;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.WrappedThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import lpctools.scripts.utils.choosers.AxisSupplierChooser;
import lpctools.scripts.utils.choosers.BooleanSupplierChooser;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class DirectionFromAxis extends WrappedThirdListConfig implements IScriptDirectionSupplier {
	private final AxisSupplierChooser axis = addConfig(new AxisSupplierChooser(parent, "axis", this::onValueChanged));
	private final BooleanSupplierChooser positive = addConfig(new BooleanSupplierChooser(parent, "positive", this::onValueChanged));
	public DirectionFromAxis(ILPCConfigReadable parent) {super(parent, nameKey, null);}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		res.add(1, (button, mouseButton) -> axis.openChoose(), ()->fullKey + ".axis", buttonGenericAllocator);
		res.add(1, (button, mouseButton) -> positive.openChoose(), ()->fullKey + ".positive", buttonGenericAllocator);
	}
	@Override public @NotNull Function<CompiledVariableList, Direction>
	compile(VariableMap variableMap) throws CompileFailedException {
		Function<CompiledVariableList, Direction.Axis> axis = this.axis.get().compile(variableMap);
		ToBooleanFunction<CompiledVariableList> positive = this.positive.get().compileToBoolean(variableMap);
		return list->{
			Direction.Axis _axis = axis.apply(list);
			return Direction.get(positive.applyAsBoolean(list) ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE, _axis);
		};
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "fromAxis";
	public static final String fullKey = fullPrefix + nameKey;
}
