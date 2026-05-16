package lpctools.script.editScreen;

import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;
import lpctools.mixin.client.accessors.TextFieldWidgetAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonEvent;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

import static lpctools.lpcfymasaapi.LPCConfigUtils.calculateTextButtonWidth;

public class WidthAutoAdjustTextField extends GuiTextFieldGeneric{
	public final int minWidth;
	public @Nullable Consumer<String> callback;
	public final ScriptDisplayWidget parent;
	public WidthAutoAdjustTextField(ScriptDisplayWidget parent, int minWidth, @Nullable Consumer<String> callback) {
		super(0, 0, minWidth, 16, Minecraft.getInstance().font);
		this.parent = parent;
		this.minWidth = minWidth;
		this.callback = callback;
	}
	public WidthAutoAdjustTextField(ScriptDisplayWidget parent, int minWidth, String text, @Nullable Consumer<String> callback) {
		this(parent, minWidth, callback);
		setValue(text);
	}
	private @Nullable String lastText;
	@Override public void setFocused(boolean focused) {
		super.setFocused(focused);
		if(callback != null && !getValue().equals(lastText))
			callback.accept(getValue());
		lastText = getValue();
	}
	@Override public void insertText(@NonNull String text) {
		super.insertText(text);
		recalculateWidth(getValue());
	}
	
	@Override public void deleteCharsToPos(int position) {
		super.deleteCharsToPos(position);
		recalculateWidth(getValue());
	}
	
	@Override public void setValue(@NonNull String text) {
		recalculateWidth(text);
		super.setValue(text);
		recalculateWidth(getValue());
	}
	
	@Override public boolean mouseClicked(MouseButtonEvent click, boolean doubleClick) {
		if(click.button() == 0 && isMouseOver(click.x(), click.y()))
			parent.editScreen.setScriptFocused(this);
		return super.mouseClicked(click, doubleClick);
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
	@Override public void setSize(int width, int height) {
		super.setSize(width, height);
		((TextFieldWidgetAccessor)this).invokeUpdateTextPosition();
	}
	public void setCallback(@Nullable Consumer<String> callback){this.callback = callback;}
	
	private void recalculateWidth(String text){
		var textRenderer = parent.editScreen.getFont();
		int newWidth = Math.max(minWidth, calculateTextButtonWidth(text, textRenderer, getHeight()) + textRenderer.lineHeight);
		if(newWidth != getWidth()){
			setWidth(newWidth);
			parent.markUpdateChain();
		}
	}
}
