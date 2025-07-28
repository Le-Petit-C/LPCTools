package lpctools.scripts.suppliers.vector3d;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.WrappedThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import lpctools.scripts.utils.choosers.Vector3dSupplierChooser;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

import java.util.function.BiConsumer;

public class Vector3dCrossProduct extends WrappedThirdListConfig implements IScriptVector3dSupplier {
	private final Vector3dSupplierChooser vec1 = addConfig(new Vector3dSupplierChooser(parent, "vec1", this::onValueChanged));
	private final Vector3dSupplierChooser vec2 = addConfig(new Vector3dSupplierChooser(parent, "vec2", this::onValueChanged));
	public Vector3dCrossProduct(ILPCConfigReadable parent) {super(parent, nameKey, null);}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		res.add(1, (button, mouseButton) -> vec1.openChoose(), ()->fullKey + ".vec1", buttonGenericAllocator);
		res.add(1, (button, mouseButton) -> vec2.openChoose(), ()->fullKey + ".vec2", buttonGenericAllocator);
	}
	@Override public @NotNull BiConsumer<CompiledVariableList, Vector3d>
	compileToVector3d(VariableMap variableMap) throws CompileFailedException {
		BiConsumer<CompiledVariableList, Vector3d> vec1 = this.vec1.get().compileToVector3d(variableMap);
		BiConsumer<CompiledVariableList, Vector3d> vec2 = this.vec2.get().compileToVector3d(variableMap);
		Vector3d buf = new Vector3d();
		return (list, pos)->{
			vec1.accept(list, buf);
			vec2.accept(list, pos);
			buf.cross(pos, pos);
		};
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "crossProduct";
	public static final String fullKey = fullPrefix + nameKey;
}
