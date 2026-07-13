package lpctools.script.suppliers.Random;

import lpctools.script.IScriptWithSubScript;

public interface IRandomSupplierAllocator {
	<T> IRandomSupplier<T> allocate(IScriptWithSubScript parent, Class<T> targetClass);
}
