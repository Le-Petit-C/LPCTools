package lpctools.script;

import lpctools.script.editScreen.ScriptDisplayWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractScript implements IScript {
	protected @Nullable ScriptDisplayWidget displayWidget;
	public final IScriptWithSubScript parent;
	public AbstractScript(IScriptWithSubScript parent){
		this.parent = parent;
	}
	@Override @Nullable public IScriptWithSubScript getParent() {
		return parent;
	}
	@Override public @NotNull ScriptDisplayWidget getDisplayWidget() {
		if(displayWidget == null) displayWidget = new ScriptDisplayWidget(this);
		return displayWidget;
	}
}
