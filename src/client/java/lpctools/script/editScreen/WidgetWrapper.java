package lpctools.script.editScreen;

import fi.dy.masa.malilib.gui.widgets.WidgetBase;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public class WidgetWrapper {
	public static ClickableWidget wrap(WidgetBase widget, ScriptEditScreen screen){
		return new ClickableWidget(widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight(), Text.of("")) {
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
				if(widget.onMouseClicked((int)Math.floor(mouseX), (int)Math.floor(mouseY), button)) return true;
				return super.mouseClicked(mouseX, mouseY, button);
			}
			@Override public boolean mouseReleased(double mouseX, double mouseY, int button) {
				widget.onMouseReleased((int)Math.floor(mouseX), (int)Math.floor(mouseY), button);
				return super.mouseReleased(mouseX, mouseY, button);
			}
			@Override public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
				if(widget.onMouseScrolled((int)Math.floor(mouseX), (int)Math.floor(mouseY), horizontalAmount, verticalAmount)) return true;
				return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
			}
			@Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
				if(widget.onKeyTyped(keyCode, scanCode, modifiers)) return true;
				return super.keyPressed(keyCode, scanCode, modifiers);
			}
			@Override public boolean charTyped(char chr, int modifiers) {
				if(widget.onCharTyped(chr, modifiers)) return true;
				return super.charTyped(chr, modifiers);
			}
		};
	}
}
