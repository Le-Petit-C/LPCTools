package lpctools.scripts.suppliers.vector3d;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.WrappedThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import lpctools.scripts.utils.choosers.DoubleSupplierChooser;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

import java.util.function.BiConsumer;
import java.util.function.ToDoubleFunction;

public class Vector3dFromDoubles extends WrappedThirdListConfig implements IScriptVector3dSupplier {
	private final DoubleSupplierChooser x = addConfig(new DoubleSupplierChooser(parent, "x", this::onValueChanged));
	private final DoubleSupplierChooser y = addConfig(new DoubleSupplierChooser(parent, "y", this::onValueChanged));
	private final DoubleSupplierChooser z = addConfig(new DoubleSupplierChooser(parent, "z", this::onValueChanged));
	public Vector3dFromDoubles(ILPCConfigReadable parent) {super(parent, nameKey, null);}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		res.add(1, (button, mouseButton) -> x.openChoose(), ()->fullKey + ".x", buttonGenericAllocator);
		res.add(1, (button, mouseButton) -> y.openChoose(), ()->fullKey + ".y", buttonGenericAllocator);
		res.add(1, (button, mouseButton) -> z.openChoose(), ()->fullKey + ".z", buttonGenericAllocator);
	}
	@Override public @NotNull BiConsumer<CompiledVariableList, Vector3d>
	compileToVector3d(VariableMap variableMap) throws CompileFailedException {
		ToDoubleFunction<CompiledVariableList> x = this.x.get().compileToDouble(variableMap);
		ToDoubleFunction<CompiledVariableList> y = this.y.get().compileToDouble(variableMap);
		ToDoubleFunction<CompiledVariableList> z = this.z.get().compileToDouble(variableMap);
		return (list, pos)->pos.set(x.applyAsDouble(list), y.applyAsDouble(list), z.applyAsDouble(list));
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "fromDoubles";
	public static final String fullKey = fullPrefix + nameKey;
}
