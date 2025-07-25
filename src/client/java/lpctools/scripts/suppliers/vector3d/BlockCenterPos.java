package lpctools.scripts.suppliers.vector3d;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.WrappedThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.choosers.BlockPosSupplierChooser;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

import java.util.function.BiConsumer;

public class BlockCenterPos extends WrappedThirdListConfig implements IScriptVector3dSupplier {
	private final BlockPosSupplierChooser pos = addConfig(new BlockPosSupplierChooser(parent, "pos", this::onValueChanged));
	public BlockCenterPos(ILPCConfigReadable parent) {super(parent, nameKey, null);}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		res.add(1, (button, mouseButton) -> pos.openChoose(), ()->fullKey + ".pos", buttonGenericAllocator);
	}
	@Override public @NotNull BiConsumer<CompiledVariableList, Vector3d>
	compileToVector3d(VariableMap variableMap) throws CompileFailedException {
		BiConsumer<CompiledVariableList, BlockPos.Mutable> pos = this.pos.get().compileToBlockPos(variableMap);
		BlockPos.Mutable buf = new BlockPos.Mutable();
		return (list, res)->{
			pos.accept(list, buf);
			res.set(buf.getX() + 0.5, buf.getY() + 0.5, buf.getZ() + 0.5);
		};
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "blockCenterPos";
	public static final String fullKey = fullPrefix + nameKey;
	
}
