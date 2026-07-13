package lpctools.tweaks;

import lpctools.lpcfymasaapi.configButtons.transferredConfigs.BooleanHotkeyConfig;

public class PlayerCrosshairFilter {
	public static boolean isLocalPlayerGettingHitResult;
	public static final BooleanHotkeyConfig passThroughEntities = new BooleanHotkeyConfig(TweakConfigs.tweaks, "passThroughEntities", false, "");
	public static final BooleanHotkeyConfig passThroughBlocks = new BooleanHotkeyConfig(TweakConfigs.tweaks, "passThroughBlocks", false, "");
}
