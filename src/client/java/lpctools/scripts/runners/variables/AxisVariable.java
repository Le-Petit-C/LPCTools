package lpctools.scripts.runners.variables;

import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

public class AxisVariable extends Variable<Direction.Axis>{
	public AxisVariable(@NotNull ILPCConfigReadable parent) {super(parent, nameKey);}
	@Override protected Direction.Axis allocate() {return Direction.Axis.values()[0];}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "axis";
	public static final String fullKey = fullPrefix + nameKey;
	public static final VariableTestPack testPack =
		new VariableTestPack(v->v instanceof AxisVariable, fullKey);
}
