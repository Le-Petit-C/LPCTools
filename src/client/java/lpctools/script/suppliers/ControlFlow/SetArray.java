package lpctools.script.suppliers.ControlFlow;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lpctools.script.CompileEnvironment;
import lpctools.script.IScript;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptFunction;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import lpctools.script.suppliers.Integer.ConstantInteger;
import lpctools.script.suppliers.Random.Null;
import lpctools.script.suppliers.ScriptSupplierLake;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static lpctools.lpcfymasaapi.LPCConfigUtils.warnFailedLoadingConfig;

public class SetArray extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IControlFlowSupplier {
	protected final SupplierStorage<Object[]> array = ofStorage(new Null<>(this, Object[].class),
		Text.translatable("lpctools.script.suppliers.ControlFlowIssue.setArray.subSuppliers.array.name"));
	protected final SupplierStorage<Integer> index = ofStorage(new ConstantInteger(this),
		Text.translatable("lpctools.script.suppliers.ControlFlowIssue.setArray.subSuppliers.index.name"));
	protected final SupplierStorage<Object> value = ofStorage(new Null<>(this, Object.class),
		Text.translatable("lpctools.script.suppliers.ControlFlowIssue.setArray.subSuppliers.value.name"));
	protected final SubSupplierEntry<?>[] subSuppliers = subSupplierBuilder()
		.addEntry(Object[].class, array)
		.addEntry(Integer.class, index)
		.build();
	
	public static final String arrayJsonKey = "array";
	public static final String indexJsonKey = "index";
	
	public SetArray(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SubSupplierEntry<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptFunction<CompileEnvironment.RuntimeVariableMap, ControlFlowIssue>
	compile(CompileEnvironment variableMap) {
		var compiledArraySupplier = array.get().compile(variableMap);
		var compiledIndexSupplier = index.get().compile(variableMap);
		var compiledValueSupplier = value.get().compile(variableMap);
		return map->{
			Object value = compiledValueSupplier.scriptApply(map);
			Object[] array = compiledArraySupplier.scriptApply(map);
			if(array == null) throw ScriptRuntimeException.nullPointer(this);
			Integer index = compiledIndexSupplier.scriptApply(map);
			if(index == null) throw ScriptRuntimeException.nullPointer(this);
			if(index < 0 || index >= array.length)
				throw ScriptRuntimeException.indexOutOfBounds(this, index, array.length);
			array[index] = value;
			return ControlFlowIssue.NO_ISSUE;
		};
	}
	
	@Override public @Nullable JsonElement getAsJsonElement() {
		JsonObject res = new JsonObject();
		res.add(arrayJsonKey, ScriptSupplierLake.getJsonEntryFromSupplier(array.get()));
		res.add(indexJsonKey, ScriptSupplierLake.getJsonEntryFromSupplier(index.get()));
		return res;
	}
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {
		if(element == null) return;
		if (!(element instanceof JsonObject object)) {
			warnFailedLoadingConfig("FromArray", element);
			return;
		}
		ScriptSupplierLake.loadSupplierOrWarn(object.get(arrayJsonKey), Object[].class, this, res -> array.set(res), "FromArray.array");
		ScriptSupplierLake.loadSupplierOrWarn(object.get(indexJsonKey), Integer.class, this, res -> index.set(res), "FromArray.index");
	}
	
	@Override public @NotNull List<? extends IScript> getSubScripts() {return List.of(array.get());}
}
