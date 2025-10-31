package lpctools.script.suppliers.randoms;

import lpctools.script.IScriptWithSubScript;

public interface IRandomSupplierAllocator {
	<T> IRandomSupplier<T> allocate(IScriptWithSubScript parent, Class<T> targetClass);
}
