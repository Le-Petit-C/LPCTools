package lpctools.scripts.runners;

import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.choosers.BlockPosSupplierChooser;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class InteractBlock extends BlockPosSupplierChooser implements IScriptRunner{
	public InteractBlock(ILPCConfigReadable parent) {
		super(parent, nameKey, null);
		setValueChangeCallback(this::notifyScriptChanged);
	}
	@Override public @NotNull Consumer<CompiledVariableList> compile(VariableMap variableMap) throws CompileFailedException {
		BiConsumer<CompiledVariableList, BlockPos.Mutable> func = get().compileToBlockPos(variableMap);
		BlockPos.Mutable mutable = new BlockPos.Mutable();
		return list->{
			ClientPlayerInteractionManager itm = MinecraftClient.getInstance().interactionManager;
			ClientPlayerEntity player = MinecraftClient.getInstance().player;
			if(itm != null && player != null){
				func.accept(list, mutable);
				BlockPos pos = mutable.toImmutable();
				BlockHitResult hitResult = new BlockHitResult(pos.toCenterPos(), Direction.UP, pos, false);
				itm.interactBlock(player, Hand.MAIN_HAND, hitResult);
			}
		};
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "interactBlock";
	public static final String fullKey = IScriptRunner.fullPrefix + nameKey;
	//public static final String interactPosButtonKey = fullKey + ".interactPos";
}
