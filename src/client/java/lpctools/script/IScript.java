package lpctools.script;

import com.google.gson.JsonElement;
import lpctools.script.editScreen.ScriptDisplayWidget;
import lpctools.script.exceptions.ScriptException;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

public interface IScript {
	@Nullable Text getName();
	@Nullable Text getComment();
	//获取Buttons, Widgets和TextFields
	default @Nullable Iterable<?> getWidgets(){return null;}
	@Nullable JsonElement getAsJsonElement();
	void setValueFromJsonElement(@Nullable JsonElement element);
	@Nullable IScriptWithSubScript getParent();
	default @NotNull Script getScript(){return Objects.requireNonNull(getParent()).getScript();}
	@NotNull ScriptDisplayWidget getDisplayWidget();
	void applyToDisplayWidgetIfNotNull(Consumer<? super ScriptDisplayWidget> consumer);
	
	default <T extends ScriptException> T putException(T exception){
		getScript().putException(this, exception);
		return exception;
	}
}
