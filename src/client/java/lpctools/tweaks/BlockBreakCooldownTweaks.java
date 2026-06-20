package lpctools.tweaks;

import lpctools.lpcfymasaapi.Registries;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.BooleanHotkeyConfig;
import lpctools.mixin.client.accessors.MultiPlayerGameModeAccessor;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;

public class BlockBreakCooldownTweaks {
	public static final BooleanHotkeyConfig extraBlockBreakCooldown = new BooleanHotkeyConfig(TweakConfigs.tweaks, "extraBlockBreakCooldown", false, null);
	public static final BooleanHotkeyConfig spareTimeDecreasesBlockBreakCooldown =
		new BooleanHotkeyConfig(TweakConfigs.tweaks, "spareTimeDecreasesBlockBreakCooldown", false, null,
			BlockBreakCooldownTweaks::spareTimeDecreasesBlockBreakCooldownCallback);
	public static final BooleanHotkeyConfig startBreakBlockResetsBlockBreakCooldown = new BooleanHotkeyConfig(TweakConfigs.tweaks, "startBreakBlockResetsBlockBreakCooldown", false, null);
	
	private static final SpareTimeDecreasesBlockBreakCooldownEvent
		spareTimeDecreasesBlockBreakCooldownEvent = new SpareTimeDecreasesBlockBreakCooldownEvent();
	
	private static void spareTimeDecreasesBlockBreakCooldownCallback() {
		Registries.END_CLIENT_TICK.register(spareTimeDecreasesBlockBreakCooldownEvent, spareTimeDecreasesBlockBreakCooldown.getAsBoolean());
	}
	
	private static class SpareTimeDecreasesBlockBreakCooldownEvent implements ClientTickEvents.EndTick {
		int lastCooldownTicks = 0;
		@Override public void onEndTick(Minecraft mc) {
			MultiPlayerGameMode gameMode = mc.gameMode;
			if(gameMode == null) return;
			MultiPlayerGameModeAccessor accessor = (MultiPlayerGameModeAccessor)gameMode;
			int thisCooldownTicks = accessor.getDestroyDelay();
			if(thisCooldownTicks != lastCooldownTicks) lastCooldownTicks = thisCooldownTicks;
			else if(lastCooldownTicks > 0) accessor.setDestroyDelay(--lastCooldownTicks);
		}
	}
}
