package lpctools.script.suppliers.BlockPos;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptNullableFunction;
import lpctools.script.suppliers.AbstractSignResultSupplier;
import lpctools.script.suppliers.Vec3d.ConstantVec3d;
import lpctools.util.Functions;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public class BlockPosFromVec3d extends AbstractSignResultSupplier<Functions.Vec3d2BlockPosFunction> implements IBlockPosSupplier {
	protected final SupplierStorage<Vec3d> vec = ofStorage(Vec3d.class, new ConstantVec3d(this),
		Text.translatable("lpctools.script.suppliers.BlockPos.blockPosFromVec3d.subSuppliers.vec.name"), "vec");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(vec);
	
	public BlockPosFromVec3d(IScriptWithSubScript parent) {super(parent, Functions.FLOOR, Functions.vec3d2BlockPosFunctionInfo, 0);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptNullableFunction<CompileEnvironment.RuntimeVariableMap, BlockPos>
	compile(CompileEnvironment variableMap) {
		var sign = compareSign;
		var vecSupplier = vec.get().compile(variableMap);
		return map->{
			var vec = vecSupplier.scriptApply(map);
			if(vec == null) throw ScriptRuntimeException.nullPointer(this);
			return sign.blockPosFromVec3d(vec);
		};
	}
}
