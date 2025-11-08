package lpctools.script.suppliers.Integer;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptFunction;
import lpctools.script.suppliers.AbstractSignResultSupplier;
import lpctools.script.suppliers.BlockPos.ConstantBlockPos;
import lpctools.util.Functions;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public class IntegerFromBlockPos extends AbstractSignResultSupplier<Functions.IntegerFromBlockPosFunction> implements IIntegerSupplier {
	protected final SupplierStorage<BlockPos> pos = ofStorage(BlockPos.class, new ConstantBlockPos(this),
		Text.translatable("lpctools.script.suppliers.Integer.integerFromBlockPos.subSuppliers.pos.name"), "pos");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(pos);
	
	public IntegerFromBlockPos(IScriptWithSubScript parent) {super(parent, Functions.COORDINATE_X, Functions.integerFromBlockPosFunctionInfo, 1);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptFunction<CompileEnvironment.RuntimeVariableMap, Integer>
	compile(CompileEnvironment variableMap) {
		var sign = compareSign;
		var posSupplier = pos.get().compile(variableMap);
		return map->{
			var pos = posSupplier.scriptApply(map);
			if(pos == null) throw ScriptRuntimeException.nullPointer(this);
			return sign.integerFromBlockPos(pos);
		};
	}
}
