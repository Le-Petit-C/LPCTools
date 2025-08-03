package lpctools.scripts.runners.variables;

import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

public class Vector3dVariable extends Variable<Vector3d>{
	public Vector3dVariable(@NotNull ILPCConfigReadable parent) {super(parent, nameKey);}
	@Override protected Vector3d allocate() {return new Vector3d();}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "vector3d";
	public static final String fullKey = fullPrefix + nameKey;
	public static final VariableTestPack testPack =
		new VariableTestPack(v->v instanceof Vector3dVariable, fullKey);
}
