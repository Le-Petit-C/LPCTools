package lpctools.script.suppliers.Array;

import lpctools.script.suppliers.IScriptSupplierNotNull;

public interface IArraySupplier extends IScriptSupplierNotNull<Object[]> {
	@Override default Class<? extends Object[]> getSuppliedClass(){return Object[].class;}
}
