package lpctools.lpcfymasaapi.screen;

import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.render.GuiContext;
import java.util.List;

import lpctools.util.CachedSupplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemButton extends ButtonBase {
	public Item item;
	private final CachedSupplier<ItemStack> cachedItemStack = new CachedSupplier<>(()->{
		try {
			return item.getDefaultInstance();
		} catch (Exception e) {
			return ItemStack.EMPTY;
		}
	});
	public ItemButton(Item item, int x, int y, List<String> hoverStrings) {
		super(x - 8, y - 8, 16, 16);
		this.item = item;
		setHoverStrings(hoverStrings);
	}

	@Override public void render(GuiContext context, int mouseX, int mouseY, boolean selected) {
		super.render(context, mouseX, mouseY, selected);
		hovered = isMouseOver(mouseX, mouseY);
		if(hovered) context.fill(x, y, x + 16, y + 16, 0x3fffffff);
		context.renderItem(cachedItemStack.get(), getX(), getY());
	}

	public void setItem(Item item){
		this.item = item;
		cachedItemStack.invalidate();
	}
}
