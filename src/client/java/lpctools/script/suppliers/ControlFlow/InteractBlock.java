package lpctools.script.suppliers.ControlFlow;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptFunction;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import lpctools.script.suppliers.BlockPos.ConstantBlockPos;
import lpctools.script.suppliers.Boolean.ConstantBoolean;
import lpctools.script.suppliers.Direction.ConstantDirection;
import lpctools.script.suppliers.Vec3d.ConstantVec3d;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public class InteractBlock extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IControlFlowSupplier {
	protected final SupplierStorage<Boolean> useOffhand = ofStorage(Boolean.class, new ConstantBoolean(this),
		Text.translatable("lpctools.script.suppliers.ControlFlowIssue.interactBlock.subSuppliers.useOffhand.name"));
	protected final SupplierStorage<Vec3d> pos = ofStorage(Vec3d.class, new ConstantVec3d(this),
		Text.translatable("lpctools.script.suppliers.ControlFlowIssue.interactBlock.subSuppliers.pos.name"));
	protected final SupplierStorage<Direction> direction = ofStorage(Direction.class, new ConstantDirection(this),
		Text.translatable("lpctools.script.suppliers.ControlFlowIssue.interactBlock.subSuppliers.direction.name"));
	protected final SupplierStorage<BlockPos> blockPos = ofStorage(BlockPos.class, new ConstantBlockPos(this),
		Text.translatable("lpctools.script.suppliers.ControlFlowIssue.interactBlock.subSuppliers.blockPos.name"));
	protected final SupplierStorage<Boolean> insideBlock = ofStorage(Boolean.class, new ConstantBoolean(this),
		Text.translatable("lpctools.script.suppliers.ControlFlowIssue.interactBlock.subSuppliers.insideBlock.name"));
	protected final SubSupplierEntry<?>[] subSuppliers = subSupplierBuilder()
		.addEntry(useOffhand, "useOffhand")
		.addEntry(pos, "pos")
		.addEntry(direction, "direction")
		.addEntry(blockPos, "blockPos")
		.addEntry(insideBlock, "insideBlock")
		.build();
	
	public InteractBlock(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SubSupplierEntry<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptFunction<CompileEnvironment.RuntimeVariableMap, ControlFlowIssue>
	compile(CompileEnvironment variableMap) {
		var compiledUseOffhandSupplier = useOffhand.get().compile(variableMap);
		var compiledPosSupplier = pos.get().compile(variableMap);
		var compiledDirectionSupplier = direction.get().compile(variableMap);
		var compiledBlockPosSupplier = blockPos.get().compile(variableMap);
		var compiledInsideBlock = insideBlock.get().compile(variableMap);
		return map->{
			var mc = MinecraftClient.getInstance();
			var itm = mc.interactionManager;
			var player = mc.player;
			if(itm == null || player == null) return ControlFlowIssue.NO_ISSUE;
			var useOffhand = compiledUseOffhandSupplier.scriptApply(map);
			if(useOffhand == null) throw ScriptRuntimeException.nullPointer(this);
			var pos = compiledPosSupplier.scriptApply(map);
			if(pos == null) throw ScriptRuntimeException.nullPointer(this);
			var direction = compiledDirectionSupplier.scriptApply(map);
			if(direction == null) throw ScriptRuntimeException.nullPointer(this);
			var blockPos = compiledBlockPosSupplier.scriptApply(map);
			if(blockPos == null) throw ScriptRuntimeException.nullPointer(this);
			var insideBlock = compiledInsideBlock.scriptApply(map);
			if(insideBlock == null) throw ScriptRuntimeException.nullPointer(this);
			itm.interactBlock(player, useOffhand ? Hand.OFF_HAND : Hand.MAIN_HAND,
				new BlockHitResult(pos, direction, blockPos, insideBlock));
			return ControlFlowIssue.NO_ISSUE;
		};
	}
}
