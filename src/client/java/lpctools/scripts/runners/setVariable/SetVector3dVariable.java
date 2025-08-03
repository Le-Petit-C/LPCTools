package lpctools.scripts.runners.setVariable;

import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.runners.variables.*;
import lpctools.scripts.suppliers.vector3d.IScriptVector3dSupplier;
import lpctools.scripts.utils.choosers.Vector3dSupplierChooser;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SetVector3dVariable extends SetVariable<IScriptVector3dSupplier>{
	public SetVector3dVariable(@NotNull ILPCConfigReadable parent) {
		super(parent, nameKey, new Vector3dSupplierChooser(parent, "chooser", null));
	}
	@Override protected VariableTestPack testPack() {return Vector3dVariable.testPack;}
	@Override protected @NotNull Consumer<CompiledVariableList>
	setValue(VariableMap variableMap, IScriptVector3dSupplier src, int index) throws CompileFailedException {
		BiConsumer<CompiledVariableList, Vector3d> func = src.compileToVector3d(variableMap);
		return list->func.accept(list, list.getVariable(index));
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "setVector3dVariable";
	public static final String fullKey = fullPrefix + nameKey;
}
