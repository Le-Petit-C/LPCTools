package lpctools.scripts.suppliers.blockPos;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.ButtonConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import lpctools.scripts.suppliers.interfaces.IScriptBlockPosSupplier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class PlayerBlockPos extends ButtonConfig implements IScriptBlockPosSupplier {
	public PlayerBlockPos(ILPCConfigReadable parent) {super(parent, nameKey, null);}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "playerBlockPos";
	public static final String fullKey = fullPrefix + nameKey;
	@Override public Function<CompiledVariableList, BlockPos> compile(VariableMap variableMap) {
		return list->{
			ClientPlayerEntity player = MinecraftClient.getInstance().player;
			if(player == null) return BlockPos.ORIGIN;
			else return player.getBlockPos();
		};
	}
}
