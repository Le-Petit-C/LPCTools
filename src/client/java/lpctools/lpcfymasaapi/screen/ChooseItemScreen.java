package lpctools.lpcfymasaapi.screen;

import fi.dy.masa.litematica.util.ToBooleanFunction;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class ChooseItemScreen extends GuiBase {
	public final int width;
	public final int height;
	public static final int w = 22, h = 22;
	private final ChooseItemData<?> data;
	private record SearchResult(Item renderItem, List<String> hoverStrings, Runnable chosenCallback){}
	private final ArrayList<SearchResult> searchedItems = new ArrayList<>();
	private record ChooseItemData<T>(List<T> items, Function<T, Item> toItem, Function<T, Identifier> toIdentifier, Function<T, String> toDisplayString, Consumer<T> callback) {
		public void refreshSearchedItems(String text, ArrayList<SearchResult> searchedItems){
			ToBooleanFunction<Identifier> idTest;
			Identifier testId = Identifier.tryParse(text);
			if(testId != null) idTest = obj->obj.getNamespace().contains(testId.getNamespace())
				&& obj.getPath().contains(testId.getPath());
			else idTest = o->false;
			searchedItems.clear();
			for(T obj : items){
				Item item = toItem.apply(obj);
				Identifier id = toIdentifier.apply(obj);
				String displayString = toDisplayString.apply(obj);
				if(displayString.contains(text) || idTest.applyAsBoolean(id))
					searchedItems.add(new SearchResult(item, List.of(displayString, id.toString()), ()->callback.accept(obj)));
			}
		}
	}
	private int shift = 0;
	private boolean needInitGui = true;
	GuiTextFieldGeneric searchBar;
	public <T> ChooseItemScreen(@Nullable Screen parent, List<T> items, Function<T, Item> toItem, Function<T, Identifier> toIdentifier, Function<T, String> toDisplayString, int width, int height, Consumer<T> callback){
		this.width = width;
		this.height = height;
		this.data = new ChooseItemData<>(items, toItem, toIdentifier, toDisplayString, callback);
		setParent(parent);
		data.refreshSearchedItems("", searchedItems);
	}
	public static ChooseItemScreen ofItems(@Nullable Screen parent, List<Item> items, int width, int height, Consumer<Item> callback){
		return new ChooseItemScreen(parent, items, item->item, Registries.ITEM::getId, item->item.getName().getString(), width, height, callback);
	}
	public static ChooseItemScreen ofBlocks(@Nullable Screen parent, List<Block> items, int width, int height, Consumer<Block> callback){
		return new ChooseItemScreen(parent, items, Block::asItem, Registries.BLOCK::getId, block->block.getName().getString(), width, height, callback);
	}
	@SuppressWarnings("unused")
	public static ChooseItemScreen ofAllItems(@Nullable Screen parent, int width, int height, Consumer<Item> callback){
		return ofItems(parent, Registries.ITEM.stream().toList(), width, height, callback);
	}
	public static ChooseItemScreen ofAllBlocks(@Nullable Screen parent, int width, int height, Consumer<Block> callback){
		return ofBlocks(parent, Registries.BLOCK.stream().toList(), width, height, callback);
	}
	public static ChooseItemScreen ofAllBlocks(int width, int height, Consumer<Block> callback){
		return ofAllBlocks(MinecraftClient.getInstance().currentScreen, width, height, callback);
	}
	@Override public void initGui() {
		super.initGui();
		int y0 = (getScreenHeight() - h * (height - 1)) / 2;
		int x0 = (getScreenWidth() - w * (width - 1)) / 2;
		if(searchBar == null) searchBar = new GuiTextFieldGeneric(0, 0, w * width, 15, textRenderer);
		searchBar.setPosition(x0 - w / 2, y0 - h / 2 - 16);
		addTextField(searchBar, textField -> {
			data.refreshSearchedItems(textField.getText(), searchedItems);
			shift = 0;
			needInitGui = true;
			return true;
		});
		int y = y0;
		break1:
		for(int a = 0; a < height; ++a){
			int x = x0;
			for(int b = 0; b < width; ++b){
				int index = (a + shift) * width + b;
				if(index >= searchedItems.size()) break break1;
				SearchResult result = searchedItems.get(index);
				addButton(new ItemButton(result.renderItem, x, y, result.hoverStrings), (button, mouseButton)->{
					result.chosenCallback.run();
					closeGui(true);
				});
				x += w;
			}
			y += h;
		}
	}
	@Override public boolean onMouseScrolled(int mouseX, int mouseY, double horizontalAmount, double verticalAmount) {
		int lastShift = shift;
		shift -= (int)Math.signum(verticalAmount);
		int mh = ((searchedItems.size() + width - 1) / width) - height;
		if(shift > mh) shift = mh;
		if(shift < 0) shift = 0;
		if(shift != lastShift) needInitGui = true;
		return true;
	}
	@Override public void render(DrawContext drawContext, int mouseX, int mouseY, float partialTicks) {
		if(needInitGui){
			initGui();
			needInitGui = false;
		}
		Screen parent = getParent();
		if(parent != null) parent.render(drawContext, 0, 0, partialTicks);
		super.render(drawContext, mouseX, mouseY, partialTicks);
	}
}
