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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static lpctools.lpcfymasaapi.LPCConfigUtils.calculateTextButtonWidth;

//先前的ChooseScreen感觉不够好用，重写一个
//TODO
public class NewChooseScreen<T> extends GuiBase {
	public interface IOption<T> {
		@NotNull Text getName();
		@Nullable Text getComment();
		void onSelected(int yourDepth, NewChooseScreen<T> screen);
	}
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
		@Override public void onSelected(int yourDepth, NewChooseScreen<T> screen) {
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
		@Override public void onSelected(int yourDepth, NewChooseScreen<T> screen) {
			screen.applyCallback(callbackVal);
			screen.closeGui(true);
		}
	}
	
	private static final int buttonHeight = 20;
	private static final int buttonStride = 22;
	private static final int titleHeight = 53;
	private static final int scrollBarWidth = 2;
	
	private final ArrayList<OptionList> optionList = new ArrayList<>();
	private final Consumer<T> callback;
	private int selectedList = 0;
	
	public static <T> NewChooseScreen<T> openChooseScreen(OptionNode<T> tree, @Nullable Screen parent, Consumer<T> callback){
		var res = new NewChooseScreen<>(parent, callback);
		var mc = MinecraftClient.getInstance();
		if(mc.currentScreen == parent) mc.currentScreen = null;
		mc.setScreen(res);
		res.setOptionList(0, tree.subOptionSupplier.get());
		return res;
	}
	
	public static <T> NewChooseScreen<T> openChooseScreen(OptionNode<T> tree, Consumer<T> callback){
		return openChooseScreen(tree, MinecraftClient.getInstance().currentScreen, callback);
	}
	
	private NewChooseScreen(@Nullable Screen parent, Consumer<T> callback){
		setParent(parent);
		this.callback = callback;
	}
	
	@Override public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		//TODO: 横向滚动条
		var parent = getParent();
		if(parent != null) parent.render(context, -1, -1, deltaTicks);
		super.render(context, mouseX, mouseY, deltaTicks);
		int startIndex = getListIndexAtX(0, 0), endIndex = getListIndexAtX(getScreenWidth(), optionList.size());
		for(int i = startIndex; i <= endIndex; ++i)
			optionList.get(i).render(context, mouseX, mouseY, deltaTicks);
	}
	
	@Override public boolean onMouseClicked(int mouseX, int mouseY, int mouseButton) {
		if(mouseY >= titleHeight){
			int index = getListIndexAtX(mouseX);
			if(index >= 0 && optionList.get(index).onMouseClicked(mouseX, mouseY, mouseButton))
				return true;
		}
		return super.onMouseClicked(mouseX, mouseY, mouseButton);
	}
	
	@Override public boolean onMouseScrolled(int mouseX, int mouseY, double horizontalAmount, double verticalAmount) {
		int index = mouseY >= titleHeight ? getListIndexAtX(mouseX, selectedList) : selectedList;
		if(index >= 0 && index < optionList.size()) optionList.get(index).scrollBarTargetPosition -= verticalAmount * 3 * buttonStride;
		return super.onMouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}
	
	private void setOptionList(int index, Iterable<? extends IOption<T>> options){
		if(index > optionList.size()) throw new IndexOutOfBoundsException(index);
		while(index < optionList.size()) optionList.removeLast();
		optionList.add(new OptionList(options));
		selectedList = index;
	}
	
	private void applyCallback(T val){
		callback.accept(val);
	}
	
	// 传入参数应该是变换后的x，也就是说假设调用时第一个列表的起始坐标在(0, titleHeight)
	private int getListIndexAtX(int x, int defVal){
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
	
	private int getListIndexAtX(int x) { return getListIndexAtX(x, -1); }
	
	private class OptionList extends GuiBase{
		static final double approachSpeed = 8 * 0.001; // 0.001是毫秒的乘数
		final OptionButton[] optionButtons;
		final int x, index, scrollBarHeight, scrollBarBottom;
		int selectedIndex = -1;
		long lastMillis = System.currentTimeMillis();
		double scrollBarTargetPosition = 0, scrollBarCurrentPosition = 0;
		OptionList(Iterable<? extends IOption<T>> options){
			index = optionList.size();
			if(optionList.isEmpty()) x = 0;
			else x = optionList.getLast().getRight() + 1;
			var tempList = new ArrayList<OptionButton>();
			int maxButtonWidth = 0;
			int y = 0;
			for(var option : options){
				var button = new OptionButton(1, y + 1, buttonHeight, option, index, NewChooseScreen.this);
				tempList.add(button);
				maxButtonWidth = Math.max(maxButtonWidth, button.getWidth());
				y += buttonStride;
			}
			scrollBarHeight = y;
			resize(MinecraftClient.getInstance(), maxButtonWidth + scrollBarWidth + 4, NewChooseScreen.this.getScreenHeight() - titleHeight);
			scrollBarBottom = scrollBarHeight - getScreenHeight();
			optionButtons = tempList.toArray(new OptionButton[0]);
		}
		
		int getRight(){ return x + getScreenWidth(); }
		
		@Override public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
			//TODO: 纵向滚动条
			long currentMillis = System.currentTimeMillis();
			scrollBarCurrentPosition = scrollBarTargetPosition + (scrollBarCurrentPosition - scrollBarTargetPosition) * Math.exp(-(currentMillis - lastMillis) * approachSpeed);
			lastMillis = currentMillis;
			if(scrollBarCurrentPosition > scrollBarBottom) scrollBarCurrentPosition = scrollBarTargetPosition = scrollBarBottom;
			if(scrollBarCurrentPosition < 0) scrollBarCurrentPosition = scrollBarTargetPosition = 0;
			// 绘制分割线
			if(index != 0){
				int color = index == selectedList || index == selectedList + 1 ? 0xffffffff : 0x7fffffff;
				context.fill(x - 1, titleHeight, x, NewChooseScreen.this.getScreenHeight(), color);
			}
			
			int roundedPosition = (int)Math.round(scrollBarCurrentPosition);
			context.getMatrices().pushMatrix().translate(x, titleHeight);
			context.enableScissor(0, 0, getScreenWidth(), getScreenHeight());
			context.getMatrices().translate(0, -roundedPosition);
			int translatedMouseX = mouseX - x, translatedMouseY = mouseY - titleHeight + roundedPosition;
			int startIndex = Math.floorDiv(roundedPosition, buttonStride);
			int endIndex = Math.ceilDiv(roundedPosition + getScreenHeight(), buttonStride);
			for(int i = Math.max(startIndex, 0); i < endIndex && i < optionButtons.length; ++i)
				optionButtons[i].render(context, translatedMouseX, translatedMouseY,
					i == selectedIndex || optionButtons[i].isMouseOver(translatedMouseX, translatedMouseY));
			context.disableScissor();
			context.getMatrices().popMatrix();
		}
		
		@Override public boolean onMouseClicked(int mouseX, int mouseY, int mouseButton) {
			// return super.onMouseClicked(x, mouseY, mouseButton);
			int translatedMouseX = mouseX - x, translatedMouseY = mouseY - titleHeight + (int)Math.round(scrollBarCurrentPosition);
			int index = Math.floorDiv(translatedMouseY, buttonStride);
			if(index >= 0 && index < optionButtons.length)
				return optionButtons[index].onMouseClicked(translatedMouseX, translatedMouseY, mouseButton);
			else return false;
		}
	}
	
	private static class OptionButton extends ButtonGeneric {
		public <T> OptionButton(int x, int y, int height, IOption<T> option, int yourDepth, NewChooseScreen<T> screen) {
			super(x, y, 20, height, option.getName().getString());
			setWidth(calculateTextButtonWidth(displayString, textRenderer, buttonHeight));
			if(option.getComment() instanceof Text comment) setHoverStrings(comment.getString());
			setActionListener((button, mouseButton)->option.onSelected(yourDepth, screen));
		}
	}
}
