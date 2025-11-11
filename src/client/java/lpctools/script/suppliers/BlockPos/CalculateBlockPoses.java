package lpctools.script.suppliers.BlockPos;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptNullableFunction;
import lpctools.script.suppliers.AbstractSignResultSupplier;
import lpctools.util.Functions;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public class CalculateBlockPoses extends AbstractSignResultSupplier<Functions.BlockPosCalculateSign> implements IBlockPosSupplier {
	protected final SupplierStorage<BlockPos> pos1 = ofStorage(BlockPos.class, new ConstantBlockPos(this),
		Text.translatable("lpctools.script.suppliers.BlockPos.calculateBlockPoses.subSuppliers.pos1.name"), "pos1");
	protected final SupplierStorage<BlockPos> pos2 = ofStorage(BlockPos.class, new ConstantBlockPos(this),
		Text.translatable("lpctools.script.suppliers.BlockPos.calculateBlockPoses.subSuppliers.pos2.name"), "pos2");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(pos1, pos2);
	
	public CalculateBlockPoses(IScriptWithSubScript parent) {super(parent, Functions.ADD, Functions.blockPosCalculateSignInfo, 1);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptNullableFunction<CompileEnvironment.RuntimeVariableMap, BlockPos>
	compile(CompileEnvironment variableMap) {
		var pos1Supplier = pos1.get().compile(variableMap);
		var sign = compareSign;
		var pos2Supplier = pos2.get().compile(variableMap);
		return map->{
			var pos1 = pos1Supplier.scriptApply(map);
			if(pos1 == null) throw ScriptRuntimeException.nullPointer(this);
			var pos2 = pos2Supplier.scriptApply(map);
			if(pos2 == null) throw ScriptRuntimeException.nullPointer(this);
			return sign.calculateBlockPoses(pos1, pos2);
		};
	}
}
