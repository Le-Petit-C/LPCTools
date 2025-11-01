package lpctools.script;

import lpctools.script.editScreen.ScriptDisplayWidget;
import lpctools.script.editScreen.ScriptWithSubScriptDisplayWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public interface IScriptWithSubScript extends IScript{
	@NotNull List<? extends IScript> getSubScripts();
	@NotNull ScriptWithSubScriptDisplayWidget getDisplayWidget();
	void applyToDisplayWidgetWithSubScriptIfNotNull(Consumer<? super ScriptWithSubScriptDisplayWidget> consumer);
	@Override default void applyToDisplayWidgetIfNotNull(Consumer<? super ScriptDisplayWidget> consumer){
		applyToDisplayWidgetWithSubScriptIfNotNull(consumer);
	}
	default @Nullable Text getSubScriptNamePrefix(IScript script){return null;}
}
