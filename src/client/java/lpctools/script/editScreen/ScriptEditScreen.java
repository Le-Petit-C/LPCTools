package lpctools.script.editScreen;

import com.google.gson.JsonElement;
import fi.dy.masa.malilib.gui.*;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.ConfigButtonKeybind;
import fi.dy.masa.malilib.gui.widgets.WidgetBase;
import fi.dy.masa.malilib.util.position.Vec2i;
import lpctools.mixin.client.MASAMixins.GuiBaseAccessor;
import lpctools.mixin.client.TextFieldWidgetAccessor;
import lpctools.mixinInterfaces.MASAMixins.IButtonGenericMixin;
import lpctools.mixinInterfaces.MASAMixins.IConfigButtonKeybindMixin;
import lpctools.script.IScript;
import lpctools.script.ISubScriptMutable;
import lpctools.script.Script;
import lpctools.script.ScriptConfig;
import lpctools.util.data.AvlTreeList;
import lpctools.util.data.Rect2i;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

import static lpctools.lpcfymasaapi.LPCConfigUtils.calculateTextButtonWidth;

public class ScriptEditScreen extends GuiConfigsBase {
	public final ScriptConfig config;
	private final JsonElement startJson;
	public ScriptEditScreen(Script script) {
		super(0, 0, "lpctools", null, "lpctools.script.editScreen.title");
		this.config = script.config;
		startJson = script.getAsJsonElement();
		//setTitle(Text.translatable("lpctools.script.editScreen.title").getString());
	}
	//鼠标左键按下抓住背景时移动鼠标应当能够移动整个界面
	//此变量为鼠标按下时的坐标，为null表示未按下
	private Vec2i holdingScreenBackground = null;
	@Override public boolean onMouseClicked(int mouseX, int mouseY, int mouseButton) {
		boolean res = super.onMouseClicked(mouseX, mouseY, mouseButton);
		if (!res && mouseButton == 0) {
			holdingScreenBackground = new Vec2i(mouseX, mouseY);
			return true;
		}
		return res;
	}
	@Override public boolean onMouseReleased(int mouseX, int mouseY, int state) {
		boolean res = super.onMouseReleased(mouseX, mouseY, state);
		if (holdingScreenBackground != null && state == 0) {
			holdingScreenBackground = null;
			return true;
		}
		return res;
	}
	@Override public boolean onKeyTyped(int keyCode, int scanCode, int modifiers) {
		for (ButtonBase button : ((GuiBaseAccessor)this).getButtons())
			if (button.onKeyTyped(keyCode, scanCode, modifiers))
				return true;
		return super.onKeyTyped(keyCode, scanCode, modifiers);
	}
	
	private <T> void calcWidgetsBound(Rect2i res, Iterable<T> widgets, BiConsumer<? super T, Rect2i> getBound){
		Rect2i buf = new Rect2i();
		for(T widget : widgets){
			getBound.accept(widget, buf);
			res.expandToInclude(buf);
		}
	}
	private Rect2i calcWidgetsBound(){
		Rect2i res = new Rect2i();
		GuiBaseAccessor accessor = (GuiBaseAccessor)this;
		BiConsumer<WidgetBase, Rect2i> widgetRectCalc = (widget, rect)->{
			int midY = widget.getY() + widget.getHeight() / 2;
			rect.set(
				widget.getX(), midY - 10,
				widget.getX() + widget.getWidth(),
				midY + 10
			);
		};
		calcWidgetsBound(res, accessor.getButtons(), widgetRectCalc);
		calcWidgetsBound(res, accessor.getWidgets(), widgetRectCalc);
		calcWidgetsBound(res, accessor.getTextFields(),
			(widget, rect)->{
				GuiTextFieldGeneric textField = widget.getTextField();
				int midY = textField.getY() + textField.getHeight() / 2;
				rect.set(
					textField.getX(), midY - 10,
					textField.getX() + textField.getWidth(),
					midY + 10
				);
			}
		);
		return res;
	}
	
	private static final int reserve = 10, topReserve = 40;
	//移动后左上角坐标
	private int x = 0, y = 0;
	@Override public void render(DrawContext drawContext, int mouseX, int mouseY, float partialTicks) {
		if(needUpdate){
			needUpdate = false;
			initGui();
		}
		if (holdingScreenBackground != null) {
			int deltaX = mouseX - holdingScreenBackground.x;
			int deltaY = mouseY - holdingScreenBackground.y;
			holdingScreenBackground = new Vec2i(mouseX, mouseY);
			Rect2i bound = calcWidgetsBound();
			if(bound.isValid()){
				//为组件到边界保留一些距离
				bound.left -= reserve + 1;
				bound.top -= topReserve + 1;//因为有标题栏，所以上方留更多
				bound.right += reserve + 1;
				bound.bottom += reserve + 1;
				if(bound.width() > getScreenWidth()) {
					deltaX = Math.min(deltaX, -bound.left);
					deltaX = Math.max(deltaX, getScreenWidth() - bound.right);
				}
				else{
					deltaX = Math.max(deltaX, -bound.left);
					deltaX = Math.min(deltaX, getScreenWidth() - bound.right);
				}
				if(bound.height() > getScreenHeight()) {
					deltaY = Math.min(deltaY, -bound.top);
					deltaY = Math.max(deltaY, getScreenHeight() - bound.bottom);
				}
				else{
					deltaY = Math.max(deltaY, -bound.top);
					deltaY = Math.min(deltaY, getScreenHeight() - bound.bottom);
				}
			}
			//移动所有组件
			GuiBaseAccessor accessor = (GuiBaseAccessor) this;
			for (var widget : accessor.getButtons())
				widget.setPosition(widget.getX() + deltaX, widget.getY() + deltaY);
			for (var widget : accessor.getWidgets())
				widget.setPosition(widget.getX() + deltaX, widget.getY() + deltaY);
			for (var widget : accessor.getTextFields()){
				GuiTextFieldGeneric textField = widget.getTextField();
				textField.setPosition(textField.getX() + deltaX, textField.getY() + deltaY);
				((TextFieldWidgetAccessor)textField).invokeUpdateTextPosition();
			}
			x = bound.left + deltaX;
			y = bound.top + deltaY;
			drawContext.fill(x, y, bound.right + deltaX, bound.bottom + deltaY, 0x7fffffff);
		}
		if(getParent() != null)
			getParent().render(drawContext, -1, -1, partialTicks);
		super.render(drawContext, mouseX, mouseY, partialTicks);
	}
	@Override public void initGui() {
		super.initGui();
		buildButtons(config.script, x + reserve, y + topReserve + 11, true);
	}
	public <T extends ButtonBase> T addButton(T button) {
		((GuiBaseAccessor)this).getButtons().add(button);
		return button;
	}
	public void realignWidgetsByNameButton(IScript script){
		var nameButton = getExtraData(script).nameButton;
		realignWidgets(script, nameButton.getX() - 23, nameButton.getY() + nameButton.getHeight() / 2);
	}
	public void realignWidgets(IScript script, int startX, int startY){
		buildButtons(script, startX, startY, false);
	}
	//返回值：添加了多少行按钮
	private int buildButtons(IScript script, int startX, int startY, boolean needAddWidget) {
		int res = 1;
		int x = startX + 22;
		var data = getExtraData(script);
		String name = script.getName();
		int width = calculateTextButtonWidth(name, textRenderer, 20);
		data.nameButton.setPosition(x + 1, startY - 10);
		addWidget(data.nameButton);
		x += width + 2;
		if(script.getWidgets() instanceof Iterable<?> widgets){
			for(Object object : widgets){
				if(object instanceof ConfigButtonKeybind keybind)
					((IConfigButtonKeybindMixin)keybind).setHost(this);
				switch (object) {
					case ButtonBase button -> {
						button.setPosition(x + 1, startY - button.getHeight() / 2);
						x += button.getWidth() + 2;
						if(needAddWidget) addButton(button);
					}
					case WidgetBase widget -> {
						widget.setPosition(x + 1, startY - widget.getHeight() / 2);
						x += widget.getWidth() + 2;
						if(needAddWidget) addWidget(widget);
					}
					case GuiTextFieldGeneric textField -> {
						textField.setPosition(x + 1, startY - textField.getHeight() / 2);
						((TextFieldWidgetAccessor)textField).invokeUpdateTextPosition();
						x += textField.getWidth() + 2;
						if(needAddWidget) addTextField(textField, null);
					}
					case null, default -> throw new IllegalStateException("Illegal object type");
				}
			}
		}
		if(script.getSubScripts() instanceof Iterable<? extends IScript> subScripts){
			var button = addButton(data.expandButton);
			button.clearGuides();
			if(data.extended()){
				((IButtonGenericMixin)button).setIcon(MaLiLibIcons.ARROW_UP);
				for(IScript subScript : subScripts){
					button.nextGuide(res);
					res += buildButtons(subScript, startX + 22, startY + res * 22, needAddWidget);
				}
				data.lines = res;
			}
			else ((IButtonGenericMixin)button).setIcon(MaLiLibIcons.ARROW_DOWN);
		}
		return res;
	}
	@Override public List<ConfigOptionWrapper> getConfigs() {return List.of();}
	private boolean needUpdate = false;
	public void markNeedUpdate(){needUpdate = true;}
	
	@Nullable ScriptExtraOptionButton extendedExtra;
	ISubScriptMutable holdingMutableScript;
	int holdingSubScriptIndex;
	private Vec2i holdingPos;
	private static ButtonGeneric createGenericSquareButton(String text, String hoverKey){
		return new ButtonGeneric(0, 0, 20, 20, text, Text.translatable(hoverKey).getString());
	}
	private final ButtonGeneric copyButton = createGenericSquareButton("C", "lpctools.script.trigger.chooseScreen.copy");
	private final ButtonGeneric pasteButton = createGenericSquareButton("P", "lpctools.script.trigger.chooseScreen.paste");
	private final ButtonGeneric removeButton = createGenericSquareButton("-", "lpctools.script.trigger.chooseScreen.remove");
	
	@NotNull ScriptExtraData getExtraData(IScript script){
		return scriptScreenExtraDate.computeIfAbsent(script, ScriptExtraData::new);
	}
	private final HashMap<IScript, ScriptExtraData> scriptScreenExtraDate = new HashMap<>();
	private final AvlTreeList<ScriptExtraData> scriptDisplayList = new AvlTreeList<>();
	@Override public void removed() {
		if(!Objects.equals(config.script.getAsJsonElement(), startJson))
			config.getPage().get().markConfigsModified();
		super.removed();
	}
	@Override protected void buildConfigSwitcher() {}
	@Override public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		boolean res = super.mouseClicked(mouseX, mouseY, mouseButton);
		if(!res) extendedExtra = null;
		return res;
	}
	
}
