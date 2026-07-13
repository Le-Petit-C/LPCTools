package lpctools.script.suppliers.Double;

import lpctools.script.suppliers.IScriptSupplierDouble;

public interface IDoubleSupplier extends IScriptSupplierDouble {
	@Override default Class<? extends Double> getSuppliedClass(){return Double.class;}
}
