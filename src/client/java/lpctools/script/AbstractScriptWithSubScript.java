package lpctools.script;

import lpctools.script.editScreen.ScriptWithSubScriptDisplayWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractScriptWithSubScript implements IScriptWithSubScript {
	protected @Nullable ScriptWithSubScriptDisplayWidget displayWidget;
	public final IScriptWithSubScript parent;
	public AbstractScriptWithSubScript(IScriptWithSubScript parent){
		this.parent = parent;
	}
	
	@Override @Nullable public IScriptWithSubScript getParent() {
		return parent;
	}
	@Override public @NotNull ScriptWithSubScriptDisplayWidget getDisplayWidget() {
		if(displayWidget == null) displayWidget = new ScriptWithSubScriptDisplayWidget(this);
		return displayWidget;
	}
}
