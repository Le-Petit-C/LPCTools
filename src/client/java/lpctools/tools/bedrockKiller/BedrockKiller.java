package lpctools.tools.bedrockKiller;

import lpctools.lpcfymasaapi.Registries;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.BooleanConfig;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.IntegerConfig;
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
	public static final BooleanConfig torchAutoOperate = addBooleanConfig("torchAutoOperate", false, BedrockKillerEvents.INSTANCE);
	public static final IntegerConfig multiSamplingFactor = addIntegerConfig("multiSamplingFactor", 1, 1, 64);
	static { listStack.pop(); }
	
	private static boolean operating = false;
	
	public static void operate() {
		BedrockKillerEvents.INSTANCE.operate();
	}
	
	private static class BedrockKillerEvents implements ClientTickEvents.EndTick, ILPCValueChangeCallback {
		static final BedrockKillerEvents INSTANCE = new BedrockKillerEvents();

		float partialTicks = 1.0f;

		void operate() {
			if(!BKConfig.getBooleanValue() || operating) return;
			operating = true;
			try{
				Minecraft mc = Minecraft.getInstance();
				LocalPlayer player = mc.player;
				Entity camera = mc.getCameraEntity();
				if(player == null || camera == null) return;
				MinecraftAccessor accessor = (MinecraftAccessor)mc;
				mc.hitResult = player.raycastHitResult(partialTicks, camera);
				accessor.invokeStartAttack();
				accessor.invokeStartUseItem();
			} finally {
				operating = false;
			}
		}

		boolean tryOperate(Minecraft mc, ClientLevel level) {
			HitResult hitResult = mc.hitResult;
			if(level != null && hitResult instanceof BlockHitResult blockHitResult
				&& level.getBlockState(blockHitResult.getBlockPos()).getBlock() == Blocks.REDSTONE_TORCH) {
				MinecraftAccessor accessor = (MinecraftAccessor)mc;
				accessor.invokeStartAttack();
				return true;
			}
			else return false;
		}

		@Override public void onEndTick(@NonNull Minecraft mc) {
			LocalPlayer player = mc.player;
			Entity camera = mc.getCameraEntity();
			ClientLevel level = mc.level;
			int multiSamplingCount = multiSamplingFactor.getAsInt();
			if(player != null && camera != null && level != null) {
				HitResult originHitResult = mc.hitResult;
				for(int i = 1; i < multiSamplingCount; ++i) {
					partialTicks = (float)i /  multiSamplingCount;
					mc.hitResult = player.raycastHitResult(partialTicks, camera);
					if(tryOperate(mc, level)) return;
				}
				partialTicks = 1.0f;
				mc.hitResult = originHitResult;
				tryOperate(mc, level);
			}
		}

		@Override public void onValueChanged() {
			Registries.END_CLIENT_TICK.register(this, BKConfig.getBooleanValue() && torchAutoOperate.getBooleanValue());
		}
	}
}
