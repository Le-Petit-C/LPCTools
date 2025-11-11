package lpctools.script.suppliers.Boolean;

import com.google.gson.JsonElement;
import fi.dy.masa.malilib.config.IConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.gui.button.ConfigButtonBoolean;
import lpctools.script.AbstractScript;
import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptNullableFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ConstantBoolean extends AbstractScript implements IBooleanSupplier {
	public final IConfigBoolean value = new ConfigBoolean("ConstantBoolean.value", false);
	private @Nullable ConfigButtonBoolean button = null;
	public ConstantBoolean(IScriptWithSubScript parent) {super(parent);}
	@Override public @NotNull ScriptNullableFunction<CompileEnvironment.RuntimeVariableMap, Boolean>
	compile(CompileEnvironment variableMap) {
		boolean cachedValue = value.getBooleanValue();
		return map->cachedValue;
	}
	
	@Override public @Nullable JsonElement getAsJsonElement() {return value.getAsJsonElement();}
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {
		if(element == null) return;
		value.setValueFromJsonElement(element);
	}
	@Override public @Nullable Iterable<?> getWidgets() {return List.of(getButton());}
	
	private @NotNull ConfigButtonBoolean getButton(){
		if(button == null) button = new ConfigButtonBoolean(0, 0, 100, 20, value);
		return button;
	}
}
