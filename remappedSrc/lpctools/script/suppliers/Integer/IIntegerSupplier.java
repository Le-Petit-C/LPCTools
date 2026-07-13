package lpctools.script.suppliers.Integer;

import lpctools.script.suppliers.IScriptSupplierInteger;

public interface IIntegerSupplier extends IScriptSupplierInteger {
	@Override default Class<? extends Integer> getSuppliedClass(){return Integer.class;}
}
