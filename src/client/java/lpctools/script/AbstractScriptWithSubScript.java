package lpctools.script;

import lpctools.script.editScreen.ScriptWithSubScriptDisplayWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractScriptWithSubScript implements IScriptWithSubScript {
	protected @Nullable ScriptWithSubScriptDisplayWidget displayWidget;
	@Override public @NotNull ScriptWithSubScriptDisplayWidget getDisplayWidget() {
		if(displayWidget == null) displayWidget = new ScriptWithSubScriptDisplayWidget(this);
		return displayWidget;
	}
}
