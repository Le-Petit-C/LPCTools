package lpctools.script.suppliers.BlockPos;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.suppliers.AbstractSignResultSupplier;
import lpctools.util.Functions;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public class CalculateBlockPoses extends AbstractSignResultSupplier<Functions.BlockPosCalculateSign> implements IBlockPosSupplier {
	protected final SupplierStorage<BlockPos> pos1 = ofStorage(BlockPos.class,
		Text.translatable("lpctools.script.suppliers.BlockPos.calculateBlockPoses.subSuppliers.pos1.name"), "pos1");
	protected final SupplierStorage<BlockPos> pos2 = ofStorage(BlockPos.class,
		Text.translatable("lpctools.script.suppliers.BlockPos.calculateBlockPoses.subSuppliers.pos2.name"), "pos2");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(pos1, pos2);
	
	public CalculateBlockPoses(IScriptWithSubScript parent) {super(parent, Functions.ADD, Functions.blockPosCalculateSignInfo, 1);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptNotNullSupplier<BlockPos>
	compileNotNull(CompileEnvironment environment) {
		var pos1Supplier = pos1.get().compileCheckedNotNull(environment);
		var sign = compareSign;
		var pos2Supplier = pos2.get().compileCheckedNotNull(environment);
		return map->sign.calculateBlockPoses(pos1Supplier.scriptApply(map), pos2Supplier.scriptApply(map));
	}
}
