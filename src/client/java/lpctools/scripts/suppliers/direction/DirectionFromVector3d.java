package lpctools.scripts.suppliers.direction;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.WrappedThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import lpctools.scripts.utils.choosers.Vector3dSupplierChooser;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class DirectionFromVector3d extends WrappedThirdListConfig implements IScriptDirectionSupplier {
	private final Vector3dSupplierChooser vec = addConfig(new Vector3dSupplierChooser(parent, "vec", this::onValueChanged));
	public DirectionFromVector3d(ILPCConfigReadable parent) {super(parent, nameKey, null);}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		res.add(1, (button, mouseButton) -> vec.openChoose(), ()->fullKey + ".vec", buttonGenericAllocator);
	}
	@Override public @NotNull Function<CompiledVariableList, Direction>
	compile(VariableMap variableMap) throws CompileFailedException {
		BiConsumer<CompiledVariableList, Vector3d> vec = this.vec.get().compileToVector3d(variableMap);
		Vector3d buf = new Vector3d();
		return list->{
			vec.accept(list, buf);
			return Direction.getFacing(buf.x, buf.y, buf.z);
		};
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "fromVector3d";
	public static final String fullKey = fullPrefix + nameKey;
}
