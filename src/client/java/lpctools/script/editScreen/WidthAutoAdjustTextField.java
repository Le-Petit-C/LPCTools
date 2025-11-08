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
	public WidthAutoAdjustTextField(ScriptDisplayWidget parent, int minWidth, String text, @Nullable Consumer<String> callback) {
		this(parent, minWidth, callback);
		setText(text);
	}
	private @Nullable String lastText;
	@Override public void setFocused(boolean focused) {
		super.setFocused(focused);
		if(callback != null && !getText().equals(lastText))
			callback.accept(getText());
		lastText = getText();
	}
	@Override public void write(String text) {
		super.write(text);
		recalculateWidth(getText());
	}
	
	@Override public void eraseCharactersTo(int position) {
		super.eraseCharactersTo(position);
		recalculateWidth(getText());
	}
	
	@Override public void setText(String text) {
		recalculateWidth(text);
		super.setText(text);
		recalculateWidth(getText());
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
	public void setCallback(@Nullable Consumer<String> callback){this.callback = callback;}
	
	private void recalculateWidth(String text){
		var textRenderer = parent.editScreen.textRenderer;
		int newWidth = Math.max(minWidth, calculateTextButtonWidth(text, textRenderer, getHeight()) + textRenderer.fontHeight);
		if(newWidth != getWidth()){
			setWidth(newWidth);
			parent.markUpdateChain();
		}
	}
}
