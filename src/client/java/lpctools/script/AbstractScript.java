package lpctools.script;

import lpctools.script.editScreen.ScriptDisplayWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractScript implements IScript {
	protected @Nullable ScriptDisplayWidget displayWidget;
	@Override public @NotNull ScriptDisplayWidget getDisplayWidget() {
		if(displayWidget == null) displayWidget = new ScriptDisplayWidget(this);
		return displayWidget;
	}
}
