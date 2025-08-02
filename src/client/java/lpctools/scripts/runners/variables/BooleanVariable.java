package lpctools.scripts.runners.variables;

import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.NotNull;

public class BooleanVariable extends Variable<MutableBoolean>{
	public BooleanVariable(@NotNull ILPCConfigReadable parent) {super(parent, nameKey);}
	@Override protected MutableBoolean allocate() {return new MutableBoolean();}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "boolean";
	public static final String fullKey = fullPrefix + nameKey;
	public static final VariableTestPack testPack =
		new VariableTestPack(v->v instanceof BooleanVariable, fullKey);
}
