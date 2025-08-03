package lpctools.lpcfymasaapi.screen;

import com.google.common.collect.ImmutableMap;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static lpctools.lpcfymasaapi.LPCConfigUtils.calculateTextButtonWidth;

public class ChooseScreen extends GuiBase {
	public static <T> ChooseScreen openChooseScreen(Screen parent, String title, boolean hasCancelButton, boolean hasSearchBar, Map<String, ? extends OptionCallback<? super T>> options, Map<?, ?> chooseTree, T userData){
		ChooseScreen screen = new ChooseScreen(parent, null, title, hasCancelButton, hasSearchBar, options, chooseTree, userData);
		MinecraftClient.getInstance().setScreen(screen);
		screen.resetY();
		screen.initGui();
		return screen;
	}
	public static <T> ChooseScreen openChooseScreen(Screen parent, String title, boolean hasCancelButton, Map<String, ? extends OptionCallback<? super T>> options, Map<?, ?> chooseTree, T userData){
		return openChooseScreen(parent, title, hasCancelButton, false, options, chooseTree, userData);
	}
	@SuppressWarnings("UnusedReturnValue")
	public static <T> ChooseScreen openChooseScreen(String title, boolean hasCancelButton, Map<String, ? extends OptionCallback<? super T>> options, Map<?, ?> chooseTree, T userData){
		return openChooseScreen(MinecraftClient.getInstance().currentScreen, title, hasCancelButton, options, chooseTree, userData);
	}
	@SuppressWarnings("UnusedReturnValue")
	public static <T> ChooseScreen openChooseScreen(String title, boolean hasCancelButton, boolean hasSearchBar, Map<String, ? extends OptionCallback<? super T>> options, Map<?, ?> chooseTree, T userData){
		return openChooseScreen(MinecraftClient.getInstance().currentScreen, title, hasCancelButton, hasSearchBar, options, chooseTree, userData);
	}
	private static final int buttonHeight = 20;
	private static final int buttonHeightStride = 22;
	private static final int reservedDistance = 53;
	private int startY, minY, maxY;
	@NotNull private String searchText = "";
	@NotNull private WrappedOptionTree<?> searchedOptions;
	private final boolean hasCancelButton;
	private final boolean hasSearchBar;
	private final ChooseScreen chooseParent;
	private final WrappedOptionTree<?> options;
	private record WrappedOptionTree<T>(Map<String, ? extends OptionCallback<? super T>> options, Map<?, ?> chooseTree, T userData){
		public int optionCount(){return chooseTree.size();}
		public WrappedOptionTree<T> search(String searchText){
			ImmutableMap.Builder<Object, Object> chooseTreeBuilder = ImmutableMap.builder();
			chooseTree.forEach((key, object)->{
				String str = key.toString();
				if(str.contains(searchText) || Text.translatable(str).getString().contains(searchText))
					chooseTreeBuilder.put(key, object);
			});
			return new WrappedOptionTree<>(options, chooseTreeBuilder.build(), userData);
		}
		public void buildButtons(ChooseScreen screen, TextRenderer textRenderer){
			int x = screen.width / 2;
			MutableInt y = new MutableInt(screen.startY);
			chooseTree.forEach((key, object)->{
					if(key instanceof String text){
						screen.addButton(allocateCenterAt(x, y.getAndAdd(buttonHeightStride), Text.translatable(text).getString(), textRenderer), (button, mouse)->{
							if(object instanceof String optionKey){
								options.get(optionKey).action(button, mouse, userData);
								screen.closeGui(true);
							}
							else if(object instanceof Map<?, ?> map){
								ChooseScreen screen1 = new ChooseScreen(screen.getParent(), screen, screen.title, screen.hasCancelButton, screen.hasSearchBar, options, map, userData);
								MinecraftClient.getInstance().setScreen(screen1);
								screen1.resetY();
								screen1.initGui();
							}
						});
					}
				}
			);
			if(screen.hasCancelButton) screen.addButton(allocateCenterAt(x, y.intValue(), Text.translatable(cancelKey).getString(), textRenderer),
				(button, mouse) -> {
				if(screen.chooseParent == null) screen.closeGui(true);
				else MinecraftClient.getInstance().setScreen(screen.chooseParent);
			});
		}
	}
	public interface OptionCallback<T>{ void action(ButtonBase button, int mouseButton, T userData);}
	private <T> ChooseScreen(Screen parent, ChooseScreen chooseParent, String title, boolean hasCancelButton, boolean hasSearchBar, Map<String, ? extends OptionCallback<? super T>> options, Map<?, ?> chooseTree, T userData) {
		setParent(parent);
		setTitle(title);
		this.hasCancelButton = hasCancelButton;
		this.hasSearchBar = hasSearchBar;
		this.chooseParent = chooseParent;
		searchedOptions = this.options = new WrappedOptionTree<>(options, chooseTree, userData);
	}
	private void resetY(){
		int size = searchedOptions.optionCount() + (hasCancelButton ? 1 : 0);
		minY = maxY = (super.width - buttonHeightStride * size) / 2;
		minY = Math.min(minY, super.height - reservedDistance - buttonHeightStride * size);
		maxY = Math.max(maxY, reservedDistance);
		startY = maxY;
	}
	@Override public void initGui() {
		super.initGui();
		searchedOptions.buildButtons(this, textRenderer);
		if(hasSearchBar){
			int w = (int)Math.round(super.width * 0.5);
			int h = 14;
			int x = (int)Math.round((super.width - w) * 0.5);
			int y = 26;
			GuiTextFieldGeneric textField = new GuiTextFieldGeneric(x, y, w, h, textRenderer);
			textField.setText(searchText);
			addTextField(textField, (text)->{
				searchText = text.getText();
				searchedOptions = options.search(searchText);
				resetY();
				clearButtons();
				searchedOptions.buildButtons(this, textRenderer);
				return false;
			});
		}
	}
	
	@Override public boolean onMouseScrolled(int mouseX, int mouseY, double mouseWheelDelta) {
		startY += (int) Math.round(mouseWheelDelta * buttonHeightStride);
		startY = Math.clamp(startY, minY, maxY);
		initGui();
		return true;
	}
	
	private static ButtonGeneric allocateCenterAt(int centerX, int centerY, String text, TextRenderer textRenderer) {
		int w = calculateTextButtonWidth(text, textRenderer, 20);
		return new ButtonGeneric(centerX - w / 2, centerY - buttonHeight / 2, w, buttonHeight, text);
	}
	private void renderChooseParents(DrawContext drawContext, int mouseX, int mouseY, float partialTicks){
		if(chooseParent != null) chooseParent.renderChooseParents(drawContext, 0, 0, partialTicks);
		super.render(drawContext, mouseX, mouseY, partialTicks);
	}
	@Override public void render(DrawContext drawContext, int mouseX, int mouseY, float partialTicks) {
		if (this.getParent() != null) this.getParent().render(drawContext, 0, 0, partialTicks);
		renderChooseParents(drawContext, mouseX, mouseY, partialTicks);
	}
	private static final String cancelKey = "lpcfymasaapi.screen.chooseScreen.cancel";
}
