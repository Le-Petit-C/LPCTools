package lpctools.script.suppliers.ControlFlowIssue;

import lpctools.script.suppliers.IScriptSupplierNotNull;

public interface IControlFlowIssueSupplier extends IScriptSupplierNotNull<ControlFlowIssue> {
	@Override default Class<? extends ControlFlowIssue> getSuppliedClass(){return ControlFlowIssue.class;}
}
