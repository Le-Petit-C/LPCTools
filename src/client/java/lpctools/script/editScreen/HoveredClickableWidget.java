package lpctools.script.editScreen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public abstract class HoveredClickableWidget extends ClickableWidget {
	public HoveredClickableWidget(int x, int y, int width, int height, Text message) {
		super(x, y, width, height, message);
	}
	public abstract void postRenderHovered(DrawContext drawContext, int mouseX, int mouseY, boolean selected);
}
