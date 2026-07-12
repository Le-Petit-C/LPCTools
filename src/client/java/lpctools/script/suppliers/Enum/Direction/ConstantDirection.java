package lpctools.script.suppliers.Enum.Direction;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import lpctools.script.AbstractScript;
import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static lpctools.lpcfymasaapi.LPCConfigUtils.warnFailedLoadingConfig;

public class ConstantDirection extends AbstractScript implements IDirectionSupplier {
	Direction value = Direction.UP;
	private @Nullable ButtonGeneric switchButton = null;
	public ConstantDirection(IScriptWithSubScript parent) {super(parent);}
	@Override public @NotNull ScriptNotNullSupplier<Direction>
	compileNotNull(CompileEnvironment environment) {
		final var cachedValue = value;
		return map->cachedValue;
	}
	
	@Override public @Nullable JsonElement getAsJsonElement() {return new JsonPrimitive(value.getName());}
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {
		if(element == null) return;
		if(!(element instanceof JsonPrimitive primitive) ||
			!(Direction.byName(primitive.getAsString()) instanceof Direction direction)){
			warnFailedLoadingConfig("ConstantInteger", element);
			return;
		}
		value = direction;
	}
	@Override public @Nullable Iterable<?> getWidgets() {return List.of(getSwitchButton());}
	
	private @NotNull ButtonGeneric getSwitchButton(){
		if(switchButton == null){
			switchButton = new ButtonGeneric(0, 0, 100, 20, value.getName());
			switchButton.setActionListener((button, mouseButton)->{
				value = Direction.from3DDataValue(value.get3DDataValue() + 1);
				switchButton.setDisplayString(value.getName());
			});
		}
		return switchButton;
	}
}
