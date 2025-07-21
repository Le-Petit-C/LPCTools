package lpctools.scripts.runners.variables;

import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;

public class IntVariable extends Variable<Number>{
	public IntVariable(@NotNull ILPCConfigReadable parent, @NotNull String nameKey) {super(parent, nameKey);}
	@Override protected Mutable<Number> allocate() {return new MutableInt();}
}
