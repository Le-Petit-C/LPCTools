package lpctools.script.suppliers.Array;

import lpctools.script.suppliers.IScriptSupplier;

public interface IArraySupplier extends IScriptSupplier<Object[]> {
	@Override default Class<? extends Object[]> getSuppliedClass(){return Object[].class;}
}
