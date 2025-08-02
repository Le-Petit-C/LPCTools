package lpctools.scripts.suppliers.vector3d;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.WrappedThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import lpctools.scripts.utils.choosers.DirectionSupplierChooser;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class Vector3dFromDirection extends WrappedThirdListConfig implements IScriptVector3dSupplier {
	private final DirectionSupplierChooser direction = addConfig(new DirectionSupplierChooser(parent, "direction", this::onValueChanged));
	public Vector3dFromDirection(ILPCConfigReadable parent) {super(parent, nameKey, null);}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		res.add(1, (button, mouseButton) -> direction.openChoose(), ()->fullKey + ".direction", buttonGenericAllocator);
	}
	@Override public @NotNull BiConsumer<CompiledVariableList, Vector3d>
	compileToVector3d(VariableMap variableMap) throws CompileFailedException {
		Function<CompiledVariableList, Direction> direction = this.direction.get().compile(variableMap);
		return (list, res)->{
			Vec3d vec = direction.apply(list).getDoubleVector();
			res.set(vec.getX(), vec.getY(), vec.getZ());
		};
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "fromDirection";
	public static final String fullKey = fullPrefix + nameKey;
	
}
