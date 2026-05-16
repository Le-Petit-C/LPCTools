package lpctools.script.editScreen;

import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.widgets.WidgetBase;
import fi.dy.masa.malilib.render.GuiContext;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class WidgetWrapper extends HoveredClickableWidget{
	private static final ButtonGeneric defaultWidget = new ButtonGeneric(0, 0, 20, 20, "").setRenderDefaultBackground(false);
	private @NotNull WidgetBase widget = defaultWidget;
	public @NotNull ScriptEditScreen screen;
	public WidgetWrapper(@Nullable WidgetBase widget, @NotNull ScriptEditScreen screen){
		super(0, 0, 20, 20, Component.nullToEmpty(""));
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
	
	@Override public void postRenderHovered(GuiGraphicsExtractor drawContext, int mouseX, int mouseY, boolean selected){
		widget.postRenderHovered(GuiContext.fromGuiGraphics(drawContext), mouseX, mouseY, selected);
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
	@Override protected void updateWidgetNarration(@NonNull NarrationElementOutput builder) {}
	@Override protected void extractWidgetRenderState(@NonNull GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
		widget.render(GuiContext.fromGuiGraphics(context), mouseX, mouseY, widget.isMouseOver(mouseX, mouseY) || this == screen.getFocused() || this == screen.getScriptFocused());
	}
	@Override public boolean mouseClicked(@NonNull MouseButtonEvent click, boolean doubleClick) {
		return widget.onMouseClicked(click, doubleClick);
	}
	@Override public boolean mouseReleased(@NonNull MouseButtonEvent click) {
		widget.onMouseReleased(click);
		return false;
	}
	@Override public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		return widget.onMouseScrolled((int)Math.floor(mouseX), (int)Math.floor(mouseY), horizontalAmount, verticalAmount);
	}
	
	@Override public boolean keyPressed(@NonNull KeyEvent input) {
		return widget.onKeyTyped(input);
	}
	
	@Override public boolean charTyped(@NonNull CharacterEvent input) {
		return widget.onCharTyped(input);
	}
	@Override public boolean isMouseOver(double mouseX, double mouseY) {
		return widget.isMouseOver((int) mouseX, (int) mouseY);
	}
	
	public static HoveredClickableWidget wrap(WidgetBase widget, ScriptEditScreen screen){
		return new WidgetWrapper(widget, screen);
	}
}
