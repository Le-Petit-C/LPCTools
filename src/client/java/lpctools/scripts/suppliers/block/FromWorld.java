package lpctools.scripts.suppliers.block;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.WrappedThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.choosers.BlockPosSupplierChooser;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class FromWorld extends WrappedThirdListConfig implements IScriptBlockSupplier {
	private final BlockPosSupplierChooser pos;
	public FromWorld(ILPCConfigReadable parent) {
		super(parent, nameKey, null);
		pos = addConfig(new BlockPosSupplierChooser(parent, "pos", this::onValueChanged));
	}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		res.add(1, (button, mouseButton) -> pos.openChoose(), ()->fullKey + ".pos", buttonGenericAllocator);
	}
	@Override public @NotNull Function<CompiledVariableList, Block>
	compile(VariableMap variableMap) throws CompileFailedException {
		BiConsumer<CompiledVariableList, BlockPos.Mutable> pos;
		pos = this.pos.get().compileToBlockPos(variableMap);
		BlockPos.Mutable buf = new BlockPos.Mutable();
		return list->{
			ClientWorld world = MinecraftClient.getInstance().world;
			pos.accept(list, buf);
			if(world == null) return Blocks.AIR;
			else return world.getBlockState(buf).getBlock();
		};
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "fromWorld";
	public static final String fullKey = fullPrefix + nameKey;
}
