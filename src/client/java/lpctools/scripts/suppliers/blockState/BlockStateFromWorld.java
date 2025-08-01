package lpctools.scripts.suppliers.blockState;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.WrappedThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import lpctools.scripts.utils.choosers.BlockPosSupplierChooser;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class BlockStateFromWorld extends WrappedThirdListConfig implements IScriptBlockStateSupplier {
	private final BlockPosSupplierChooser blockPos;
	public BlockStateFromWorld(ILPCConfigReadable parent) {
		super(parent, nameKey, null);
		blockPos = addConfig(new BlockPosSupplierChooser(parent, "blockPos", this::onValueChanged));
	}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		res.add(1, (button, mouseButton) -> blockPos.openChoose(), ()->fullKey + ".blockPos", buttonGenericAllocator);
	}
	@Override public @NotNull Function<CompiledVariableList, BlockState>
	compile(VariableMap variableMap) throws CompileFailedException {
		BiConsumer<CompiledVariableList, BlockPos.Mutable> pos;
		pos = this.blockPos.get().compileToBlockPos(variableMap);
		BlockPos.Mutable buf = new BlockPos.Mutable();
		return list->{
			ClientWorld world = MinecraftClient.getInstance().world;
			pos.accept(list, buf);
			if(world == null) return Blocks.AIR.getDefaultState();
			else return world.getBlockState(buf);
		};
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "fromWorld";
	public static final String fullKey = fullPrefix + nameKey;
}
