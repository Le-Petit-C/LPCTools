package lpctools.tools.signFractionHelper;

import lpctools.lpcfymasaapi.Registries;
import lpctools.mixin.client.accessors.AbstractSignEditScreenAccessor;
import lpctools.tools.ToolUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;

import java.util.Objects;

import static lpctools.tools.signFractionHelper.SignFractionHelper.*;

public class SignFractionHelperRunner implements Registries.ScreenChangedCallback, ToolUtils.ToolRunner {
	@Override public void registerAll(boolean b) {
		Registries.ON_SCREEN_CHANGED.register(this, b);
	}

	@Override public void onScreenChanged(Screen newScreen) {
		if(!(newScreen instanceof AbstractSignEditScreen signScreen)) return;
		AbstractSignEditScreenAccessor accessor = (AbstractSignEditScreenAccessor) signScreen;
		if(accessor.lpctools$isFrontText()) {
			String value = selected.getFraction().toString();
			String oldValue = accessor.lpctools$messages()[0];
			if(!Objects.equals(oldValue, value)) {
				accessor.lpctools$messages()[0] = value;
				Minecraft.getInstance().gui.setScreen(null);
				overlayText("stamped", value);
				return;
			}
		}
		if(alwaysCloseScreen.getBooleanValue())
			Minecraft.getInstance().gui.setScreen(null);
	}
}
