package lpctools.scripts.runners;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.ChooseConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.ScriptConfigData;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import lpctools.scripts.suppliers.interfaces.IScriptSupplier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;

public class InteractBlock extends ChooseConfig<IScriptSupplier<BlockPos>> implements IScriptRunner{
	public InteractBlock(ILPCConfigReadable parent) {
		super(parent, nameKey, ScriptConfigData.blockPosSupplierConfigs, null);
		setValueChangeCallback(()->getScript().onValueChanged());
	}
	@Override public Consumer<CompiledVariableList> compile(VariableMap variableMap) throws CompileFailedException {
		Function<CompiledVariableList, BlockPos> func = get().compile(variableMap);
		return list->{
			ClientPlayerInteractionManager itm = MinecraftClient.getInstance().interactionManager;
			ClientPlayerEntity player = MinecraftClient.getInstance().player;
			if(itm != null && player != null){
				BlockPos pos = func.apply(list).toImmutable();
				BlockHitResult hitResult = new BlockHitResult(pos.toCenterPos(), Direction.UP, pos, false);
				itm.interactBlock(player, Hand.MAIN_HAND, hitResult);
			}
		};
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "interactBlock";
	public static final String fullKey = fullPrefix + nameKey;
	//public static final String interactPosButtonKey = fullKey + ".interactPos";
}
