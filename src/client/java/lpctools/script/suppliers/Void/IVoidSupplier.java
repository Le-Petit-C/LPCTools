package lpctools.script.suppliers.Void;

import lpctools.script.suppliers.IScriptSupplier;

public interface IVoidSupplier extends IScriptSupplier<Void> {
	@Override default Class<? extends Void> getSuppliedClass(){return Void.class;}
}
