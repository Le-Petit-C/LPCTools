package lpctools.scripts.suppliers.blockPos;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.ButtonConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

public class PlayerBlockPos extends ButtonConfig implements IScriptBlockPosSupplier {
	public PlayerBlockPos(ILPCConfigReadable parent) {super(parent, nameKey, null);}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "playerBlockPos";
	public static final String fullKey = fullPrefix + nameKey;
	
	@Override public @NotNull BiConsumer<CompiledVariableList, BlockPos.Mutable> compileToBlockPos(VariableMap variableMap) {
		return (list, pos)->{
			ClientPlayerEntity player = MinecraftClient.getInstance().player;
			if(player == null) pos.set(BlockPos.ORIGIN);
			else pos.set(player.getBlockPos());
		};
	}
}
