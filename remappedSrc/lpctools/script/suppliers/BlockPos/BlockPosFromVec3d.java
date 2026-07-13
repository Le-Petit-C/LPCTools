package lpctools.script.suppliers.BlockPos;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.suppliers.AbstractOperatorResultSupplier;
import lpctools.util.operatorUtils.Operators;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class BlockPosFromVec3d extends AbstractOperatorResultSupplier<Operators.Vec3d2BlockPosFunction> implements IBlockPosSupplier {
	protected final SupplierStorage<Vec3> vec = ofStorage(Vec3.class,
		Component.translatable("lpctools.script.suppliers.blockPos.blockPosFromVec3d.subSuppliers.vec.name"), "vec");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(vec);
	
	public BlockPosFromVec3d(IScriptWithSubScript parent) {super(parent, Operators.FLOOR, Operators.vec3d2BlockPosFunctionInfo, 0);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptNotNullSupplier<BlockPos>
	compileNotNull(CompileEnvironment environment) {
		var sign = operatorSign;
		var vecSupplier = vec.get().compileCheckedNotNull(environment);
		return map->sign.blockPosFromVec3d(vecSupplier.scriptApply(map));
	}
}
