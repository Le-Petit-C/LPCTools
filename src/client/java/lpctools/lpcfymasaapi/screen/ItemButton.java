package lpctools.lpcfymasaapi.screen;

import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.render.GuiContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.List;

public class ItemButton extends ButtonBase {
	private ItemStack stack;
	public Item item;
	public ItemButton(Item item, int x, int y, List<String> hoverStrings) {
		super(x - 8, y - 8, 16, 16);
		this.item = item;
		stack = new ItemStack(item);
		setHoverStrings(hoverStrings);
	}
	
	@Override public void render(GuiContext context, int mouseX, int mouseY, boolean selected) {
		super.render(context, mouseX, mouseY, selected);
		hovered = isMouseOver(mouseX, mouseY);
		if(hovered) context.fill(x, y, x + 16, y + 16, 0x3fffffff);
		context.drawItem(stack, getX(), getY());
	}
	
	public void setItem(Item item){
		this.item = item;
		stack = new ItemStack(item);
	}
}
