package lpctools.lpcfymasaapi.widgets;

import fi.dy.masa.malilib.gui.widgets.WidgetBase;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Vec2f;

import java.util.ArrayList;
import java.util.Arrays;

import static lpctools.lpcfymasaapi.LPCConfigUtils.calculateTextButtonWidth;

public class WHAutoAdjustStringWidget extends WidgetBase {
	private boolean needUpdateSize = true;
	private Align align;
	private final ArrayList<String> texts = new ArrayList<>();
	private final int alignX, alignY;
	private int textStartX, textStartY;
	private int frameColor = 0xbf3f3fff, fillColor = 0x7f3f3f3f;
	public WHAutoAdjustStringWidget(int x, int y, Align align, String ...text) {
		super(x, y, 20, 20);
		this.align = align;
		setTexts(text);
		this.alignX = x;
		this.alignY = y;
	}
	
	public Align getAlign(){return align;}
	public void setAlign(Align align){
		this.align = align;
		needUpdateSize = true;
	}
	
	public void setFrameColor(int frameColor) {this.frameColor = frameColor;}
	public void setFillColor(int fillColor) {this.fillColor = fillColor;}
	public void setTexts(String[] texts){
		this.texts.clear();
		for(var str : texts){
			String[] separates = str.split("\n");
			this.texts.addAll(Arrays.asList(separates));
		}
		needUpdateSize = true;
	}
	public void setTexts(Iterable<String> texts){
		this.texts.clear();
		for(var str : texts){
			String[] separates = str.split("\n");
			this.texts.addAll(Arrays.asList(separates));
		}
		needUpdateSize = true;
	}
	
	@Override public void render(DrawContext drawContext, int mouseX, int mouseY, boolean selected) {
		tryUpdateSize();
		super.render(drawContext, mouseX, mouseY, selected);
		int x = getX(), y = getY(), w = getWidth(), h = getHeight();
		drawContext.fill(x - 2, y - 2, x + w + 2, y, frameColor);
		drawContext.fill(x - 2, y + h, x + w + 2, y + h + 2, frameColor);
		drawContext.fill(x - 2, y, x, y + h, frameColor);
		drawContext.fill(x + w, y, x + w + 2, y + h, frameColor);
		drawContext.fill(x, y, x + w, y + h, fillColor);
		for(int i = 0; i < texts.size(); ++i) this.drawStringWithShadow(drawContext,
			textStartX, textStartY + (textRenderer.fontHeight + 2) * i, 0xffffffff, texts.get(i));
	}
	
	@Override public void setX(int x) {
		textStartX += x - getX();
		super.setX(x);
	}
	
	@Override public void setY(int y) {
		textStartY += y - getY();
		super.setY(y);
	}
	
	@Override public int getX() {
		tryUpdateSize();
		return super.getX();
	}
	
	@Override public int getY() {
		tryUpdateSize();
		return super.getY();
	}
	
	@Override public int getWidth() {
		tryUpdateSize();
		return super.getWidth();
	}
	
	@Override public int getHeight() {
		tryUpdateSize();
		return super.getHeight();
	}
	
	private void tryUpdateSize(){
		if(!needUpdateSize) return;
		needUpdateSize = false;
		int width = calculateTextButtonWidth("", textRenderer, 20);
		int tx = width / 2, ty = (20 - textRenderer.fontHeight) / 2;
		for(String str : texts){
			int w = calculateTextButtonWidth(str, textRenderer, 20);
			if(w > width) width = w;
		}
		int height = (texts.size() - 1) * (textRenderer.fontHeight + 2) + 20;
		setWidth(width);
		setHeight(height);
		super.setX((int)(alignX + width * align.x));
		super.setY((int)(alignY + height * align.y));
		textStartX = getX() + tx;
		textStartY = getY() + ty;
	}
	
	@SuppressWarnings("unused")
	public static class Align extends Vec2f{
		public static final Align LEFT_UP = new Align(-1.0f, -1.0f);
		public static final Align RIGHT_UP = new Align(0.0f, -1.0f);
		public static final Align LEFT_DOWN = new Align(-1.0f, 0.0f);
		public static final Align RIGHT_DOWN = new Align(0.0f, 0.0f);
		public static final Align CENTER = new Align(-0.5f, -0.5f);
		public Align(float x, float y) {super(x, y);}
		public Align XOpposite(){return new Align(-1 - x, y);}
		public Align YOpposite(){return new Align(x, -1 - y);}
		public Align Opposite(){return new Align(-1 - x, -1 - y);}
	}
}
