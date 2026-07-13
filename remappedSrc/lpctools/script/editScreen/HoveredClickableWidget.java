package lpctools.script.editScreen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

public abstract class HoveredClickableWidget extends AbstractWidget {
	public HoveredClickableWidget(int x, int y, int width, int height, Component message) {
		super(x, y, width, height, message);
	}
	public abstract void postRenderHovered(GuiGraphics drawContext, int mouseX, int mouseY, boolean selected);
}
