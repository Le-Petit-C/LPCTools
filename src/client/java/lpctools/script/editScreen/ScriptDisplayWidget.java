package lpctools.script.editScreen;

import com.google.common.collect.Iterables;
import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.widgets.WidgetBase;
import fi.dy.masa.malilib.gui.wrappers.TextFieldWrapper;
import lpctools.script.IScript;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static lpctools.lpcfymasaapi.LPCConfigUtils.calculateTextButtonWidth;

public class ScriptDisplayWidget extends ClickableWidget{
	public final @Nullable ScriptWithSubScriptDisplayWidget parent;
	public final ScriptEditScreen editScreen;
	public final int depth;
	protected final @NotNull IScript script;
	protected final @Nullable ButtonGeneric nameButton;
	protected final @NotNull ArrayList<ClickableWidget> widgets = new ArrayList<>();
	private int right; //此widget及其子widget的最大right
	private boolean needUpdate = true;
	private boolean updating = false;
	
	/* 构造函数 */
	
	public ScriptDisplayWidget(IScript script) {
		super(0, 0, 22, 22, Text.of(""));
		this.script = script;
		var parentScript = script.getParent();
		this.parent = parentScript != null ? parentScript.getDisplayWidget() : null;
		this.editScreen = script.getScript().getEditScreen();
		if(parent != null) depth = parent.depth + 1;
		else depth = 0;
		String name = script.getName();
		if(name != null) nameButton = new ButtonGeneric(0, 0, calculateTextButtonWidth(name, editScreen.textRenderer, 20), 20, name)
			.setRenderDefaultBackground(false);
		else nameButton = null;
	}
	
	/* 成员方法 */
	@Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
		for(var widget : getAllWidgets()){
			if(widget.isMouseOver(mouseX, mouseY)){
				var res = widget.mouseClicked(mouseX, mouseY, button);
				if(res) return true;
			}
		}
		return false;
	}
	
	@Override public boolean mouseReleased(double mouseX, double mouseY, int button) {
		for(var widget : getAllWidgets()){
			if(widget.isMouseOver(mouseX, mouseY)){
				var res = widget.mouseReleased(mouseX, mouseY, button);
				if(res) return true;
			}
		}
		return false;
	}
	
	@Override public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		for(var widget : getAllWidgets()){
			if(widget.isMouseOver(mouseX, mouseY)){
				var res = widget.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
				if(res) return true;
			}
		}
		return false;
	}
	
	@Override public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		for(var widget : getAllWidgets()){
			if(widget.isMouseOver(mouseX, mouseY)){
				var res = widget.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
				if(res) return true;
			}
		}
		return false;
	}
	
	@Override public void mouseMoved(double mouseX, double mouseY) {
		for(var widget : getAllWidgets()){
			if(widget.isMouseOver(mouseX, mouseY))
				widget.mouseMoved(mouseX, mouseY);
		}
	}
	
	public IScript getIScript(){return script;}
	public int getRight(){return right;}
	//此Widget及子行占多少行，未展开或无subScript时始终为1
	public int getSize(){return 1;}
	public void markNeedUpdate(){needUpdate = true;}
	public void markParentUpdateChain(){
		var widget = parent;
		while(widget != null){
			if(widget.needUpdate()) break;
			widget.markNeedUpdate();
			widget = widget.parent;
		}
	}
	public void markUpdateChain(){
		var widget = this;
		do {
			if(widget.needUpdate()) break;
			widget.markNeedUpdate();
			widget = widget.parent;
		} while(widget != null);
	}
	public boolean needUpdate(){return needUpdate;}
	public int getLine(){
		int res = 0;
		var widget = this;
		while(widget.parent != null){
			++res;
			res += widget.parent.lineOf(widget);
			widget = widget.parent;
		}
		return res;
	}
	
	public void tryUpdate(){
		if(needUpdate && !updating) {
			updating = true;
			update();
			updating = false;
			needUpdate = false;
		}
	}
	
	//更新widgets位置
	public void updateDisplayWidgets(){
		int midY = 11 + getLine() * 22;
		int x = depth * 22;
		int newRight = x + 22;
		if (nameButton != null) newRight += buildWidget(newRight, midY, WidgetWrapper.wrap(nameButton, editScreen));
		widgets.clear();
		if(script.getWidgets() instanceof Iterable<?> _widgets){
			for(Object o : _widgets){
				switch (o){
					case WidgetBase w -> {
						var widget = WidgetWrapper.wrap(w, editScreen);
						widgets.add(widget);
						newRight += buildWidget(newRight, midY, widget);
					}
					case GuiTextFieldGeneric t -> {
						widgets.add(t);
						newRight += buildWidget(newRight, midY, t);
					}
					case TextFieldWrapper<?> w -> {
						var textField = w.getTextField();
						widgets.add(textField);
						newRight += buildWidget(newRight, midY, textField);
					}
					default -> throw new IllegalArgumentException("Unexpected button type: " + o);
				}
			}
		}
		setPosition(x, getLine() * 22);
		setWidth(newRight - x);
	}
	
	protected void update(){
		updateDisplayWidgets();
		int newRight = getX() + getWidth() + 44;
		if(parent instanceof ScriptWithSubScriptMutableDisplayWidget<?>) newRight += 66;
		
		//更新right
		if (right != newRight) {
			markParentUpdateChain();
			right = newRight;
		}
	}
	
	//渲染时并不渲染所有而是只渲染自己这一行，应当由ScriptEditScreen来决定具体渲染哪些行
	@Override public void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		updateDisplayWidgets();
		if (nameButton != null) nameButton.render(context, mouseX, mouseY, nameButton.isMouseOver(mouseX, mouseY));
		for (var widget : widgets) widget.render(context, mouseX, mouseY, deltaTicks);
	}
	
	protected Iterable<ClickableWidget> getAllWidgets(){
		if (nameButton != null) return Iterables.concat(List.of(WidgetWrapper.wrap(nameButton, editScreen)), widgets);
		else return widgets;
	}
	
	//根据line获取其下第line行的ScriptDisplayWidget，自身line为0
	@Nullable ScriptDisplayWidget getByLine(int line){
		return line == 0 ? this : null;
	}
	
	@Override protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
	
	/* 静态函数 */
	
	protected static int buildWidget(int left, int midY, Widget widget){
		widget.setPosition(left + 1, midY - (widget.getHeight() >> 1));
		return widget.getWidth() + 2;
	}
}
