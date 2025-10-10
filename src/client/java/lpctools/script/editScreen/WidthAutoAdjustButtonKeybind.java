package lpctools.script.editScreen;

import fi.dy.masa.malilib.gui.button.ConfigButtonKeybind;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import net.minecraft.client.gui.DrawContext;

import static lpctools.lpcfymasaapi.LPCConfigUtils.calculateTextButtonWidth;

public class WidthAutoAdjustButtonKeybind extends ConfigButtonKeybind {
	public final ScriptDisplayWidget widget;
	private boolean needUpdateWidth = true;
	public WidthAutoAdjustButtonKeybind(int x, int y, int height, IKeybind keybind, ScriptDisplayWidget widget) {
		super(x, y, 20, height, keybind, widget.editScreen);
		this.widget = widget;
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
