package lpctools.scripts.runners.variables;

import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.jetbrains.annotations.NotNull;

public class DoubleVariable extends Variable<MutableDouble>{
	public DoubleVariable(@NotNull ILPCConfigReadable parent) {super(parent, nameKey);}
	@Override protected MutableDouble allocate() {return new MutableDouble();}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "double";
	public static final String fullKey = fullPrefix + nameKey;
	public static final VariableTestPack testPack =
		new VariableTestPack(v->v instanceof DoubleVariable, fullKey);
}
