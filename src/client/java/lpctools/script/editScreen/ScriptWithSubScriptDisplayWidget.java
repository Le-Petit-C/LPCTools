package lpctools.script.editScreen;

import com.google.common.collect.Iterables;
import fi.dy.masa.malilib.gui.LeftRight;
import fi.dy.masa.malilib.gui.MaLiLibIcons;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lpctools.mixinInterfaces.MASAMixins.IButtonGenericMixin;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.ScriptConfigs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ScriptWithSubScriptDisplayWidget extends ScriptDisplayWidget{
	public final @NotNull ButtonGeneric expandButton;
	boolean extended = false;
	private final ArrayList<ScriptDisplayWidget> subWidgets = new ArrayList<>();
	private final IntArrayList lineList = new IntArrayList();
	private final Object2IntOpenHashMap<ScriptDisplayWidget> indexMap = new Object2IntOpenHashMap<>();
	private int subSize = 0, subRight = 0;
	public ScriptWithSubScriptDisplayWidget(IScriptWithSubScript script) {
		super(script);
		expandButton = new ButtonGeneric(0, 0, 20, 20, "");
		expandButton.setIconAlignment(LeftRight.CENTER);
		expandButton.setActionListener(((button, mouseButton) -> toggleExtended()));
		updateIcon();
	}
	
	@Override public void updateDisplayWidgets() {
		buildWidget(getX(), getLine() * 22 + 11, WidgetWrapper.wrap(expandButton, editScreen));
		super.updateDisplayWidgets();
	}
	
	@Override protected void update(){
		super.update();
		subRight = subSize = 0;
		indexMap.clear();
		lineList.clear();
		subWidgets.clear();
		for(var s : getIScript().getSubScripts())
			subWidgets.add(s.getDisplayWidget());
		for(var w : subWidgets){
			indexMap.put(w, lineList.size());
			lineList.add(subSize);
			w.tryUpdate();
			subSize += w.getSize();
			subRight = Math.max(subRight, w.getRight());
		}
	}
	
	//widget相对于起始位置的line index（第一个为0）
	public int lineOf(ScriptDisplayWidget widget){
		tryUpdate();
		int i = indexMap.getOrDefault(widget, -1);
		if(i >= 0) return lineList.getInt(i);
		else return -1;
	}
	public int lineOf(int index){return lineList.getInt(index);}
	public int indexOf(ScriptDisplayWidget widget){
		tryUpdate();
		return indexMap.getOrDefault(widget, -1);
	}
	int subCount(){
		tryUpdate();
		return subWidgets.size();
	}
	ScriptDisplayWidget getSub(int index){
		tryUpdate();
		return subWidgets.get(index);
	}
	
	@Override public IScriptWithSubScript getIScript() {
		return (IScriptWithSubScript)super.getIScript();
	}
	@Override public int getSize() {
		tryUpdate();
		if(extended) return subSize + 1;
		else return 1;
	}
	@Override public int getRight() {
		tryUpdate();
		if(extended) return Math.max(super.getRight(), subRight);
		else return super.getRight();
	}
	
	private void updateIcon(){((IButtonGenericMixin) expandButton).setIcon(extended ? MaLiLibIcons.ARROW_UP : MaLiLibIcons.ARROW_DOWN);}
	
	void toggleExtended(){
		extended = !extended;
		markUpdateChain();
		updateIcon();
	}
	
	public void renderExpandGuidelines(DrawContext context){
		tryUpdate();
		if(extended && !subWidgets.isEmpty()){
			int thickness = ScriptConfigs.guidelineThickness.getIntegerValue();
			int color = ScriptConfigs.guidelineColor.getIntegerValue();
			int left = getX() + (22 - thickness) / 2;
			int right = getX() + 22;
			int top = getY() + 22;
			int yShift = getY() + (getHeight() - thickness) / 2;
			int n = 1;
			int lastN = 0;
			for (var widget : subWidgets) {
				int y = yShift + n * 22;
				context.fill(left + thickness, y, right, y + thickness, color);
				lastN = n;
				n += widget.getSize();
			}
			int y = yShift + lastN * 22;
			context.fill(left, top, left + thickness, y + thickness, color);
		}
	}
	
	@Override public void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		tryUpdate();
		super.renderWidget(context, mouseX, mouseY, deltaTicks);
		expandButton.render(context, mouseX, mouseY, expandButton.isMouseOver(mouseX, mouseY));
		renderExpandGuidelines(context);
	}
	
	@Override protected Iterable<ClickableWidget> getAllWidgets() {
		tryUpdate();
		if (nameButton != null) return Iterables.concat(List.of(WidgetWrapper.wrap(expandButton, editScreen), WidgetWrapper.wrap(nameButton, editScreen)), widgets);
		else return Iterables.concat(List.of(WidgetWrapper.wrap(expandButton, editScreen)), widgets);
	}
	
	@Override @Nullable ScriptDisplayWidget getByLine(int line) {
		tryUpdate();
		if(line < 0 || line >= getSize()) return null;
		else if(line == 0) return this;
		else {
			--line;
			ScriptDisplayWidget leftSub = getSub(0);
			if(line == 0) return leftSub;
			int leftLine = 0;
			int left = 0, right = subCount();
			//二分法
			while(right - left > 1){
				int middle = (left + right) >> 1;
				var midSub = getSub(middle);
				int midLine = lineOf(midSub);
				if(midLine == line) return midSub;
				else if(midLine < line) {
					left = middle;
					leftLine = midLine;
					leftSub = midSub;
				}
				else right = middle;
			}
			return leftSub.getByLine(line - leftLine);
		}
	}
}
