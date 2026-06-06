package lpctools.tools.bedrockKiller;

import lpctools.lpcfymasaapi.Registries;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.BooleanConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.BooleanHotkeyThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import lpctools.mixin.client.accessors.MinecraftAccessor;
import lpctools.tools.ToolConfigs;
import lpctools.tools.ToolUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.NonNull;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;

public class BedrockKiller {
	public static final BooleanHotkeyThirdListConfig BKConfig = new BooleanHotkeyThirdListConfig(ToolConfigs.toolConfigs, "BK", BedrockKillerEvents.INSTANCE);
	static { ToolUtils.setLPCToolsToggleText(BKConfig); }
	static { listStack.push(BKConfig); }
	public static final BooleanConfig autoOperateWhenLookingRedstoneTorch = addBooleanConfig("autoOperateWhenLookingRedstoneTorch", false, BedrockKillerEvents.INSTANCE);
	static { listStack.pop(); }
	
	private static boolean operating = false;
	
	public static void operate() {
		if(!BKConfig.getBooleanValue() || operating) return;
		operating = true;
		try{
			Minecraft mc = Minecraft.getInstance();
			LocalPlayer player = mc.player;
			Entity camera = mc.getCameraEntity();
			if(player == null || camera == null) return;
			MinecraftAccessor accessor = (MinecraftAccessor)mc;
			mc.hitResult = player.raycastHitResult(0, camera);
			accessor.invokeStartAttack();
			accessor.invokeStartUseItem();
		} finally {
			operating = false;
		}
	}
	
	private static class BedrockKillerEvents implements ClientTickEvents.EndTick, ILPCValueChangeCallback {
		static final BedrockKillerEvents INSTANCE = new BedrockKillerEvents();
		@Override public void onEndTick(@NonNull Minecraft mc) {
			ClientLevel level = mc.level;
			HitResult hitResult = mc.hitResult;
			if(level != null && hitResult instanceof BlockHitResult blockHitResult
					&& level.getBlockState(blockHitResult.getBlockPos()).getBlock() == Blocks.REDSTONE_TORCH) {
				MinecraftAccessor accessor = (MinecraftAccessor)mc;
				accessor.invokeStartAttack();
			}
		}

		@Override public void onValueChanged() {
			Registries.END_CLIENT_TICK.register(this, BKConfig.getBooleanValue() && autoOperateWhenLookingRedstoneTorch.getBooleanValue());
		}
	}
}
