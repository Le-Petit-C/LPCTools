package lpctools.script.suppliers.Direction;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import lpctools.script.AbstractScript;
import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptNullableFunction;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static lpctools.lpcfymasaapi.LPCConfigUtils.warnFailedLoadingConfig;

public class ConstantDirection extends AbstractScript implements IDirectionSupplier {
	Direction value = Direction.UP;
	private @Nullable ButtonGeneric switchButton = null;
	public ConstantDirection(IScriptWithSubScript parent) {super(parent);}
	@Override public @NotNull ScriptNullableFunction<CompileEnvironment.RuntimeVariableMap, Direction>
	compile(CompileEnvironment variableMap) {
		final var cachedValue = value;
		return map->cachedValue;
	}
	
	@Override public @Nullable JsonElement getAsJsonElement() {return new JsonPrimitive(value.getId());}
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {
		if(element == null) return;
		if(!(element instanceof JsonPrimitive primitive) ||
			!(Direction.byId(primitive.getAsString()) instanceof Direction direction)){
			warnFailedLoadingConfig("ConstantInteger", element);
			return;
		}
		value = direction;
	}
	@Override public @Nullable Iterable<?> getWidgets() {return List.of(getSwitchButton());}
	
	private @NotNull ButtonGeneric getSwitchButton(){
		if(switchButton == null){
			switchButton = new ButtonGeneric(0, 0, 100, 20, value.getId());
			switchButton.setActionListener((button, mouseButton)->{
				value = Direction.byIndex(value.getIndex() + 1);
				switchButton.setDisplayString(value.getId());
			});
		}
		return switchButton;
	}
}
