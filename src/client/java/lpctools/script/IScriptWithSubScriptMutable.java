package lpctools.script;

import lpctools.script.editScreen.ScriptWithSubScriptDisplayWidget;
import lpctools.script.editScreen.ScriptWithSubScriptMutableDisplayWidget;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.function.Consumer;

public interface IScriptWithSubScriptMutable<T extends IScript> extends IScriptWithSubScript{
	//提示你某处的insert按钮被按下了，需要选择一个内容插入
	void notifyInsertion(Consumer<T> callback);
	@Override @NotNull ArrayList<T> getSubScripts();
	@Override @NotNull ScriptWithSubScriptMutableDisplayWidget<T> getDisplayWidget();
	void applyToDisplayWidgetWithSubScriptMutableIfNotNull(Consumer<? super ScriptWithSubScriptMutableDisplayWidget<T>> consumer);
	@Override default void applyToDisplayWidgetWithSubScriptIfNotNull(Consumer<? super ScriptWithSubScriptDisplayWidget> consumer){
		applyToDisplayWidgetWithSubScriptMutableIfNotNull(consumer);
	}
}
