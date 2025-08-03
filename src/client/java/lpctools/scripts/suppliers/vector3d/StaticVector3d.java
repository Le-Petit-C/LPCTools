package lpctools.scripts.suppliers.vector3d;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.Vector3dConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

import java.util.function.BiConsumer;

public class StaticVector3d extends Vector3dConfig implements IScriptVector3dSupplier {
	public StaticVector3d(ILPCConfigReadable parent) {
		super(parent, nameKey, Vec3d.ZERO, null);
		setValueChangeCallback(this::notifyScriptChanged);
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "static";
	public static final String fullKey = fullPrefix + nameKey;
	
	@Override public @NotNull BiConsumer<CompiledVariableList, Vector3d>
	compileToVector3d(VariableMap variableMap) {
		Vector3d vec = getPos(new Vector3d());
		return (list, res)->res.set(vec);
	}
}
