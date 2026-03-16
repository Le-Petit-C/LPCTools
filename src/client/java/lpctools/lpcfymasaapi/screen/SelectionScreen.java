package lpctools.lpcfymasaapi.screen;

import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static lpctools.generic.SelectionScreenConfigs.*;
import static lpctools.lpcfymasaapi.LPCConfigUtils.calculateTextButtonWidth;

// 先前的ChooseScreen感觉不够好用，重写一个
// 此界面开启时不会响应窗口大小变化事件，也就是说开启后若窗口大小发生变化有可能会排版异常
public class SelectionScreen<T> extends GuiBase {
	public interface IOption<T> {
		@NotNull Text getName();
		@Nullable Text getComment();
		void onSelected(int yourDepth, SelectionScreen<T> screen);
	}
	@SuppressWarnings("unused")
	public static class OptionNode<T> implements IOption<T> {
		private final Supplier<? extends Iterable<? extends IOption<T>>> subOptionSupplier;
		public final @NotNull Text name;
		public final @Nullable Text comment;
		public OptionNode(Supplier<? extends Iterable<? extends IOption<T>>> subOptionSupplier, @NotNull Text name, @Nullable Text comment){
			this.subOptionSupplier = subOptionSupplier;
			this.name = name;
			this.comment = comment;
		}
		public OptionNode(Supplier<? extends Iterable<? extends IOption<T>>> subOptionSupplier, @NotNull Text name) { this(subOptionSupplier, name, null); }
		public OptionNode(Iterable<? extends IOption<T>> subOptionSupplier, @NotNull Text name, @Nullable Text comment) { this(()->subOptionSupplier, name, comment); }
		public OptionNode(Iterable<? extends IOption<T>> subOptionSupplier, @NotNull Text name) { this(()->subOptionSupplier, name, null); }
		public static <T> OptionNode<T> ofOptions(Iterable<? extends T> subOptions,
												  @NotNull Function<T, Text> nameGenerator, @Nullable Function<T, Text> commentGenerator,
												  @NotNull Text name, @Nullable Text comment) {
			return new OptionNode<>(()->{
				var list = new ArrayList<OptionLeaf<T>>();
				subOptions.forEach(option->
					list.add(new OptionLeaf<>(option, nameGenerator.apply(option),
						commentGenerator == null ? null : commentGenerator.apply(option))));
				return list;
			}, name, comment);
		}
		public static <T> OptionNode<T> ofOptions(Iterable<? extends T> subOptions, @NotNull Function<T, Text> nameGenerator, @NotNull Text name){
			return ofOptions(subOptions, nameGenerator, null, name, null);
		}
		@Override public @NotNull Text getName() { return name; }
		@Override public @Nullable Text getComment() { return comment; }
		@Override public void onSelected(int yourDepth, SelectionScreen<T> screen) {
			screen.setOptionList(yourDepth + 1, subOptionSupplier.get());
		}
	}
	public static class OptionLeaf<T> implements IOption<T> {
		private final T callbackVal;
		public final @NotNull Text name;
		public final @Nullable Text comment;
		public OptionLeaf(T callbackVal, @NotNull Text name, @Nullable Text comment){
			this.callbackVal = callbackVal;
			this.name = name;
			this.comment = comment;
		}
		public OptionLeaf(T callbackVal, @NotNull Text name) { this(callbackVal, name, null); }
		@Override public @NotNull Text getName() { return name; }
		@Override public @Nullable Text getComment() { return comment; }
		@Override public void onSelected(int yourDepth, SelectionScreen<T> screen) {
			screen.applyCallback(callbackVal);
			screen.closeGui(true);
		}
	}
	
	private static final int buttonHeight = 20;
	private static final int buttonStride = 22;
	private static final int titleHeight = 53;
	private static final int scrollBarThickness = 2;
	
	private final ArrayList<OptionList> optionList = new ArrayList<>();
	private final Consumer<T> callback;
	private int selectedList = 0;
	// true时表示正在拖动滚动条，正在拖动的滚动条由selectedList指定。
	// 有效值（0~optionList.size()-1）时表示正在拖动对应OptionList的竖向滚动条，其他表示正在拖动SelectionScreen的横向滚动条
	private boolean isHoldingScrollBar = false;
	private static class ScrollBarInfo {
		double targetPosition = 0, currentPosition = 0;
		double holdingRatio;
		final double contentSize, viewSize, maxPosition;
		ScrollBarInfo(double contentSize, double viewSize){
			this.contentSize = contentSize;
			this.viewSize = viewSize;
			this.maxPosition = contentSize - viewSize;
		}
		static @Nullable ScrollBarInfo create(double contentSize, double viewSize){
			if(contentSize <= viewSize) return null;
			return new ScrollBarInfo(contentSize, viewSize);
		}
		static @Nullable ScrollBarInfo recreate(@Nullable ScrollBarInfo old, double contentSize, double viewSize){
			var res = create(contentSize, viewSize);
			if(old != null && res != null) {
				res.currentPosition = old.currentPosition;
				res.targetPosition = old.targetPosition;
			}
			return res;
		}
		void tick(long millisPassed){
			currentPosition = targetPosition + (currentPosition - targetPosition) * Math.exp(-millisPassed * approachSpeed.getDoubleValue() * 0.001);
			if(currentPosition > maxPosition) currentPosition = targetPosition = maxPosition;
			if(currentPosition < 0) currentPosition = targetPosition = 0;
		}
		// ratio范围在0~1之间，表示点击位置在滚动条上的比例
		void prepareClicked(double ratio){
			if(currentPosition / contentSize <= ratio && ratio <= (currentPosition + viewSize) / contentSize) targetPosition = currentPosition;
			else targetPosition = currentPosition = contentSize * ratio - viewSize / 2.0;
			this.holdingRatio = ratio;
			// 越界更新操作交给之后的tick()，此处不处理
		}
		void updateHolding(double ratio){
			targetPosition = currentPosition += (ratio - holdingRatio) * contentSize;
			holdingRatio = ratio;
		}
		void instant(){ currentPosition = targetPosition; }
		static double getPosition(@Nullable ScrollBarInfo info){
			return info == null ? 0 : info.currentPosition;
		}
	}
	
	private static class MillisTimer {
		private long lastMillis = System.currentTimeMillis();
		public long tick(){
			long currentMillis = System.currentTimeMillis();
			long res = currentMillis - lastMillis;
			lastMillis = currentMillis;
			return res;
		}
	}
	private static void tickScrollBarInfo(@Nullable ScrollBarInfo info, MillisTimer timer){
		long deltaMillis = timer.tick(); // 无论有没有info，都要更新timer
		if(info != null) info.tick(deltaMillis);
	}
	private final MillisTimer millisTimer = new MillisTimer();
	private ScrollBarInfo scrollBarInfo = null;
	
	public static <T> SelectionScreen<T> openSelectionScreen(@Nullable Text title, OptionNode<T> tree, @Nullable Screen parent, Consumer<T> callback){
		var res = new SelectionScreen<>(title, parent, callback);
		var mc = MinecraftClient.getInstance();
		if(mc.currentScreen == parent) mc.currentScreen = null;
		mc.setScreen(res);
		res.setOptionList(0, tree.subOptionSupplier.get());
		return res;
	}
	
	@SuppressWarnings("unused")
	public static <T> SelectionScreen<T> openSelectionScreen(OptionNode<T> tree, @Nullable Screen parent, Consumer<T> callback){
		return openSelectionScreen(null, tree, parent, callback);
	}
	
	public static <T> SelectionScreen<T> openSelectionScreen(@Nullable Text title, OptionNode<T> tree, Consumer<T> callback){
		return openSelectionScreen(title, tree, MinecraftClient.getInstance().currentScreen, callback);
	}
	
	@SuppressWarnings("UnusedReturnValue")
	public static <T> SelectionScreen<T> openSelectionScreen(OptionNode<T> tree, Consumer<T> callback){
		return openSelectionScreen(null, tree, MinecraftClient.getInstance().currentScreen, callback);
	}
	
	public static <T> SelectionScreen<T> openSelectionScreen(@Nullable Text title, Map<String, T> sources, Map<String, ?> tree, Consumer<T> callback){
		return openSelectionScreen(title, generateOptionNodeFromMap(sources, tree, title == null ? Text.of("") : title), callback);
	}
	
	@SuppressWarnings("unused")
	public static <T> SelectionScreen<T> openSelectionScreen(Map<String, T> sources, Map<String, ?> tree, Consumer<T> callback){
		return openSelectionScreen(null, sources, tree, callback);
	}
	
	private SelectionScreen(@Nullable Text title, @Nullable Screen parent, Consumer<T> callback){
		setParent(parent);
		setTitle(title == null ? "" : title.getString());
		this.callback = callback;
	}
	
	@Override public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		var client = MinecraftClient.getInstance();
		double dMouseX = client.mouse.getX();
		double dMouseY = client.mouse.getY();
		tickScrollBarInfo(scrollBarInfo, millisTimer);
		var parent = getParent();
		if(parent != null) parent.render(context, -1, -1, deltaTicks);
		super.render(context, mouseX, mouseY, deltaTicks);
		double position = ScrollBarInfo.getPosition(scrollBarInfo);
		int startIndex = getListIndexAtX(position, 0), endIndex = getListIndexAtX(position + getScreenWidth(), optionList.size() - 1);
		var matrices = context.getMatrices();
		matrices.push();
		matrices.translate((float) -position, 0, 0);
		int dividerBottom = scrollBarInfo == null ? getScreenHeight() : getScreenHeight() - (scrollBarThickness + 2);
		for(int i = startIndex; i <= endIndex; ++i){
			var list = optionList.get(i);
			// 绘制分割线
			if(i != 0) context.fill(list.x - 1, titleHeight, list.x, dividerBottom,
				getDividerColor(i == selectedList || i == selectedList + 1));
			matrices.push();
			matrices.translate(list.x, titleHeight, 0);
			context.enableScissor(0, 0, list.getScreenWidth(), list.getScreenHeight());
			list.renderEx(context, dMouseX + position - list.x, dMouseY - titleHeight);
			context.disableScissor();
			matrices.pop();
		}
		matrices.pop();
		// 绘制横向滚动条
		if(scrollBarInfo != null) {
			boolean highlighted = (selectedList < 0 || selectedList >= optionList.size()) && isHoldingScrollBar;
			// 绘制滚动条背景
			context.fill(0, getScreenHeight() - 1 - scrollBarThickness, getScreenWidth(), getScreenHeight() - 1, getScrollBarBackgroundColor(highlighted));
			// 绘制滚动条
			double k = (double)getScreenWidth() / scrollBarInfo.contentSize;
			double scrollBarLeft = scrollBarInfo.currentPosition * k;
			double scrollBarWidth = getScreenWidth() * k;
			// 使用translate+scale，避免因为四舍五入导致的锯齿
			matrices.push();
			matrices.translate((float) scrollBarLeft, getScreenHeight() - 1 - scrollBarThickness, 0);
			matrices.scale((float) scrollBarWidth, 1.0f, 1.0f);
			context.fill(0, 0, 1, scrollBarThickness, getScrollBarColor(highlighted));
			matrices.pop();
		}
	}
	
	@Override public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		if(scrollBarInfo != null){
			if(getScreenHeight() - 1 - scrollBarThickness <= mouseY && mouseY < getScreenHeight() - 1){
				// 点击了横向滚动条
				isHoldingScrollBar = true;
				selectedList = -1;
				scrollBarInfo.prepareClicked(mouseX / getScreenWidth());
				return true;
			}
		}
		double translatedX = mouseX + ScrollBarInfo.getPosition(scrollBarInfo);
		if(mouseY >= titleHeight){
			selectedList = getListIndexAtX(translatedX);
			if(selectedList >= 0){
				var list = optionList.get(selectedList);
				if(list.scrollBarInfo != null){
					if(list.getRight() - 1 - scrollBarThickness <= translatedX && translatedX < list.getRight() - 1){
						// 点击了纵向滚动条
						isHoldingScrollBar = true;
						list.scrollBarInfo.prepareClicked((mouseY - titleHeight) / list.getScreenHeight());
						return true;
					}
				}
				if(list.mouseClicked(translatedX, mouseY, mouseButton))
					return true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	@Override public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
		if(isHoldingScrollBar){
			isHoldingScrollBar = false;
			return true;
		}
		return super.mouseReleased(mouseX, mouseY, mouseButton);
	}
	
	@Override public void mouseMoved(double mouseX, double mouseY) {
		if(isHoldingScrollBar){
			if(0 <= selectedList && selectedList < optionList.size()){
				var list = optionList.get(selectedList);
				if(list.scrollBarInfo != null) list.scrollBarInfo.updateHolding((mouseY - titleHeight) / list.getScreenHeight());
			}
			else if(scrollBarInfo != null) scrollBarInfo.updateHolding(mouseX / getScreenWidth());
			return;
		}
		super.mouseMoved(mouseX, mouseY);
	}
	
	@Override public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if(super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) return true;
		int index = mouseY >= titleHeight ? getListIndexAtX(mouseX + ScrollBarInfo.getPosition(scrollBarInfo), selectedList) : selectedList;
		if(index >= 0 && index < optionList.size()){
			var bar = optionList.get(index).scrollBarInfo;
			if(bar != null) {
				bar.targetPosition -= verticalAmount * verticalScrollSpeed.getDoubleValue() * buttonStride;
				if (isHoldingScrollBar) bar.instant();
			}
		}
		if(scrollBarInfo != null){
			scrollBarInfo.targetPosition += horizontalAmount * horizontalScrollSpeed.getDoubleValue() * buttonStride;
			if(isHoldingScrollBar) scrollBarInfo.instant();
		}
		return false;
	}
	
	private void setOptionList(int index, Iterable<? extends IOption<T>> options){
		if(index > optionList.size()) throw new IndexOutOfBoundsException(index);
		while(index < optionList.size()) optionList.removeLast();
		var list = new OptionList(options);
		optionList.add(list);
		selectedList = index;
		scrollBarInfo = ScrollBarInfo.recreate(scrollBarInfo, list.getRight(), getScreenWidth());
		if(scrollBarInfo != null){
			if(scrollBarInfo.currentPosition < list.getRight() - list.width)
				scrollBarInfo.currentPosition = list.getRight() - list.width;
			if(scrollBarInfo.currentPosition > list.x)
				scrollBarInfo.currentPosition = list.x;
			scrollBarInfo.instant();
		}
		for(var v : optionList) v.updateData();
	}
	
	private void applyCallback(T val){
		callback.accept(val);
	}
	
	// 传入参数应该是变换后的x，也就是说假设调用时第一个列表的起始坐标在(0, titleHeight)
	private int getListIndexAtX(double x, int defVal){
		if(optionList.isEmpty() || x < 0 || x >= optionList.getLast().getRight()) return defVal;
		int left = 0, right = optionList.size();
		while(left + 1 < right){
			int mid = (left + right) / 2;
			if(optionList.get(mid).x <= x) left = mid;
			else right = mid;
		}
		var list = optionList.get(left);
		if(list.x <= x && x < list.getRight()) return left;
		else return defVal;
	}
	
	private int getListIndexAtX(double x) { return getListIndexAtX(x, -1); }
	
	private class OptionList extends GuiBase{
		final OptionButton[] optionButtons;
		final int index, maxButtonWidth;
		final MillisTimer millisTimer = new MillisTimer();
		int x = 0;
		int selectedIndex = -1;
		ScrollBarInfo scrollBarInfo = null;
		OptionList(Iterable<? extends IOption<T>> options){
			index = optionList.size();
			var tempList = new ArrayList<OptionButton>();
			int maxButtonWidth = 0;
			int y = 0;
			// 按钮和左边间隔2，按钮之间间隔2，按钮到滚动条间隔2，滚动条到右侧间隔1
			for(var option : options){
				var button = new OptionButton(2, y + 1, buttonHeight, option, index, SelectionScreen.this);
				tempList.add(button);
				maxButtonWidth = Math.max(maxButtonWidth, button.getWidth());
				y += buttonStride;
			}
			this.maxButtonWidth = maxButtonWidth;
			optionButtons = tempList.toArray(new OptionButton[0]);
			updateData();
		}
		
		int getRight(){ return x + getScreenWidth(); }
		
		public void renderEx(DrawContext context, double mouseX, double mouseY) {
			tickScrollBarInfo(scrollBarInfo, millisTimer);
			boolean highlighted = index == selectedList;
			var matrices = context.getMatrices();
			if(highlighted) context.fill(0, 0, getScreenWidth(), getScreenHeight(), backgroundHighlightColor.getIntegerValue());
			if(scrollBarInfo != null){
				// 绘制滚动条背景
				context.fill(getScreenWidth() - 1 - scrollBarThickness, 0, getScreenWidth() - 1, getScreenHeight(), getScrollBarBackgroundColor(highlighted));
				// 绘制滚动条
				double k = (double)getScreenHeight() / scrollBarInfo.contentSize;
				double scrollBarTop = scrollBarInfo.currentPosition * k;
				double scrollBarHeight = getScreenHeight() * k;
				// 使用translate+scale，避免因为四舍五入导致的锯齿
				matrices.push();
				matrices.translate(getScreenWidth() - 1 - scrollBarThickness, (float) scrollBarTop, 0);
				matrices.scale(1.0f, (float) scrollBarHeight, 1.0f);
				context.fill(0, 0, scrollBarThickness, 1, getScrollBarColor(highlighted));
				matrices.pop();
			}
			double position = ScrollBarInfo.getPosition(scrollBarInfo);
			matrices.translate(0.0f, (float) -position, 0.0f);
			double translatedMouseY = mouseY + position;
			int startIndex = (int) Math.floor(position / buttonStride);
			int endIndex = Math.min((int) Math.ceil((position + getScreenHeight()) / buttonStride), optionButtons.length);
			boolean canHighlight = mouseY >= 0 && mouseY < getScreenHeight();
			for(int i = Math.max(startIndex, 0); i < endIndex && i < optionButtons.length; ++i){
				var button = optionButtons[i];
				boolean highlight = i == selectedIndex || (canHighlight && optionButtons[i].isMouseOver((int) mouseX, (int) translatedMouseY));
				int tx, ty;
				if(highlight) {
					tx = button.getX() + button.getWidth() / 2;
					ty = button.getY() + button.getHeight() / 2;
				}
				else {
					tx = (int) mouseX;
					ty = (int) translatedMouseY;
				}
				button.render(tx, ty, highlight, context);
			}
		}
		
		@Override public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
			int translatedMouseX = (int) (mouseX - x), translatedMouseY = (int) (mouseY - titleHeight + ScrollBarInfo.getPosition(scrollBarInfo));
			int index = Math.floorDiv(translatedMouseY, buttonStride);
			if(index >= 0 && index < optionButtons.length){
				var button = optionButtons[index];
				if(button.onMouseClicked(translatedMouseX, translatedMouseY, mouseButton)) {
					selectedIndex = index;
					return true;
				}
			}
			return super.mouseClicked(mouseX, mouseY, mouseButton);
		}
		
		void updateData(){
			x = index == 0 ? 0 : optionList.get(index - 1).getRight() + 1;
			int extraWidth = scrollBarInfo == null ? 4 : scrollBarThickness + 5;
			int extraHeight = SelectionScreen.this.scrollBarInfo == null ? 0 : -(scrollBarThickness + 2);
			resize(mc, maxButtonWidth + extraWidth, SelectionScreen.this.getScreenHeight() - titleHeight + extraHeight);
			scrollBarInfo = ScrollBarInfo.recreate(scrollBarInfo, optionButtons.length * buttonStride, getScreenHeight());
		}
	}
	
	private static class OptionButton extends ButtonGeneric {
		public <T> OptionButton(int x, int y, int height, IOption<T> option, int yourDepth, SelectionScreen<T> screen) {
			super(x, y, 20, height, option.getName().getString());
			setWidth(calculateTextButtonWidth(displayString, textRenderer, buttonHeight));
			if(option.getComment() instanceof Text comment) setHoverStrings(comment.getString());
			setActionListener((button, mouseButton)->option.onSelected(yourDepth, screen));
		}
	}
	
	private static Object finalizeSupplier(Object o){
		if(o instanceof Supplier<?> s) return finalizeSupplier(s.get());
		else return o;
	}
	
	private static <T> OptionNode<T> generateOptionNodeFromMap(Map<String, T> sources, Map<?, ?> tree, @NotNull Text name){
		return new OptionNode<>(()->{
			var res = new ArrayList<IOption<T>>();
			tree.forEach((o1, o2)->{
				Text nextName = switch (finalizeSupplier(o1)) {
					case String s -> Text.of(s);
					case Text t -> t;
					default -> null;
				};
				if(nextName == null) return;
				IOption<T> next = switch (finalizeSupplier(o2)){
					case Map<?, ?> m -> generateOptionNodeFromMap(sources, m, nextName);
					case String s -> new OptionLeaf<>(sources.get(s), nextName);
					default -> null;
				};
				if(next == null) return;
				res.add(next);
			});
			return res;
		}, name);
	}
}
