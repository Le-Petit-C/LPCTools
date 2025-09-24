package lpctools.script;

import com.google.gson.JsonElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IScript {
	String getName();
	//获取Buttons, Widgets和TextFields
	default @Nullable Iterable<?> getWidgets(){return null;}
	default @Nullable Iterable<? extends IScript> getSubScripts(){return null;}
	@Nullable JsonElement getAsJsonElement();
	void setValueFromJsonElement(@Nullable JsonElement element);
	@NotNull IScript getParent();
	default @NotNull Script getScript(){return getParent().getScript();}
}
