package lpctools.script;

import lpctools.script.trigger.TriggerOption;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public interface ISubScriptMutable extends IScript{
	//这里可能并不需要AvlTreeList, ArrayList应该足够了
	@NotNull ArrayList<? extends TriggerOption> getSubScripts();
}
