package lpctools.script.suppliers.Block;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import lpctools.script.suppliers.BlockPos.ConstantBlockPos;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public class BlockInWorld extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IBlockSupplier {
	protected final SupplierStorage<BlockPos> blockPos = ofStorage(BlockPos.class, new ConstantBlockPos(this),
		Text.translatable("lpctools.script.suppliers.Block.blockInWorld.subSuppliers.blockPos.name"), "blockPos");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(blockPos);
	
	public BlockInWorld(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptNotNullSupplier<Block>
	compileNotNull(CompileEnvironment environment) {
		var booleanSupplier = blockPos.get().compileCheckedNotNull(environment);
		return map->{
			var world = MinecraftClient.getInstance().world;
			if(world != null) {
				var pos = booleanSupplier.scriptApply(map);
				return world.getBlockState(pos).getBlock();
			}
			else return Blocks.VOID_AIR;
		};
	}
}
