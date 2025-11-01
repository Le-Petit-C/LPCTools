package lpctools.script.editScreen;

import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;
import lpctools.mixin.client.TextFieldWidgetAccessor;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

import static lpctools.lpcfymasaapi.LPCConfigUtils.calculateTextButtonWidth;

public class WidthAutoAdjustTextField extends GuiTextFieldGeneric{
	public final int minWidth;
	public @Nullable Consumer<String> callback;
	public final ScriptDisplayWidget parent;
	public WidthAutoAdjustTextField(ScriptDisplayWidget parent, int minWidth, @Nullable Consumer<String> callback) {
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
		var editScreen = parent.editScreen;
		setWidth(Math.max(minWidth, calculateTextButtonWidth(getText(), editScreen.textRenderer, getHeight())));
		parent.update();
		return b;
	}
	
	@Override public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		if(mouseButton == 0 && isMouseOver(mouseX, mouseY))
			parent.editScreen.setScriptFocused(this);
		return super.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	@Override public void setPosition(int x, int y) {
		super.setPosition(x, y);
		((TextFieldWidgetAccessor)this).invokeUpdateTextPosition();
	}
	@Override public void setWidth(int width) {
		super.setWidth(width);
		((TextFieldWidgetAccessor)this).invokeUpdateTextPosition();
	}
	@Override public void setHeight(int height) {
		super.setHeight(height);
		((TextFieldWidgetAccessor)this).invokeUpdateTextPosition();
	}
	@Override public void setDimensions(int width, int height) {
		super.setDimensions(width, height);
		((TextFieldWidgetAccessor)this).invokeUpdateTextPosition();
	}
}
