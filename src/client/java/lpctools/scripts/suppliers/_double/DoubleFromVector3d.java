package lpctools.scripts.suppliers._double;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.WrappedThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import lpctools.scripts.utils.choosers.Axis3SupplierChooser;
import lpctools.scripts.utils.choosers.Vector3dSupplierChooser;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

import java.util.function.*;

public class DoubleFromVector3d extends WrappedThirdListConfig implements IScriptDoubleSupplier {
	private final Vector3dSupplierChooser vec = addConfig(new Vector3dSupplierChooser(parent, "vec", this::onValueChanged));
	private final Axis3SupplierChooser axis = addConfig(new Axis3SupplierChooser(parent, "axis", this::onValueChanged));
	public DoubleFromVector3d(ILPCConfigReadable parent) {super(parent, nameKey, null);}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		res.add(1, (button, mouseButton) -> vec.openChoose(), ()->fullKey + ".vec", buttonGenericAllocator);
		res.add(1, (button, mouseButton) -> axis.openChoose(), ()->fullKey + ".axis", buttonGenericAllocator);
	}
	@Override public @NotNull ToDoubleFunction<CompiledVariableList>
	compileToDouble(VariableMap variableMap) throws CompileFailedException {
		BiConsumer<CompiledVariableList, Vector3d> vec = this.vec.get().compileToVector3d(variableMap);
		Function<CompiledVariableList, Direction.Axis> axis = this.axis.get().compile(variableMap);
		Vector3d vecBuf = new Vector3d();
		return list->{
			vec.accept(list, vecBuf);
			return axis.apply(list).choose(vecBuf.x, vecBuf.y, vecBuf.z);
		};
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "fromVector3d";
	public static final String fullKey = fullPrefix + nameKey;
}
