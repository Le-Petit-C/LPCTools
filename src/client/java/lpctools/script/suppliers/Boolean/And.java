package lpctools.script.suppliers.Boolean;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptBooleanSupplier;
import lpctools.script.suppliers.AbstractSupplierWithSubScriptMutable;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static lpctools.lpcfymasaapi.LPCConfigUtils.warnFailedLoadingConfig;

public class And extends AbstractSupplierWithSubScriptMutable<Boolean, Boolean> implements IBooleanSupplier{
	private @Nullable Iterable<?> widgets;
	public final @Nullable Text name;
	
	public And(IScriptWithSubScript parent, @Nullable Text name) {
		super(parent);
		this.name = name;
	}
	public And(IScriptWithSubScript parent) {this(parent, null);}
	
	@Override public Class<Boolean> getArgumentClass() {return Boolean.class;}
	@Override public @NotNull ScriptBooleanSupplier
	compileBoolean(CompileEnvironment environment) {
		ArrayList<ScriptBooleanSupplier> compiledSubRunners = new ArrayList<>();
		for(var sub : getSubScripts()) compiledSubRunners.add(compileCheckedBoolean(sub, environment));
		return map->{
			for(var runnable : compiledSubRunners)
				if(!runnable.scriptApplyAsBoolean(map)) return false;
			return true;
		};
	}
	@Override public @Nullable JsonElement getAsJsonElement() {return getSubSuppliersAsJsonArray();}
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {
		if(element == null) return;
		if(element instanceof JsonArray array) loadSubSuppliersFromJsonArray(array);
		else warnFailedLoadingConfig(getName(), element);
	}
	
	@Override public @Nullable Iterable<?> getWidgets() {
		if(widgets == null) widgets = List.of(createAddButton());
		return widgets;
	}
	@Override @Nullable public Text getName() {
		if(name != null) return name;
		else return super.getName();
	}
}
