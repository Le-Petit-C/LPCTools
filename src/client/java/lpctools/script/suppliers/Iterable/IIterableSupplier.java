package lpctools.script.suppliers.Iterable;

import lpctools.script.suppliers.IScriptSupplier;

public interface IIterableSupplier extends IScriptSupplier<ObjectIterable> {
	@Override default Class<? extends ObjectIterable> getSuppliedClass(){return ObjectIterable.class;}
}
