package lpctools.script.suppliers.Iterable;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptFunction;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import lpctools.script.suppliers.Random.Null;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

//从近到远遍历，距离是到方块坐标所表示的方块中心的距离，也就是方块坐标xyz都加了0.5
public class IterableFromArray extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IIterableSupplier {
	protected final SupplierStorage<Object[]> array = ofStorage(Object[].class, new Null<>(this, Object[].class),
		Text.translatable("lpctools.script.suppliers.Iterable.iterableFromArray.subSuppliers.array.name"), "array");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(array);
	
	public IterableFromArray(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptFunction<CompileEnvironment.RuntimeVariableMap, ObjectIterable>
	compile(CompileEnvironment variableMap) {
		var compiledArraySupplier = array.get().compile(variableMap);
		return map->{
			var array = compiledArraySupplier.scriptApply(map);
			if(array == null) throw ScriptRuntimeException.nullPointer(this);
			return ObjectIterable.of(array);
		};
	}
}
