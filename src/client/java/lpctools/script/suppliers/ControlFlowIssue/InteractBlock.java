package lpctools.script.suppliers.ControlFlowIssue;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class InteractBlock extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IControlFlowIssueSupplier {
	protected final SupplierStorage<Boolean> useOffhand = ofStorage(Boolean.class,
		Component.translatable("lpctools.script.suppliers.controlFlowIssue.interactBlock.subSuppliers.useOffhand.name"), "useOffhand");
	protected final SupplierStorage<Vec3> pos = ofStorage(Vec3.class,
		Component.translatable("lpctools.script.suppliers.controlFlowIssue.interactBlock.subSuppliers.pos.name"), "pos");
	protected final SupplierStorage<Direction> direction = ofStorage(Direction.class,
		Component.translatable("lpctools.script.suppliers.controlFlowIssue.interactBlock.subSuppliers.direction.name"), "direction");
	protected final SupplierStorage<BlockPos> blockPos = ofStorage(BlockPos.class,
		Component.translatable("lpctools.script.suppliers.controlFlowIssue.interactBlock.subSuppliers.blockPos.name"), "blockPos");
	protected final SupplierStorage<Boolean> insideBlock = ofStorage(Boolean.class,
		Component.translatable("lpctools.script.suppliers.controlFlowIssue.interactBlock.subSuppliers.insideBlock.name"), "insideBlock");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(useOffhand, pos, direction, blockPos, insideBlock);
	
	public InteractBlock(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptNotNullSupplier<ControlFlowIssue>
	compileNotNull(CompileEnvironment environment) {
		var compiledUseOffhandSupplier = useOffhand.get().compileCheckedNotNull(environment);
		var compiledPosSupplier = pos.get().compileCheckedNotNull(environment);
		var compiledDirectionSupplier = direction.get().compileCheckedNotNull(environment);
		var compiledBlockPosSupplier = blockPos.get().compileCheckedNotNull(environment);
		var compiledInsideBlock = insideBlock.get().compileCheckedNotNull(environment);
		return map->{
			var mc = Minecraft.getInstance();
			var itm = mc.gameMode;
			var player = mc.player;
			if(itm == null || player == null) return ControlFlowIssue.NO_ISSUE;
			itm.useItemOn(player, compiledUseOffhandSupplier.scriptApply(map) ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND,
				new BlockHitResult(compiledPosSupplier.scriptApply(map), compiledDirectionSupplier.scriptApply(map),
					compiledBlockPosSupplier.scriptApply(map), compiledInsideBlock.scriptApply(map)));
			return ControlFlowIssue.NO_ISSUE;
		};
	}
}
