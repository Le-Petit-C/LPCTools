package lpctools.scripts.runners.variables;

import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;

public class IntVariable extends Variable<MutableInt>{
	public IntVariable(@NotNull ILPCConfigReadable parent) {super(parent, nameKey);}
	@Override protected MutableInt allocate() {return new MutableInt();}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "int";
	public static final String fullKey = fullPrefix + nameKey;
}
