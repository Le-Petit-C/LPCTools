package lpctools.script.suppliers.ControlFlow;

public enum ControlFlowIssue {
	NO_ISSUE(false, false){
		@Override public ControlFlowIssue applied() {return NO_ISSUE;}
	},
	BREAK(true, true){
		@Override public ControlFlowIssue applied(){return NO_ISSUE;}
	},
	CONTINUE(false, true){
		@Override public ControlFlowIssue applied(){return NO_ISSUE;}
	},
	RETURN(true, true){
		@Override public ControlFlowIssue applied(){return RETURN;}
	};
	public final boolean shouldBreak, shouldEndRunMultiple;
	ControlFlowIssue(boolean shouldBreak, boolean shouldEndRunMultiple){
		this.shouldBreak = shouldBreak;
		this.shouldEndRunMultiple = shouldEndRunMultiple;
	}
	public abstract ControlFlowIssue applied();
}
