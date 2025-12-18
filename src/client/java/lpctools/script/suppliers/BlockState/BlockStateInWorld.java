package lpctools.script.suppliers.BlockState;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public class BlockStateInWorld extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IBlockStateSupplier {
	protected final SupplierStorage<BlockPos> blockPos = ofStorage(BlockPos.class,
		Text.translatable("lpctools.script.suppliers.blockState.blockStateInWorld.subSuppliers.blockPos.name"), "blockPos");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(blockPos);
	
	public BlockStateInWorld(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptNotNullSupplier<BlockState>
	compileNotNull(CompileEnvironment environment) {
		var blockPosSupplier = blockPos.get().compileCheckedNotNull(environment);
		return map->{
			var world = MinecraftClient.getInstance().world;
			if(world != null) {
				var pos = blockPosSupplier.scriptApply(map);
				return world.getBlockState(pos);
			}
			else return Blocks.VOID_AIR.getDefaultState();
		};
	}
}
