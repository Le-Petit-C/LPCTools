package lpctools.script.suppliers.ControlFlowIssue;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public class InteractBlock extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IControlFlowIssueSupplier {
	protected final SupplierStorage<Boolean> useOffhand = ofStorage(Boolean.class,
		Text.translatable("lpctools.script.suppliers.controlFlowIssue.interactBlock.subSuppliers.useOffhand.name"), "useOffhand");
	protected final SupplierStorage<Vec3d> pos = ofStorage(Vec3d.class,
		Text.translatable("lpctools.script.suppliers.controlFlowIssue.interactBlock.subSuppliers.pos.name"), "pos");
	protected final SupplierStorage<Direction> direction = ofStorage(Direction.class,
		Text.translatable("lpctools.script.suppliers.controlFlowIssue.interactBlock.subSuppliers.direction.name"), "direction");
	protected final SupplierStorage<BlockPos> blockPos = ofStorage(BlockPos.class,
		Text.translatable("lpctools.script.suppliers.controlFlowIssue.interactBlock.subSuppliers.blockPos.name"), "blockPos");
	protected final SupplierStorage<Boolean> insideBlock = ofStorage(Boolean.class,
		Text.translatable("lpctools.script.suppliers.controlFlowIssue.interactBlock.subSuppliers.insideBlock.name"), "insideBlock");
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
			var mc = MinecraftClient.getInstance();
			var itm = mc.interactionManager;
			var player = mc.player;
			if(itm == null || player == null) return ControlFlowIssue.NO_ISSUE;
			itm.interactBlock(player, compiledUseOffhandSupplier.scriptApply(map) ? Hand.OFF_HAND : Hand.MAIN_HAND,
				new BlockHitResult(compiledPosSupplier.scriptApply(map), compiledDirectionSupplier.scriptApply(map),
					compiledBlockPosSupplier.scriptApply(map), compiledInsideBlock.scriptApply(map)));
			return ControlFlowIssue.NO_ISSUE;
		};
	}
}
