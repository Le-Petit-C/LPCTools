package lpctools.script.suppliers.randoms;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import lpctools.script.*;
import lpctools.script.editScreen.ScriptDisplayWidget;
import lpctools.script.editScreen.ScriptFitTextField;
import lpctools.script.runtimeInterfaces.ScriptFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static lpctools.lpcfymasaapi.LPCConfigUtils.warnFailedLoadingConfig;

public class FromVariable<T> extends AbstractScript implements IRandomSupplier<T> {
	
	private @NotNull String variableName = "var";
	private @Nullable Iterable<?> widgets = null;
	public final Class<T> targetClass;
	
	public FromVariable(IScriptWithSubScript parent, Class<T> targetClass) {
		super(parent);
		this.targetClass = targetClass;
	}
	
	@Override public @Nullable Iterable<?> getWidgets() {
		if(widgets == null){
			widgets = List.of(new ScriptFitTextField(
				getDisplayWidget(), 100, text->{
				variableName = text;
				applyToDisplayWidgetIfNotNull(ScriptDisplayWidget::markUpdateChain);
			}));
		}
		return widgets;
	}
	
	@Override public @NotNull ScriptFunction<CompileEnvironment.RuntimeVariableMap, Object>
	compileRandom(CompileEnvironment variableMap) {
		var variableRef = variableMap.getVariableReference(variableName);
		return variableRef::getValue;
	}
	
	@Override public @Nullable JsonElement getAsJsonElement() {
		return new JsonPrimitive(variableName);
	}
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {
		if(element == null) return;
		if (!(element instanceof JsonPrimitive primitive)) {
			warnFailedLoadingConfig("FromVariable", element);
			return;
		}
		variableName = primitive.getAsString();
	}
	
	@Override public Class<? extends T> getSuppliedClass() {return targetClass;}
}
