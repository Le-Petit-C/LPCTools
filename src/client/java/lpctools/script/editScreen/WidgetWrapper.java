package lpctools.script.editScreen;

import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.widgets.WidgetBase;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WidgetWrapper extends HoveredClickableWidget{
	private static final ButtonGeneric defaultWidget = new ButtonGeneric(0, 0, 20, 20, "").setRenderDefaultBackground(false);
	private @NotNull WidgetBase widget = defaultWidget;
	public @NotNull ScriptEditScreen screen;
	public WidgetWrapper(@Nullable WidgetBase widget, @NotNull ScriptEditScreen screen){
		super(0, 0, 20, 20, Text.of(""));
		setWrappedWidget(widget);
		this.screen = screen;
	}
	
	public void setWrappedWidget(@Nullable WidgetBase widget){
		if(widget == null) widget = defaultWidget;
		this.widget = widget;
		setX(widget.getX());
		setY(widget.getY());
		setWidth(widget.getX());
		setHeight(widget.getHeight());
	}
	
	@Override public void postRenderHovered(DrawContext drawContext, int mouseX, int mouseY, boolean selected){
		widget.postRenderHovered(drawContext, mouseX, mouseY, selected);
	}
	@Override public void setX(int x) {
		super.setX(x);
		widget.setX(x);
	}
	@Override public void setY(int y) {
		super.setY(y);
		widget.setY(y);
	}
	@Override public int getX() {return widget.getX();}
	@Override public int getY() {return widget.getY();}
	@Override public int getWidth() {return widget.getWidth();}
	@Override public int getHeight() {return widget.getHeight();}
	@Override protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
	@Override protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		widget.render(context, mouseX, mouseY, widget.isMouseOver(mouseX, mouseY) || this == screen.getFocused() || this == screen.getScriptFocused());
	}
	@Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
		return widget.onMouseClicked((int)Math.floor(mouseX), (int)Math.floor(mouseY), button);
	}
	@Override public boolean mouseReleased(double mouseX, double mouseY, int button) {
		widget.onMouseReleased((int)Math.floor(mouseX), (int)Math.floor(mouseY), button);
		return false;
	}
	@Override public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		return widget.onMouseScrolled((int)Math.floor(mouseX), (int)Math.floor(mouseY), horizontalAmount, verticalAmount);
	}
	@Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return widget.onKeyTyped(keyCode, scanCode, modifiers);
	}
	@Override public boolean charTyped(char chr, int modifiers) {
		return widget.onCharTyped(chr, modifiers);
	}
	@Override public boolean isMouseOver(double mouseX, double mouseY) {
		return widget.isMouseOver((int) mouseX, (int) mouseY);
	}
	
	public static HoveredClickableWidget wrap(WidgetBase widget, ScriptEditScreen screen){
		return new WidgetWrapper(widget, screen);
	}
}
