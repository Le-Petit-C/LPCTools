package lpctools.script.suppliers.Type;

import lpctools.script.suppliers.IScriptSupplier;
import lpctools.script.suppliers.ScriptType;

public interface ITypeSupplier extends IScriptSupplier<ScriptType> {
	//Boolean是final的，所以不需要? extends Boolean
	@Override default Class<ScriptType> getSuppliedClass(){return ScriptType.class;}
}
