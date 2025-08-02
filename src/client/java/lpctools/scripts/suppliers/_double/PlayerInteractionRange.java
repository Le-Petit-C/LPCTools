package lpctools.scripts.suppliers._double;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.ButtonConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.function.ToDoubleFunction;

public class PlayerInteractionRange extends ButtonConfig implements IScriptDoubleSupplier {
	public PlayerInteractionRange(ILPCConfigReadable parent) {super(parent, nameKey, null);}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "playerInteractionRange";
	public static final String fullKey = fullPrefix + nameKey;
	@Override public @NotNull ToDoubleFunction<CompiledVariableList>
	compileToDouble(VariableMap variableMap) {
		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		if(player != null) lastStoredReachDistance = player.getBlockInteractionRange();
		return list->lastStoredReachDistance;
	}
	private static double lastStoredReachDistance = 4.5;
}
