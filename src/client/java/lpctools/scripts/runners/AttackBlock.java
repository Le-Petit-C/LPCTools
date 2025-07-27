package lpctools.scripts.runners;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.WrappedThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.choosers.BlockPosSupplierChooser;
import lpctools.scripts.choosers.Direction6SupplierChooser;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class AttackBlock extends WrappedThirdListConfig implements IScriptRunner{
	BlockPosSupplierChooser blockPos = addConfig(new BlockPosSupplierChooser(parent, "blockPos", null));
	Direction6SupplierChooser direction = addConfig(new Direction6SupplierChooser(parent, "direction", null));
	public AttackBlock(ILPCConfigReadable parent) {
		super(parent, nameKey, null);
		setValueChangeCallback(this::notifyScriptChanged);
	}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		res.add(1, (button, mouseButton) -> blockPos.openChoose(), ()->fullKey + ".blockPos", buttonGenericAllocator);
		res.add(1, (button, mouseButton) -> direction.openChoose(), ()->fullKey + ".direction", buttonGenericAllocator);
	}
	@Override public @NotNull Consumer<CompiledVariableList> compile(VariableMap variableMap) throws CompileFailedException {
		BiConsumer<CompiledVariableList, BlockPos.Mutable> blockPos = this.blockPos.get().compileToBlockPos(variableMap);
		Function<CompiledVariableList, Direction> side = this.direction.get().compile(variableMap);
		BlockPos.Mutable blockPosBuf = new BlockPos.Mutable();
		return list->{
			ClientPlayerInteractionManager itm = MinecraftClient.getInstance().interactionManager;
			ClientPlayerEntity player = MinecraftClient.getInstance().player;
			if(itm != null && player != null){
				blockPos.accept(list, blockPosBuf);
				itm.attackBlock(blockPosBuf.toImmutable(), side.apply(list));
			}
		};
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "attackBlock";
	public static final String fullKey = IScriptRunner.fullPrefix + nameKey;
	//public static final String interactPosButtonKey = fullKey + ".interactPos";
}
