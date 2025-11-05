package lpctools.script.suppliers.ControlFlow;

import lpctools.script.suppliers.IScriptSupplier;

public interface IControlFlowSupplier extends IScriptSupplier<ControlFlowIssue> {
	@Override default Class<? extends ControlFlowIssue> getSuppliedClass(){return ControlFlowIssue.class;}
}
