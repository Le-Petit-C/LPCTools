package lpctools.script.suppliers.BlockPos;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptNullableFunction;
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
	
	@Override public @NotNull ScriptNullableFunction<CompileEnvironment.RuntimeVariableMap, BlockPos>
	compile(CompileEnvironment variableMap) {
		var compiledXSupplier = x.get().compile(variableMap);
		var compiledYSupplier = y.get().compile(variableMap);
		var compiledZSupplier = z.get().compile(variableMap);
		return map->{
			var x = compiledXSupplier.scriptApply(map);
			if(x == null) throw ScriptRuntimeException.nullPointer(this);
			var y = compiledYSupplier.scriptApply(map);
			if(y == null) throw ScriptRuntimeException.nullPointer(this);
			var z = compiledZSupplier.scriptApply(map);
			if(z == null) throw ScriptRuntimeException.nullPointer(this);
			return new BlockPos(x, y, z);
		};
	}
}
