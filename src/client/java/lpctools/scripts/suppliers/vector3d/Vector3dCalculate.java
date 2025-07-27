package lpctools.scripts.suppliers.vector3d;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.WrappedThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.choosers.Vector3dSupplierChooser;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import lpctools.scripts.utils.operators.ExtraOperatorConfig;
import lpctools.scripts.utils.operators.Operators;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

import java.util.function.BiConsumer;

public class Vector3dCalculate extends WrappedThirdListConfig implements IScriptVector3dSupplier {
	private final Vector3dSupplierChooser vec1 = addConfig(new Vector3dSupplierChooser(parent, "vec1", this::onValueChanged));
	private final ExtraOperatorConfig operator = addConfig(new ExtraOperatorConfig(this));
	private final Vector3dSupplierChooser vec2 = addConfig(new Vector3dSupplierChooser(parent, "vec2", this::onValueChanged));
	public Vector3dCalculate(ILPCConfigReadable parent) {super(parent, nameKey, null);}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		res.add(1, (button, mouseButton) -> vec1.openChoose(), ()->fullKey + ".vec1", buttonGenericAllocator);
		res.add(1, (button, mouseButton) -> vec2.openChoose(), ()->fullKey + ".vec2", buttonGenericAllocator);
	}
	@Override public @NotNull BiConsumer<CompiledVariableList, Vector3d>
	compileToVector3d(VariableMap variableMap) throws CompileFailedException {
		BiConsumer<CompiledVariableList, Vector3d> vec1 = this.vec1.get().compileToVector3d(variableMap);
		Operators.IExtraOperator operator = this.operator.get();
		BiConsumer<CompiledVariableList, Vector3d> vec2 = this.vec2.get().compileToVector3d(variableMap);
		Vector3d buf1 = new Vector3d();
		Vector3d buf2 = new Vector3d();
		return (list, pos)->{
			vec1.accept(list, buf1);
			vec2.accept(list, buf2);
			operator.operate(buf1, buf2, pos);
		};
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "vector3dCalculate";
	public static final String fullKey = fullPrefix + nameKey;
}
