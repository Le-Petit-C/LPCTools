package lpctools.script.suppliers.BlockPos;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import lpctools.script.suppliers.Integer.ConstantInteger;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public class BlockPosFromCoordinates extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IBlockPosSupplier {
	protected final SupplierStorage<Integer> x = ofStorage(Integer.class, new ConstantInteger(this),
		Text.translatable("lpctools.script.suppliers.BlockPos.blockPosFromCoordinates.subSuppliers.x.name"), "x");
	protected final SupplierStorage<Integer> y = ofStorage(Integer.class, new ConstantInteger(this),
		Text.translatable("lpctools.script.suppliers.BlockPos.blockPosFromCoordinates.subSuppliers.y.name"), "y");
	protected final SupplierStorage<Integer> z = ofStorage(Integer.class, new ConstantInteger(this),
		Text.translatable("lpctools.script.suppliers.BlockPos.blockPosFromCoordinates.subSuppliers.z.name"), "z");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(x);
	
	public BlockPosFromCoordinates(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptNotNullSupplier<BlockPos>
	compileNotNull(CompileEnvironment environment) {
		var compiledXSupplier = x.get().compileCheckedNotNull(environment);
		var compiledYSupplier = y.get().compileCheckedNotNull(environment);
		var compiledZSupplier = z.get().compileCheckedNotNull(environment);
		return map->new BlockPos(compiledXSupplier.scriptApply(map), compiledYSupplier.scriptApply(map), compiledZSupplier.scriptApply(map));
	}
}
