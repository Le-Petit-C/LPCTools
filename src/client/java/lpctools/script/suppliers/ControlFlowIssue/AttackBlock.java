package lpctools.script.suppliers.ControlFlowIssue;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class AttackBlock extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IControlFlowIssueSupplier {
	protected final SupplierStorage<BlockPos> blockPos = ofStorage(BlockPos.class,
		Component.translatable("lpctools.script.suppliers.controlFlowIssue.attackBlock.subSuppliers.blockPos.name"), "blockPos");
	protected final SupplierStorage<Direction> direction = ofStorage(Direction.class,
		Component.translatable("lpctools.script.suppliers.controlFlowIssue.attackBlock.subSuppliers.direction.name"), "direction");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(blockPos, direction);
	
	public AttackBlock(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptNotNullSupplier<ControlFlowIssue>
	compileNotNull(CompileEnvironment environment) {
		var compiledBlockPosSupplier = blockPos.get().compileCheckedNotNull(environment);
		var compiledDirectionSupplier = direction.get().compileCheckedNotNull(environment);
		return map->{
			var itm = Minecraft.getInstance().gameMode;
			if (itm != null) itm.startDestroyBlock(compiledBlockPosSupplier.scriptApply(map), compiledDirectionSupplier.scriptApply(map));
			return ControlFlowIssue.NO_ISSUE;
		};
	}
}
