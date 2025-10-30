package lpctools.script;

import lpctools.script.editScreen.ScriptWithSubScriptDisplayWidget;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IScriptWithSubScript extends IScript{
	@NotNull List<? extends IScript> getSubScripts();
	@NotNull ScriptWithSubScriptDisplayWidget getDisplayWidget();
}
