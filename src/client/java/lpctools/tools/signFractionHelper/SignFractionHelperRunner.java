package lpctools.tools.signFractionHelper;

import lpctools.lpcfymasaapi.Registries;
import lpctools.mixin.client.accessors.AbstractSignEditScreenAccessor;
import lpctools.tools.ToolUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;

import static lpctools.tools.signFractionHelper.SignFractionHelper.*;

public class SignFractionHelperRunner implements Registries.ScreenChangedCallback, ToolUtils.ToolRunner {
	@Override public void registerAll(boolean b) {
		Registries.ON_SCREEN_CHANGED.register(this, b);
	}

	/**
	 * 界面切换完成之后再动手：把选定值写进界面自己的 {@code messages[0]}，然后立刻把界面关掉。
	 * <p>
	 * 关闭走 vanilla 的 {@code Gui.setScreen} → {@code AbstractSignEditScreen.removed()}：
	 * 告示牌界面会用它自己的 {@code messages} 和 {@code isFrontText} 把包发出去，
	 * 所以这里既不拼包、也不会和原版字段错位。
	 * <p>
	 * 因为 {@code ON_SCREEN_CHANGED} 挂在 {@code setScreen} 的 TAIL，关闭发生在同一次调用内部，
	 * 界面一帧都不会被渲染；{@code init()} 里开的文本输入也会被 {@code removed()} 正常关掉。
	 * <p>
	 * 只接管正面：背面原样打开界面，留给玩家自己写。
	 */
	@Override public void onScreenChanged(Screen newScreen) {
		if(!(newScreen instanceof AbstractSignEditScreen signScreen)) return;
		if(Minecraft.getInstance().gui.screen() != newScreen) return;   // 这次切换被别的工具取消了
		AbstractSignEditScreenAccessor accessor = (AbstractSignEditScreenAccessor) signScreen;
		// 只接管正面：背面由玩家自己控制
		if(!accessor.lpctools$isFrontText()) return;
		String value = selected.getFraction().toString();
		accessor.lpctools$messages()[0] = value;
		Minecraft.getInstance().gui.setScreen(null);
		overlayText("stamped", value);
	}
}
