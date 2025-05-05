package lpctools.tools;

import fi.dy.masa.malilib.util.InfoUtils;
import fi.dy.masa.malilib.util.StringUtils;
import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.LPCConfigPage;
import lpctools.lpcfymasaapi.configbutton.ILPCConfig;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.ThirdListConfig;
import lpctools.tools.autoGrindstone.AutoGrindstone;
import lpctools.tools.slightXRay.SlightXRay;
import lpctools.tools.fillingAssistant.FillingAssistant;
import lpctools.tools.liquidCleaner.LiquidCleaner;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

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
    public static void displayDisableReason(@NotNull ILPCConfig tool, @Nullable String reasonKey){
        String reason = StringUtils.translate("lpctools.tools.disableNotification", tool.getNameTranslation());
        if(reasonKey != null)
            reason += " : " + StringUtils.translate("lpctools.tools.disableReason." + reasonKey);
        InfoUtils.sendVanillaMessage(Text.literal(reason));
    }
    public static void displayDisableMessage(@NotNull ILPCConfig tool){displayDisableReason(tool, null);}
    public static void displayEnableMessage(@NotNull ILPCConfig tool){
        InfoUtils.sendVanillaMessage(Text.translatable("lpctools.tools.enableNotification", tool.getNameTranslation()));
    }
    public static <T extends BooleanSupplier & ILPCConfig> void displayToggleMessage(T tool){
        if(tool.getAsBoolean()) displayEnableMessage(tool);
        else displayDisableMessage(tool);
    }
}
