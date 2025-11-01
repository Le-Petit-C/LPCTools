package lpctools.script.editScreen;

import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.interfaces.IGuiIcon;
import net.minecraft.client.gui.DrawContext;
import org.jetbrains.annotations.Nullable;

import static lpctools.lpcfymasaapi.LPCConfigUtils.calculateTextButtonWidth;

public class WidthAutoAdjustButtonGeneric extends ButtonGeneric {
	public final ScriptDisplayWidget widget;
	private boolean needUpdateWidth = true;
	public WidthAutoAdjustButtonGeneric(ScriptDisplayWidget widget, int x, int y, int height, String text, @Nullable IGuiIcon icon, String... hoverStrings) {
		super(x, y, 20, height, text, icon, hoverStrings);
		this.widget = widget;
		textCentered = true;
	}
	@Override public void updateDisplayString() {
		super.updateDisplayString();
		needUpdateWidth = true;
	}
	@Override public void render(DrawContext drawContext, int mouseX, int mouseY, boolean selected) {
		if(needUpdateWidth){
			needUpdateWidth = false;
			setWidth(calculateTextButtonWidth(displayString, textRenderer, getHeight()));
			widget.markUpdateChain();
		}
		super.render(drawContext, mouseX, mouseY, selected);
	}
}
