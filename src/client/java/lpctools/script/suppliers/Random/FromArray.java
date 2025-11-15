package lpctools.script.suppliers.Random;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptNullableSupplier;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class FromArray<T> extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IRandomSupplier<T> {
	protected final SupplierStorage<Object[]> array = ofStorage(Object[].class,
		Text.translatable("lpctools.script.suppliers.random.fromArray.subSuppliers.array.name"), "array");
	protected final SupplierStorage<Integer> index = ofStorage(Integer.class,
		Text.translatable("lpctools.script.suppliers.random.fromArray.subSuppliers.index.name"), "index");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(array, index);
	
	public final Class<T> targetClass;
	
	public FromArray(IScriptWithSubScript parent, Class<T> targetClass) {
		super(parent);
		this.targetClass = targetClass;
	}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptNullableSupplier<Object>
	compileRandom(CompileEnvironment environment) {
		var compiledArraySupplier = array.get().compileCheckedNotNull(environment);
		var compiledIndexSupplier = compileCheckedInteger(index.get(), environment);
		return map->{
			Object[] array = compiledArraySupplier.scriptApply(map);
			int index = compiledIndexSupplier.scriptApplyAsInt(map);
			if(index < 0 || index >= array.length)
				throw ScriptRuntimeException.indexOutOfBounds(this, index, array.length);
			return array[index];
		};
	}
	@Override public Class<? extends T> getSuppliedClass() {return targetClass;}
}
