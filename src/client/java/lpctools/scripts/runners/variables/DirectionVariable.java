package lpctools.scripts.runners.variables;

import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

public class DirectionVariable extends Variable<Direction>{
	public DirectionVariable(@NotNull ILPCConfigReadable parent) {super(parent, nameKey);}
	@Override protected Direction allocate() {return Direction.values()[0];}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "direction";
	public static final String fullKey = fullPrefix + nameKey;
	public static final VariableTestPack testPack =
		new VariableTestPack(v->v instanceof DirectionVariable, fullKey);
}
