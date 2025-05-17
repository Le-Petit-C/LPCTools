package lpctools.tools;

import fi.dy.masa.malilib.util.InfoUtils;
import fi.dy.masa.malilib.util.StringUtils;
import lpctools.lpcfymasaapi.implementations.ILPCConfig;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.ThirdListConfig;
import lpctools.lpcfymasaapi.implementations.ILPCConfigList;
import lpctools.tools.autoGrindstone.AutoGrindstone;
import lpctools.tools.antiSpawner.AntiSpawner;
import lpctools.tools.slightXRay.SlightXRay;
import lpctools.tools.fillingAssistant.FillingAssistant;
import lpctools.tools.liquidCleaner.LiquidCleaner;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;

public class ToolConfigs {
    static ThirdListConfig FAConfig;
    static ThirdListConfig LCConfig;
    static ThirdListConfig SXConfig;
    static ThirdListConfig AGConfig;
    static ThirdListConfig ASConfig;
    public static void init(){
        ILPCConfigList lastList = peekConfigList();
        try(ConfigListLayer layer = new ConfigListLayer()){
            layer.set(FAConfig = addThirdListConfig(lastList, "FA", false));
            FillingAssistant.init();
            layer.set(LCConfig = addThirdListConfig(lastList, "LC", false));
            LiquidCleaner.init();
            layer.set(SXConfig = addThirdListConfig(lastList, "SX", false));
            SlightXRay.init();
            layer.set(AGConfig = addThirdListConfig(lastList, "AG", false));
            AutoGrindstone.init();
            layer.set(ASConfig = addThirdListConfig(lastList, "AS", false));
            AntiSpawner.init();
        }
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
