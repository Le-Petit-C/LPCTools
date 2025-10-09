package lpctools.script;

import com.google.gson.JsonElement;
import lpctools.script.editScreen.ScriptDisplayWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public interface IScript {
	String getName();
	//获取Buttons, Widgets和TextFields
	default @Nullable Iterable<?> getWidgets(){return null;}
	@Nullable JsonElement getAsJsonElement();
	void setValueFromJsonElement(@Nullable JsonElement element);
	@Nullable IScriptWithSubScript getParent();
	default @NotNull Script getScript(){return Objects.requireNonNull(getParent()).getScript();}
	@NotNull ScriptDisplayWidget getDisplayWidget();
}
