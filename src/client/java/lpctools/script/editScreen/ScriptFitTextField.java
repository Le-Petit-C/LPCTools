package lpctools.script.editScreen;

import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;
import lpctools.script.IScript;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

import static lpctools.lpcfymasaapi.LPCConfigUtils.calculateTextButtonWidth;

public class ScriptFitTextField extends GuiTextFieldGeneric{
	public final int minWidth;
	public @Nullable Consumer<String> callback;
	public final IScript parent;
	public ScriptFitTextField(IScript parent,int minWidth, @Nullable Consumer<String> callback) {
		super(0, 0, minWidth, 16, MinecraftClient.getInstance().textRenderer);
		this.parent = parent;
		this.minWidth = minWidth;
		this.callback = callback;
	}
	private @Nullable String lastText;
	@Override public void setFocused(boolean focused) {
		super.setFocused(focused);
		if(callback != null && !getText().equals(lastText))
			callback.accept(getText());
		lastText = getText();
	}
	@Override public boolean charTyped(char chr, int modifiers) {
		boolean b = super.charTyped(chr, modifiers);
		var editScreen = parent.getScript().editScreen;
		setWidth(Math.max(minWidth, calculateTextButtonWidth(getText(), editScreen.textRenderer, getHeight())));
		editScreen.realignWidgetsByNameButton(parent);
		return b;
	}
}
