package lpctools.scripts.suppliers.vector3d;

import lpctools.scripts.CompileFailedException;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import lpctools.scripts.suppliers.IScriptSupplier;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

import java.util.function.BiConsumer;
import java.util.function.Function;

public interface IScriptVector3dSupplier extends IScriptSupplier<Vector3d> {
	String fullPrefix = IScriptSupplier.fullPrefix + "vector3d.";
	@Override @Deprecated @NotNull default Function<CompiledVariableList, Vector3d>
	compile(VariableMap variableMap) throws CompileFailedException{
		BiConsumer<CompiledVariableList, Vector3d> func = compileToVector3d(variableMap);
		return list->{
			Vector3d res = new Vector3d();
			func.accept(list, res);
			return res;
		};
	}
	@NotNull BiConsumer<CompiledVariableList, Vector3d> compileToVector3d(VariableMap variableMap) throws CompileFailedException;
}
