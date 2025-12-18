package lpctools.lpcfymasaapi.screen;

import fi.dy.masa.malilib.gui.GuiBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//先前的ChooseScreen感觉不够好用，重写一个
//TODO
public class NewChooseScreen extends GuiBase {
	public interface IOption {
		@NotNull String getName();
		@Nullable String getComment();
		void onSelected(NewChooseScreen screen);
	}
	
}
