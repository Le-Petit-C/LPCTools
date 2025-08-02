package lpctools.scripts.suppliers.blockPos;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.WrappedThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import lpctools.scripts.utils.choosers.Vector3dSupplierChooser;
import lpctools.scripts.utils.rounding.RoundingMethod;
import lpctools.scripts.utils.rounding.RoundingMethodConfig;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

import java.util.function.BiConsumer;

public class RoundingVector3d extends WrappedThirdListConfig implements IScriptBlockPosSupplier {
	private final Vector3dSupplierChooser vec = addConfig(new Vector3dSupplierChooser(parent, "vec", null));
	private final RoundingMethodConfig method = addConfig(new RoundingMethodConfig(this));
	public RoundingVector3d(ILPCConfigReadable parent) {super(parent, nameKey, null);}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		res.add(1, (button, mouseButton) -> vec.openChoose(), ()->fullKey + ".vec", buttonGenericAllocator);
	}
	@Override public @NotNull BiConsumer<CompiledVariableList, BlockPos.Mutable>
	compileToBlockPos(VariableMap variableMap) throws CompileFailedException {
		BiConsumer<CompiledVariableList, Vector3d> vec = this.vec.get().compileToVector3d(variableMap);
		RoundingMethod method = this.method.get();
		Vector3d buf = new Vector3d();
		return (list, pos)->{
			vec.accept(list, buf);
			pos.set(method.round(buf.x), method.round(buf.y), method.round(buf.z));
		};
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "roundingVector3d";
	public static final String fullKey = fullPrefix + nameKey;
}
