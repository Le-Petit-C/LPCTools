package lpctools.script.editScreen;

import fi.dy.masa.malilib.gui.LeftRight;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import lpctools.script.IScript;
import lpctools.script.ScriptConfigs;
import net.minecraft.client.gui.DrawContext;

class GuidelineExpandButton extends ButtonGeneric {
	public final ScriptEditScreen scriptEditScreen;
	final IntArrayList guided = new IntArrayList();
	GuidelineExpandButton(ScriptEditScreen scriptEditScreen, IScript script) {
		super(0, 0, 20, 20, "");
		this.scriptEditScreen = scriptEditScreen;
		setIconAlignment(LeftRight.CENTER);
		setActionListener((button, mouseButton) -> scriptEditScreen.getExtraData(script).toggleExtended());
	}
	void clearGuides() {guided.clear();}
	void nextGuide(int n) {guided.add(n);}
	@Override public void render(DrawContext drawContext, int mouseX, int mouseY, boolean selected) {
		super.render(drawContext, mouseX, mouseY, selected);
		if (!guided.isEmpty()) {
			int thickness = ScriptConfigs.guidelineThickness.getIntegerValue();
			int color = ScriptConfigs.guidelineColor.getIntegerValue();
			int left = getX() + (getWidth() - thickness) / 2;
			int right = getX() + getWidth();
			int top = getY() + 22;
			int yShift = getY() + (getHeight() - thickness) / 2;
			for (int n : guided) {
				int y = yShift + n * 22;
				drawContext.fill(left + thickness, y, right, y + thickness, color);
			}
			int y = yShift + guided.getLast() * 22;
			drawContext.fill(left, top, left + thickness, y + thickness, color);
		}
	}
}
