package lpctools.script.suppliers.BlockPos;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import lpctools.script.suppliers.Direction.ConstantDirection;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

public class DirectionVector extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IBlockPosSupplier {
	protected final SupplierStorage<Direction> direction = ofStorage(Direction.class, new ConstantDirection(this),
		Text.translatable("lpctools.script.suppliers.BlockPos.directionVector.subSuppliers.direction.name"), "direction");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(direction);
	
	public DirectionVector(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptNotNullSupplier<BlockPos>
	compileNotNull(CompileEnvironment environment) {
		var compiledEntitySupplier = direction.get().compileCheckedNotNull(environment);
		return map->new BlockPos(compiledEntitySupplier.scriptApply(map).getVector());
	}
}
