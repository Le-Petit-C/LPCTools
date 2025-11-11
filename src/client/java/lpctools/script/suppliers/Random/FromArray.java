package lpctools.script.suppliers.Random;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptNullableFunction;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import lpctools.script.suppliers.Integer.ConstantInteger;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class FromArray<T> extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IRandomSupplier<T> {
	protected final SupplierStorage<Object[]> array = ofStorage(Object[].class, new Null<>(this, Object[].class),
		Text.translatable("lpctools.script.suppliers.Random.fromArray.subSuppliers.array.name"), "array");
	protected final SupplierStorage<Integer> index = ofStorage(Integer.class, new ConstantInteger(this),
		Text.translatable("lpctools.script.suppliers.Random.fromArray.subSuppliers.index.name"), "index");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(array, index);
	
	public final Class<T> targetClass;
	
	public FromArray(IScriptWithSubScript parent, Class<T> targetClass) {
		super(parent);
		this.targetClass = targetClass;
	}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptNullableFunction<CompileEnvironment.RuntimeVariableMap, Object>
	compileRandom(CompileEnvironment variableMap) {
		var compiledArraySupplier = array.get().compile(variableMap);
		var compiledIndexSupplier = index.get().compile(variableMap);
		return map->{
			Object[] array = compiledArraySupplier.scriptApply(map);
			if(array == null) throw ScriptRuntimeException.nullPointer(this);
			Integer index = compiledIndexSupplier.scriptApply(map);
			if(index == null) throw ScriptRuntimeException.nullPointer(this);
			if(index < 0 || index >= array.length)
				throw ScriptRuntimeException.indexOutOfBounds(this, index, array.length);
			return array[index];
		};
	}
	@Override public Class<? extends T> getSuppliedClass() {return targetClass;}
}
