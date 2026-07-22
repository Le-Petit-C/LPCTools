package lpctools.mixinData;

import net.minecraft.world.inventory.AbstractContainerMenu;

public class AbstractContainerMenuExtraData {
	public final AbstractContainerMenu menu;
	protected int hotbarStartIndex = -1;

	public AbstractContainerMenuExtraData(AbstractContainerMenu menu) { this.menu = menu; }

	@SuppressWarnings("unused") public boolean hasHotbar() { return hotbarStartIndex != -1; }
	@SuppressWarnings("unused") public int getHotbarStartIndex() { return hotbarStartIndex; }
	@SuppressWarnings("unused") public int getHotbarStartIndexOrDefault(int defaultValue) { return hotbarStartIndex == -1 ? defaultValue : hotbarStartIndex; }
	public int getHotbarStartIndexOrDefault() {
		return hotbarStartIndex == -1 ? menu.slots.size() - (menu.containerId == 0 ? 9 : 10) : hotbarStartIndex;
	}

	public static class Mutable extends AbstractContainerMenuExtraData {
		public Mutable(AbstractContainerMenu menu) { super(menu); }

		public void setHotbarStartIndex(int index) { hotbarStartIndex = index; }
	}
	public interface Getter {
		AbstractContainerMenuExtraData lpctools$getAbstractContainerMenuExtraData();
	}
}
