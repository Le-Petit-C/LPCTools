package lpctools.scripts.suppliers.supplierChooser;

import lpctools.scripts.suppliers.interfaces.IScriptSupplier;

import java.util.Map;
import java.util.function.Supplier;

public abstract class ChooserBase<T> {
	public abstract Map<String, Supplier<IScriptSupplier<T>>> getSuppliers();
}
