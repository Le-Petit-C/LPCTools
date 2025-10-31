package lpctools.script.editScreen;

import fi.dy.masa.malilib.gui.*;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.util.position.Vec2d;
import lpctools.script.*;
import lpctools.util.data.Rect2d;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static lpctools.lpcfymasaapi.LPCConfigUtils.calculateTextButtonWidth;
import static lpctools.script.ScriptConfigs.dragBoundaryConstraint;
import static lpctools.script.ScriptConfigs.dragVisualMode;

public class ScriptEditScreen extends GuiConfigsBase {
	public final Script script;
	@Nullable ScriptHoldingData scriptHoldingData;
	//鼠标左键按下抓住背景时移动鼠标应当能够移动整个界面
	//此变量为鼠标按下时鼠标按下位置相对于显示区域左上角的坐标，为null表示未按下
	private Vec2d holdingScreenBackground = null;
	//移动后左上角坐标
	private double x = reserve, y = topReserve;
	private @Nullable Element scriptFocused;
	private final ButtonGeneric copyButton = createGenericSquareButton("C", "lpctools.script.trigger.chooseScreen.copy");
	private final ButtonGeneric pasteButton = createGenericSquareButton("P", "lpctools.script.trigger.chooseScreen.paste");
	private final ButtonGeneric dragButton = createGenericSquareButton("≡", "lpctools.script.trigger.chooseScreen.drag");
	private final ButtonGeneric insertButton = createGenericSquareButton("+", "lpctools.script.trigger.chooseScreen.insert");
	private final ButtonGeneric removeButton = createGenericSquareButton("-", "lpctools.script.trigger.chooseScreen.remove");
	private final ButtonGeneric infoButton = new ButtonGeneric(0, 0, 22, 22, "");
	private long infoDisplayEndTime = 0;
	private final HashMap<IScript, ArrayList<ScriptRuntimeException>> runtimeExceptions = new HashMap<>();
	
	private static final int reserve = 10, topReserve = 40;
	
	public ScriptEditScreen(Script script) {
		super(0, 0, "lpctools", null, "lpctools.script.editScreen.title");
		this.script = script;
		infoButton.setRenderDefaultBackground(false);
		//setTitle(Text.translatable("lpctools.script.editScreen.title").getString());
	}
	private @NotNull ScriptWithSubScriptDisplayWidget getRootDisplayWidget(){
		return script.getDisplayWidget();
	}
	
	public void displayInfo(String text){
		var client = MinecraftClient.getInstance();
		var mouse = client.mouse;
		var window = client.getWindow();
		int x = (int)mouse.getScaledX(window);
		int y = (int)mouse.getScaledY(window) - infoButton.getHeight();
		infoButton.setDisplayString(text);
		infoButton.setWidth(calculateTextButtonWidth(text, textRenderer, infoButton.getHeight()));
		if(x + infoButton.getWidth() > getScreenWidth()) x = getScreenWidth() - infoButton.getWidth();
		if(y < 0) y = 0;
		infoButton.setPosition(x, y - infoButton.getHeight());
		infoDisplayEndTime = System.currentTimeMillis() + 2000;
	}
	
	public void setScriptFocused(@Nullable Element element){
		if(scriptFocused != null) scriptFocused.setFocused(false);
		scriptFocused = element;
		if (element != null) element.setFocused(true);
	}
	
	public @Nullable Element getScriptFocused(){return scriptFocused;}
	
	public void clearRuntimeExceptions(){runtimeExceptions.clear();}
	public void putRuntimeException(IScript script, ScriptRuntimeException runtimeException){
		runtimeExceptions.computeIfAbsent(script, v->new ArrayList<>()).add(runtimeException);
	}
	
	@Override public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		if(super.mouseClicked(mouseX, mouseY, mouseButton)) {
			setScriptFocused(null);
			return true;
		}
		double fixedMouseX = mouseX - x;
		double fixedMouseY = mouseY - y;
		var widget = getRootDisplayWidget().getByLine((int)Math.floor(fixedMouseY / 22));
		if(widget != null){
			if(widget.mouseClicked(fixedMouseX, fixedMouseY, mouseButton))
				return true;
		}
		setScriptFocused(null);
		if (mouseButton == 0) {
			if(widget != null){
				int mx = (int)Math.floor(fixedMouseX), my = (int)Math.floor(fixedMouseY);
				if(copyButton.onMouseClicked(mx, my, mouseButton)){
					ScriptData.setClipboard(widget.script.getAsJsonElement(), widget.script.getClass());
					displayInfo("Copied");
					return true;
				}
				else if(pasteButton.onMouseClicked(mx, my, mouseButton)){
					if(ScriptData.pasteTo(json->{
						widget.script.setValueFromJsonElement(json);
						widget.markUpdateChain();
					}, widget.script.getClass()))
						displayInfo("Pasted");
					else displayInfo("Failed to paste");
					return true;
				}
				else if(widget.parent instanceof ScriptWithSubScriptMutableDisplayWidget<?> parent) {
					if(dragButton.onMouseClicked(mx, my, mouseButton)){
						scriptHoldingData = new ScriptHoldingData(
							new Vec2d(fixedMouseX - widget.getX(), fixedMouseY - widget.getY()),
							widget
						);
						return true;
					}
					else if(insertButton.onMouseClicked(mx, my, mouseButton)){
						insertionButtonClicked(widget, parent);
						return true;
					}
					else if(removeButton.onMouseClicked(mx, my, mouseButton)){
						int i = parent.indexOf(widget);
						if(i >= 0) {
							parent.getIScript().getSubScripts().remove(i);
							parent.markUpdateChain();
						}
						else displayInfo("???为什么会这样？。。出现了一个index为-1的问题，本来不该这样的");
						return true;
					}
				}
			}
			holdingScreenBackground = new Vec2d(mouseX - x, mouseY - y);
			return true;
		}
		return false;
	}
	
	@Override public void mouseMoved(double mouseX, double mouseY) {
		double fixedMouseX = mouseX - x;
		double fixedMouseY = mouseY - y;
		if(scriptFocused != null) scriptFocused.mouseMoved(fixedMouseX, fixedMouseY);
		//移动显示区域
		if (holdingScreenBackground != null) {
			var root = getRootDisplayWidget();
			x = mouseX - holdingScreenBackground.x;
			y = mouseY - holdingScreenBackground.y;
			int width = root.getRight();
			int height = root.getSize() * 22;
			Rect2d bound = new Rect2d(x - reserve, y - topReserve, x + width + reserve, y + height + reserve);
			int SCRW = getScreenWidth(), SCRH = getScreenHeight();
			boolean b1 = bound.right - bound.left > SCRW;
			boolean b2 = bound.bottom - bound.top > SCRH;
			if((bound.left > 0) == b1) x -= bound.left;
			else if((bound.right < SCRW) == b1) x -= bound.right - SCRW;
			if((bound.top > 0) == b2) y -= bound.top;
			else if((bound.bottom < SCRH) == b2) y -= bound.bottom - SCRH;
		}
		//移动配置
		else if(scriptHoldingData != null){
			var widget = scriptHoldingData.widget;
			var parent = scriptHoldingData.parent;
			double heldLine = (fixedMouseY - scriptHoldingData.holdingPos.y) / 22 - parent.getLine() - 1;
			int currIndex = parent.indexOf(widget);
			int currLine = parent.lineOf(widget);
			int targetIndex;
			if(heldLine < currLine){
				if(currIndex > 0 && currLine - heldLine > parent.getSub(currIndex - 1).getSize() * 0.5)
					targetIndex = currIndex - 1;
				else targetIndex = -1;
			}
			else {
				if(currIndex + 1 < parent.subCount() && heldLine - currLine > parent.getSub(currIndex + 1).getSize() * 0.5)
					targetIndex = currIndex + 1;
				else targetIndex = -1;
			}
			if(targetIndex >= 0){
				Collections.swap(parent.getIScript().getSubScripts(), currIndex, targetIndex);
				widget.markUpdateChain();
			}
		}
		else {
			var widget = getRootDisplayWidget().getByLine((int)Math.floor(fixedMouseY / 22));
			if(widget != null) widget.mouseMoved(fixedMouseX, fixedMouseY);
		}
	}
	
	@Override public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
		double fixedMouseX = mouseX - x;
		double fixedMouseY = mouseY - y;
		if (holdingScreenBackground != null && mouseButton == 0) {
			holdingScreenBackground = null;
			return true;
		}
		if(scriptHoldingData != null && mouseButton == 0) {
			scriptHoldingData = null;
			return true;
		}
		if(scriptFocused != null && scriptFocused.mouseReleased(fixedMouseX, fixedMouseY, mouseButton)) return true;
		var widget = getRootDisplayWidget().getByLine((int)Math.floor(fixedMouseY / 22));
		if(widget != null && widget.mouseReleased(fixedMouseX, fixedMouseY, mouseButton)) return true;
		return super.mouseReleased(mouseX, mouseY, mouseButton);
	}
	
	@Override public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		double fixedMouseX = mouseX - x;
		double fixedMouseY = mouseY - y;
		if(scriptFocused != null && scriptFocused.mouseDragged(fixedMouseX, fixedMouseY, button, deltaX, deltaY)) return true;
		var widget = getRootDisplayWidget().getByLine((int)Math.floor(fixedMouseY / 22));
		if(widget != null && widget.mouseDragged(fixedMouseX, fixedMouseY, button, deltaX, deltaY)) return true;
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}
	
	@Override public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		double fixedMouseX = mouseX - x;
		double fixedMouseY = mouseY - y;
		if(scriptFocused != null && scriptFocused.mouseScrolled(fixedMouseX, fixedMouseY, horizontalAmount, verticalAmount)) return true;
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}
	
	@Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if(scriptFocused != null && scriptFocused.keyPressed(keyCode, scanCode, modifiers)) return true;
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	
	@Override public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
		if(scriptFocused != null && scriptFocused.keyReleased(keyCode, scanCode, modifiers)) return true;
		return super.keyReleased(keyCode, scanCode, modifiers);
	}
	
	@Override public boolean charTyped(char charIn, int modifiers) {
		if(scriptFocused != null && scriptFocused.charTyped(charIn, modifiers)) return true;
		return super.charTyped(charIn, modifiers);
	}
	
	//以父screen为背景
	@Override public void render(DrawContext drawContext, int mouseX, int mouseY, float partialTicks) {
		if(getParent() != null)
			getParent().render(drawContext, -1, -1, partialTicks);
		super.render(drawContext, mouseX, mouseY, partialTicks);
	}
	//渲染内容，选择重载drawTitle只是因为渲染顺序
	@Override public void drawTitle(DrawContext drawContext, int mouseX, int mouseY, float partialTicks) {
		//渲染内容
		var root = getRootDisplayWidget();
		var matrices = drawContext.getMatrices();
		root.tryUpdate();
		//位置修正
		matrices.pushMatrix().translate((float)x, (float)y);
		int fixedMouseX = (int)Math.round(mouseX - x), fixedMouseY = (int)Math.round(mouseY - y);
		if(holdingScreenBackground != null)
			drawContext.fill(-reserve, -topReserve, root.getRight() + reserve, root.getSize() * 22 + reserve, 0x7fffffff);
		int minLineIndex = Math.max((int)Math.floor(-y / 22), 0);
		int maxLineIndex = Math.min((int)Math.floor((getScreenHeight() - y - 1) / 22), root.getSize() - 1);
		int line = minLineIndex;
		//移动中的配置仅在目标位置绘制高亮框
		int hideMinLineIndex, hideMaxLineIndex;
		if(scriptHoldingData != null){
			var widget = scriptHoldingData.widget;
			hideMinLineIndex = widget.getLine();
			hideMaxLineIndex = hideMinLineIndex + widget.getSize();
		}
		else hideMinLineIndex = hideMaxLineIndex = 0;
		for(int i = minLineIndex; i <= maxLineIndex; ++i){
			var w = root.getByLine(i);
			if(w == null) break;
			if(line >= hideMinLineIndex && line < hideMaxLineIndex)
				drawContext.fill(w.getX() + 1, w.getY() + 1, w.getX() + w.getWidth() - 1, w.getY() + w.getHeight() - 1, 0x7fffffff);
			else w.render(drawContext, fixedMouseX, fixedMouseY, partialTicks);
			++line;
		}
		//绘制左侧引导线
		if(root.getByLine(minLineIndex) instanceof ScriptDisplayWidget w){
			var widget = w.parent;
			while (widget != null) {
				widget.renderExpandGuidelines(drawContext);
				widget = widget.parent;
			}
		}
		
		//光标处在的widget，用于后续移动和绘制拓展功能按钮
		ScriptDisplayWidget hoverWidget;
		//绘制移动中的配置的目标位置高亮框及其抓在手上的样子
		if(scriptHoldingData != null){
			//修正移动
			var holdingPos = scriptHoldingData.holdingPos;
			hoverWidget = scriptHoldingData.widget;
			var client = MinecraftClient.getInstance();
			var window = client.getWindow();
			var mouse = client.mouse;
			var method = dragVisualMode.get();
			double dx = method.moveX ? mouse.getScaledX(window) - holdingPos.x - x - hoverWidget.getX() : 0;
			double dy = method.moveY ? mouse.getScaledY(window) - holdingPos.y - y - hoverWidget.getY() : 0;
			if(dragBoundaryConstraint.getAsBoolean() && hoverWidget.parent instanceof ScriptWithSubScriptDisplayWidget parent){
				int l = parent.indexOf(hoverWidget);
				if(l == parent.subCount() - 1 && dy > 0) dy = 0;
				if(l == 0 && dy < 0) dy = 0;
			}
			matrices.translate((float)dx, (float)dy);
			fixedMouseX = (int)Math.round(holdingPos.x + hoverWidget.getX());
			fixedMouseY = (int)Math.round(holdingPos.y + hoverWidget.getY());
			//绘制
			double startY = mouseY - holdingPos.y;
			for(int i = 0; startY < getScreenHeight(); ++i){
				var w = hoverWidget.getByLine(i);
				if(w == null) break;
				w.render(drawContext, fixedMouseX, fixedMouseY, partialTicks);
				startY += 22;
			}
		}
		else hoverWidget = root.getByLine(Math.floorDiv(fixedMouseY, 22));
		//绘制拖动按钮
		if(hoverWidget != null){
			int x = hoverWidget.getX() + hoverWidget.getWidth() + 1, y = hoverWidget.getY() + 1;
			copyButton.setPosition(x, y);
			pasteButton.setPosition(x + 22, y);
			copyButton.render(drawContext, fixedMouseX, fixedMouseY, copyButton.isMouseOver(fixedMouseX, fixedMouseY));
			pasteButton.render(drawContext, fixedMouseX, fixedMouseY, pasteButton.isMouseOver(fixedMouseX, fixedMouseY));
			var parentScript = hoverWidget.script.getParent();
			if(parentScript instanceof IScriptWithSubScriptMutable<?>){
				dragButton.setPosition(x + 44, y);
				insertButton.setPosition(x + 66, y);
				removeButton.setPosition(x + 88, y);
				dragButton.render(drawContext, fixedMouseX, fixedMouseY, dragButton.isMouseOver(fixedMouseX, fixedMouseY));
				insertButton.render(drawContext, fixedMouseX, fixedMouseY, insertButton.isMouseOver(fixedMouseX, fixedMouseY));
				removeButton.render(drawContext, fixedMouseX, fixedMouseY, removeButton.isMouseOver(fixedMouseX, fixedMouseY));
			}
		}
		matrices.popMatrix();
		
		if(infoDisplayEndTime > System.currentTimeMillis())
			infoButton.render(drawContext, -1, -1, false);
		
		super.drawTitle(drawContext, mouseX, mouseY, partialTicks);
	}
	@Override public List<ConfigOptionWrapper> getConfigs() {return List.of();}
	@Override public boolean shouldPause() {
		var parent = getParent();
		if(parent != null) return parent.shouldPause();
		else return super.shouldPause();
	}
	
	private static ButtonGeneric createGenericSquareButton(String text, String hoverKey){
		return new ButtonGeneric(0, 0, 20, 20, text, Text.translatable(hoverKey).getString());
	}
	
	@Override public void removed() {
		script.config.getPage().get().markConfigsModified();
		super.removed();
		script.markNeedRecompile();
	}
	@Override protected void buildConfigSwitcher() {}
	
	private <T extends IScript> void insertionButtonClicked(ScriptDisplayWidget widget, ScriptWithSubScriptMutableDisplayWidget<T> parent){
		parent.getIScript().notifyInsertion(sub->{
			int i = parent.indexOf(widget);
			if (i >= 0) {
				parent.getIScript().getSubScripts().add(i + 1, sub);
				parent.markUpdateChain();
			} else displayInfo("???为什么会这样？。。出现了一个index为-1的问题，本来不该这样的");
		});
	}
	
	//抓住按钮拖动ScriptDisplayWidget时的记录数据
	private static class ScriptHoldingData{
		final @NotNull Vec2d holdingPos;// 鼠标抓住的位置，相对于ScriptDisplayWidget左上坐标
		final @NotNull ScriptDisplayWidget widget;
		final @NotNull ScriptWithSubScriptDisplayWidget parent;
		ScriptHoldingData(@NotNull Vec2d holdingPos, @NotNull ScriptDisplayWidget widget){
			this.holdingPos = holdingPos;
			this.widget = widget;
			if(widget.parent != null) parent = widget.parent;
			else throw new IllegalArgumentException();
		}
	}
}
