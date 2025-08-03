package lpctools.scripts.runners.setVariable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lpctools.lpcfymasaapi.configButtons.UpdateTodo;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.ChooseConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.StringThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.runners.IScriptRunner;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import lpctools.scripts.runners.variables.VariableTestPack;
import lpctools.scripts.suppliers.IScriptSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public abstract class SetVariable<T extends IScriptSupplier<?>> extends StringThirdListConfig implements IScriptRunner {
	public final ChooseConfig<T> supplier;
	public SetVariable(@NotNull ILPCConfigReadable parent, String nameKey, ChooseConfig<T> supplier) {
		super(parent, nameKey, null, null);
		setValueChangeCallback(this::notifyScriptChanged);
		this.supplier = supplier;
		supplier.setValueChangeCallback(this::onValueChanged);
		addConfig(supplier.get());
	}
	@Override public @NotNull Consumer<CompiledVariableList> compile(VariableMap variableMap) throws CompileFailedException{
		int index = variableMap.get(getStringValue(), testPack());
		return setValue(variableMap, supplier.get(), index);
	}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		res.add(-1, (button, mouseButton)->supplier.openChoose(), ()->"R", buttonGenericAllocator);
	}
	@Override public void setAlignedIndent(int indent) {supplier.setAlignedIndent(indent);}
	@Override public int getAlignedIndent() {return supplier.getAlignedIndent();}
	@Override public @Nullable JsonObject getAsJsonElement() {
		JsonObject object = new JsonObject();
		object.add(propertiesId, supplier.getAsJsonElement());
		object.addProperty("string", getStringValue());
		object.addProperty(expandedKey, expanded);
		return object;
	}
	@Override public UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement data) {
		if(data instanceof JsonObject object){
			UpdateTodo updateTodo = new UpdateTodo();
			if(object.get("string") instanceof JsonPrimitive primitive){
				String last = stringValue;
				stringValue = primitive.getAsString();
				updateTodo.valueChanged(!last.equals(stringValue));
			}
			if(object.get(expandedKey) instanceof JsonPrimitive primitive){
				boolean b = expanded;
				expanded = primitive.getAsBoolean();
				getPage().markNeedUpdate(b != expanded);
			}
			updateTodo.combine(supplier.setValueFromJsonElementEx(object.get(propertiesId)));
			return updateTodo;
		}
		return setValueFailed(data);
	}
	@Override public void onValueChanged() {
		getConfigs().clear();
		addConfig(supplier.get());
		super.onValueChanged();
	}
	protected abstract VariableTestPack testPack();
	protected abstract @NotNull Consumer<CompiledVariableList>
	setValue(VariableMap variableMap, T src, int index) throws CompileFailedException;
	public static final String nameKey = "setVariable";
	public static final String fullKey = IScriptRunner.fullPrefix + nameKey;
	public static final String fullPrefix = fullKey + '.';
}
