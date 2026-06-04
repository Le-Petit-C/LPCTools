package lpctools.tools.bedrockKiller.leakPreventer;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.BooleanHotkeyThirdListConfig;
import lpctools.mixin.client.accessors.MinecraftAccessor;
import lpctools.tools.ToolConfigs;
import lpctools.tools.ToolUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;

import static lpctools.lpcfymasaapi.LPCConfigStatics.listStack;

public class BedrockKiller {
	public static final BooleanHotkeyThirdListConfig BKConfig = new BooleanHotkeyThirdListConfig(ToolConfigs.toolConfigs, "BK");
	static { ToolUtils.setLPCToolsToggleText(BKConfig); }
	static { listStack.push(BKConfig); }
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
}
