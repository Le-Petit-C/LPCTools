package lpctools.lpcfymasaapi.screen;

import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.ArrayList;

import static lpctools.lpcfymasaapi.LPCConfigUtils.calculateAndAdjustDisplayLength;

public class ChooseScreen extends GuiBase {
	public static ChooseScreen openChooseScreen(Screen parent, String title, boolean hasCancelButton){
		ChooseScreen screen = new ChooseScreen(parent, title, hasCancelButton);
		MinecraftClient.getInstance().setScreen(screen);
		return screen;
	}
	public static ChooseScreen openChooseScreen(String title, boolean hasCancelButton){
		return openChooseScreen(MinecraftClient.getInstance().currentScreen, title, hasCancelButton);
	}
	public record ChooseScreenOption(String buttonText, IButtonActionListener listener){}
	private static final int buttonHeight = 20;
	private static final int buttonHeightStride = 22;
	private static final int reservedDistance = 20;
	private int startY, minY, maxY;
	private final boolean hasCancelButton;
	public final ArrayList<ChooseScreenOption> options = new ArrayList<>();
	ChooseScreen(Screen parent, String title, boolean hasCancelButton) {
		setParent(parent);
		setTitle(title);
		resetY();
		this.hasCancelButton = hasCancelButton;
	}
	public void add(String buttonText, IButtonActionListener listener){
		options.add(new ChooseScreenOption(buttonText, listener));
		resetY();
	}
	public void add(String buttonText, Runnable runnable){
		options.add(new ChooseScreenOption(buttonText, (button, mouseButton)->runnable.run()));
		resetY();
	}
	private void resetY(){
		int size = options.size() + (hasCancelButton ? 1 : 0);
		minY = (getScreenHeight() - buttonHeightStride * size) / 2;
		maxY = (getScreenHeight() - buttonHeightStride * size) / 2;
		minY = Math.min(minY, getScreenHeight() - reservedDistance - buttonHeightStride * size);
		maxY = Math.max(maxY, reservedDistance);
		startY = maxY;
	}
	@Override public void initGui() {
		super.initGui();
		int x = getScreenWidth() / 2;
		MutableInt y = new MutableInt(startY);
		options.forEach(option->addButton(allocateCenterAt(x, y.getAndAdd(buttonHeightStride), option.buttonText),
			(button, mouse) -> {option.listener.actionPerformedWithButton(button, mouse); closeGui(true);}));
		if(hasCancelButton) addButton(allocateCenterAt(x, y.intValue(), Text.translatable(cancelKey).getString()), (button, mouse) -> closeGui(true));
	}
	
	@Override public boolean onMouseScrolled(int mouseX, int mouseY, double horizontalAmount, double verticalAmount) {
		startY += (int) Math.round(verticalAmount * buttonHeightStride);
		startY = Math.clamp(startY, minY, maxY);
		initGui();
		return true;
	}
	
	private static ButtonGeneric allocateCenterAt(int centerX, int centerY, String text) {
		int w = calculateAndAdjustDisplayLength(text);
		return new ButtonGeneric(centerX - w / 2, centerY - buttonHeight / 2, w, buttonHeight, text);
	}
	@Override public void render(DrawContext drawContext, int mouseX, int mouseY, float partialTicks) {
		if (this.getParent() != null) this.getParent().render(drawContext, mouseX, mouseY, partialTicks);
		super.render(drawContext, mouseX, mouseY, partialTicks);
	}
	private static final String cancelKey = "lpcfymasaapi.configs.mutableConfig.cancel";
}
