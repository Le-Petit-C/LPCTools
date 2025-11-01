package lpctools.script.suppliers.Double;

import lpctools.script.suppliers.IScriptSupplier;

public interface IDoubleSupplier extends IScriptSupplier<Double> {
	@Override default Class<? extends Double> getSuppliedClass(){return Double.class;}
}
