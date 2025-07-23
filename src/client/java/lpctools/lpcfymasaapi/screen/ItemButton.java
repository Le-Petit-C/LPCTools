package lpctools.lpcfymasaapi.screen;

import fi.dy.masa.malilib.gui.button.ButtonBase;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemButton extends ButtonBase {
	public final ItemStack stack;
	public ItemButton(Item item, int x, int y) {
		super(x - 10, y - 10, 20, 20);
		stack = new ItemStack(()->item);
	}
	
	@Override public void render(DrawContext drawContext, int mouseX, int mouseY, boolean selected) {
		super.render(drawContext, mouseX, mouseY, selected);
		drawContext.drawItem(stack, getX(), getY());
	}
}
