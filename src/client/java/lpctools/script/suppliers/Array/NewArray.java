package lpctools.script.suppliers.Array;

import com.google.gson.JsonElement;
import lpctools.script.CompileEnvironment;
import lpctools.script.IScript;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptFunction;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import lpctools.script.suppliers.Random.Null;
import lpctools.script.suppliers.ScriptSupplierLake;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NewArray extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IArraySupplier {
	
	protected final SupplierStorage<Integer> size = ofStorage(new Null<>(this, Integer.class),
		Text.translatable("lpctools.script.suppliers.Array.newArray.subSuppliers.size.name"));
	protected final SubSupplierEntry<?>[] subSuppliers = subSupplierBuilder()
		.addEntry(Integer.class, size)
		.build();
	
	public NewArray(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SubSupplierEntry<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptFunction<CompileEnvironment.RuntimeVariableMap, Object[]>
	compile(CompileEnvironment variableMap) {
		var compiledEntitySupplier = size.get().compile(variableMap);
		return map->{
			Integer size = compiledEntitySupplier.scriptApply(map);
			if(size == null) throw ScriptRuntimeException.nullPointer(this);
			return new Object[size];
		};
	}
	
	@Override public @Nullable JsonElement getAsJsonElement() {
		return ScriptSupplierLake.getJsonEntryFromSupplier(size.get());
	}
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {
		ScriptSupplierLake.loadSupplierOrWarn(element, Integer.class, this, res -> size.set(res), "NewArray.size");
	}
	
	@Override public @NotNull List<? extends IScript> getSubScripts() {
		return List.of(size.get());
	}
}
