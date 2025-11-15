package lpctools.script.suppliers.Integer;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptIntegerSupplier;
import lpctools.script.suppliers.AbstractSignResultSupplier;
import lpctools.util.Functions;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public class IntegerFromBlockPos extends AbstractSignResultSupplier<Functions.IntegerFromBlockPosFunction> implements IIntegerSupplier {
	protected final SupplierStorage<BlockPos> pos = ofStorage(BlockPos.class,
		Text.translatable("lpctools.script.suppliers.Integer.integerFromBlockPos.subSuppliers.pos.name"), "pos");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(pos);
	
	public IntegerFromBlockPos(IScriptWithSubScript parent) {super(parent, Functions.COORDINATE_X, Functions.integerFromBlockPosFunctionInfo, 1);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptIntegerSupplier
	compileInteger(CompileEnvironment environment) {
		var sign = compareSign;
		var posSupplier = pos.get().compileCheckedNotNull(environment);
		return map->sign.integerFromBlockPos(posSupplier.scriptApply(map));
	}
}
