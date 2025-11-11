package lpctools.script.suppliers.Block;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptNullableFunction;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import lpctools.script.suppliers.BlockPos.ConstantBlockPos;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class BlockInWorld extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IBlockSupplier {
	protected final SupplierStorage<BlockPos> blockPos = ofStorage(BlockPos.class, new ConstantBlockPos(this),
		Text.translatable("lpctools.script.suppliers.Block.blockInWorld.subSuppliers.blockPos.name"), "blockPos");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(blockPos);
	
	public BlockInWorld(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @org.jetbrains.annotations.NotNull ScriptNullableFunction<CompileEnvironment.RuntimeVariableMap, Block>
	compile(CompileEnvironment variableMap) {
		var booleanSupplier = blockPos.get().compile(variableMap);
		return map->{
			var world = MinecraftClient.getInstance().world;
			if(world != null) {
				var pos = booleanSupplier.scriptApply(map);
				if(pos == null) throw ScriptRuntimeException.nullPointer(this);
				return world.getBlockState(pos).getBlock();
			}
			else return null;
		};
	}
}
