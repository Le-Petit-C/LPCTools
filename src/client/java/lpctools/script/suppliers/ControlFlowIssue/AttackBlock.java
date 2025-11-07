package lpctools.script.suppliers.ControlFlowIssue;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptFunction;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import lpctools.script.suppliers.BlockPos.ConstantBlockPos;
import lpctools.script.suppliers.Direction.ConstantDirection;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

public class AttackBlock extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IControlFlowSupplier {
	protected final SupplierStorage<BlockPos> blockPos = ofStorage(BlockPos.class, new ConstantBlockPos(this),
		Text.translatable("lpctools.script.suppliers.ControlFlowIssue.attackBlock.subSuppliers.blockPos.name"), "blockPos");
	protected final SupplierStorage<Direction> direction = ofStorage(Direction.class, new ConstantDirection(this),
		Text.translatable("lpctools.script.suppliers.ControlFlowIssue.attackBlock.subSuppliers.direction.name"), "direction");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(blockPos, direction);
	
	public AttackBlock(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptFunction<CompileEnvironment.RuntimeVariableMap, ControlFlowIssue>
	compile(CompileEnvironment variableMap) {
		var compiledBlockPosSupplier = blockPos.get().compile(variableMap);
		var compiledDirectionSupplier = direction.get().compile(variableMap);
		return map->{
			BlockPos blockPos = compiledBlockPosSupplier.scriptApply(map);
			if(blockPos == null) throw ScriptRuntimeException.nullPointer(this);
			Direction direction = compiledDirectionSupplier.scriptApply(map);
			if(direction == null) throw ScriptRuntimeException.nullPointer(this);
			var itm = MinecraftClient.getInstance().interactionManager;
			if (itm != null) itm.attackBlock(blockPos, direction);
			return ControlFlowIssue.NO_ISSUE;
		};
	}
}
