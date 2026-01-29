package lpctools.script.suppliers.Integer;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptIntegerSupplier;
import lpctools.script.suppliers.AbstractOperatorResultSupplier;
import lpctools.util.operatorUtils.Operators;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public class IntegerFromBlockPos extends AbstractOperatorResultSupplier<Operators.IntegerFromBlockPosFunction> implements IIntegerSupplier {
	protected final SupplierStorage<BlockPos> pos = ofStorage(BlockPos.class,
		Text.translatable("lpctools.script.suppliers.integer.integerFromBlockPos.subSuppliers.pos.name"), "pos");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(pos);
	
	public IntegerFromBlockPos(IScriptWithSubScript parent) {super(parent, Operators.COORDINATE_X, Operators.integerFromBlockPosFunctionInfo, 1);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptIntegerSupplier
	compileInteger(CompileEnvironment environment) {
		var sign = operatorSign;
		var posSupplier = pos.get().compileCheckedNotNull(environment);
		return map->sign.integerFromBlockPos(posSupplier.scriptApply(map));
	}
}
