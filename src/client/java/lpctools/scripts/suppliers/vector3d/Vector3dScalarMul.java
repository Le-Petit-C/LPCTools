package lpctools.scripts.suppliers.vector3d;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.WrappedThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import lpctools.scripts.utils.choosers.DoubleSupplierChooser;
import lpctools.scripts.utils.choosers.Vector3dSupplierChooser;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

import java.util.function.BiConsumer;
import java.util.function.ToDoubleFunction;

public class Vector3dScalarMul extends WrappedThirdListConfig implements IScriptVector3dSupplier {
	private final DoubleSupplierChooser scalar = addConfig(new DoubleSupplierChooser(parent, "scalar", this::onValueChanged));
	private final Vector3dSupplierChooser pos = addConfig(new Vector3dSupplierChooser(parent, "pos", this::onValueChanged));
	public Vector3dScalarMul(ILPCConfigReadable parent) {super(parent, nameKey, null);}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		res.add(1, (button, mouseButton) -> scalar.openChoose(), ()->fullKey + ".scalar", buttonGenericAllocator);
		res.add(1, (button, mouseButton) -> pos.openChoose(), ()->fullKey + ".pos", buttonGenericAllocator);
	}
	@Override public @NotNull BiConsumer<CompiledVariableList, Vector3d>
	compileToVector3d(VariableMap variableMap) throws CompileFailedException {
		ToDoubleFunction<CompiledVariableList> scalar = this.scalar.get().compileToDouble(variableMap);
		BiConsumer<CompiledVariableList, Vector3d> pos = this.pos.get().compileToVector3d(variableMap);
		return (list, res)->{
			double k = scalar.applyAsDouble(list);
			pos.accept(list, res);
			res.mul(k);
		};
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "scalar";
	public static final String fullKey = fullPrefix + nameKey;
}
