package lpctools.tools;

import fi.dy.masa.malilib.util.StringUtils;
import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.LPCConfigPage;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.ThirdListConfig;
import lpctools.tools.autoGrindstone.AutoGrindstone;
import lpctools.tools.slightXRay.SlightXRay;
import lpctools.tools.fillingAssistant.FillingAssistant;
import lpctools.tools.liquidCleaner.LiquidCleaner;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ToolConfigs {
    public static LPCConfigList tools;
    static ThirdListConfig FAConfig;
    static ThirdListConfig LCConfig;
    static ThirdListConfig SXConfig;
    static ThirdListConfig AGConfig;
    public static void init(@NotNull LPCConfigPage page){
        tools = page.addList("tools");
        FillingAssistant.init(FAConfig = tools.addThirdListConfig("FA", false));
        LiquidCleaner.init(LCConfig = tools.addThirdListConfig("LC", false));
        SlightXRay.init(SXConfig = tools.addThirdListConfig("SX", false));
        AutoGrindstone.init(AGConfig = tools.addThirdListConfig("AG", false));
    }
    public static void displayDisableReason(@NotNull String toolDisableKey, @Nullable String reasonKey){
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if(player == null) return;
        String reason = StringUtils.translate("lpctools.tools." + toolDisableKey);
        if(reasonKey != null)
            reason += " : " + StringUtils.translate("lpctools.tools.disableReason." + reasonKey);
        player.sendMessage(Text.of(reason), true);
    }
}
