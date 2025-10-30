package lpctools.script.suppliers;

import lpctools.script.IScriptWithSubScript;

public interface IScriptSupplierAllocator<T> {
	T allocate(IScriptWithSubScript parent);
}
