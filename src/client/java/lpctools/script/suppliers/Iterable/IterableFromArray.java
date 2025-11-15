package lpctools.script.suppliers.Iterable;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

//从近到远遍历，距离是到方块坐标所表示的方块中心的距离，也就是方块坐标xyz都加了0.5
public class IterableFromArray extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IIterableSupplier {
	protected final SupplierStorage<Object[]> array = ofStorage(Object[].class,
		Text.translatable("lpctools.script.suppliers.iterable.iterableFromArray.subSuppliers.array.name"), "array");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(array);
	
	public IterableFromArray(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptNotNullSupplier<ObjectIterable>
	compileNotNull(CompileEnvironment environment) {
		var compiledArraySupplier = array.get().compileCheckedNotNull(environment);
		return map->ObjectIterable.of(compiledArraySupplier.scriptApply(map));
	}
}
