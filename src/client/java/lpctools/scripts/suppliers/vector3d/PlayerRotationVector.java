package lpctools.scripts.suppliers.vector3d;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.ButtonConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

import java.util.function.BiConsumer;

public class PlayerRotationVector extends ButtonConfig implements IScriptVector3dSupplier {
	public PlayerRotationVector(ILPCConfigReadable parent) {super(parent, nameKey, null);}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "playerRotationVector";
	public static final String fullKey = fullPrefix + nameKey;
	@Override public @NotNull BiConsumer<CompiledVariableList, Vector3d> compileToVector3d(VariableMap variableMap) {
		return (list, pos)->{
			ClientPlayerEntity player = MinecraftClient.getInstance().player;
			if(player == null) pos.set(0);
			else {
				Vec3d vec = player.getRotationVector();
				pos.set(vec.x, vec.y, vec.z);
			}
		};
	}
}
