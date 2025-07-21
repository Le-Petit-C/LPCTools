package lpctools.scripts.runners.variables;

import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.NotNull;

public class BlockPosVariable extends Variable<BlockPos>{
	public BlockPosVariable(@NotNull ILPCConfigReadable parent, @NotNull String nameKey) {super(parent, nameKey);}
	@Override protected Mutable<BlockPos> allocate() {return new MutableObject<>();}
}
