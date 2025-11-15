package lpctools.script.suppliers.Vec3d;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public class Vec3dFromBlockPos extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IVec3dSupplier {
	protected final SupplierStorage<BlockPos> blockPos = ofStorage(BlockPos.class,
		Text.translatable("lpctools.script.suppliers.Vec3d.vec3dFromBlockPos.subSuppliers.blockPos.name"), "blockPos");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(blockPos);
	
	public Vec3dFromBlockPos(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers(){ return subSuppliers; }
	
	@Override public @NotNull ScriptNotNullSupplier<Vec3d>
	compileNotNull(CompileEnvironment environment) {
		var compiledEntitySupplier = blockPos.get().compileCheckedNotNull(environment);
		return map->Vec3d.of(compiledEntitySupplier.scriptApply(map));
	}
}
