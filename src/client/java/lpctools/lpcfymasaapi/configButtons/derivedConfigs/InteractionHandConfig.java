package lpctools.lpcfymasaapi.configButtons.derivedConfigs;

import lpctools.lpcfymasaapi.configButtons.transferredConfigs.BooleanConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import net.minecraft.world.InteractionHand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InteractionHandConfig extends BooleanConfig {
	private static final String NAME_KEY = "interactionHand";
	public InteractionHandConfig(@NotNull ILPCConfigReadable parent, boolean defaultBoolean) { super(parent, NAME_KEY, defaultBoolean); }
	public InteractionHandConfig(@NotNull ILPCConfigReadable parent, boolean defaultBoolean, @Nullable ILPCValueChangeCallback callback) { super(parent, NAME_KEY, defaultBoolean, callback); }
	public int offhandPriority() { return getBooleanValue() ? -1 : 0; }
	public InteractionHand getHand() { return getBooleanValue() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND; }
	@Override public @NotNull String getFullTranslationKey() { return DerivedConfigUtils.fullKeyFromUtilBase(this); }
}
