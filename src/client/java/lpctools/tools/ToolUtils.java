package lpctools.tools;

import fi.dy.masa.malilib.config.IConfigBoolean;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.util.InfoUtils;
import fi.dy.masa.malilib.util.StringUtils;
import lpctools.lpcfymasaapi.interfaces.ILPCConfig;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ToolUtils {
    //通过设置配置的热键回调函数设置一个Boolean配置的切换文本显示为LPCTools默认风格
    public static <T extends IConfigBoolean & IHotkey & ILPCConfig> void setLPCToolsToggleText(T config){
        config.getKeybind().setCallback((action, key)->{
            config.toggleBooleanValue();
            displayToggleMessage(config.getBooleanValue(), config);
            return true;
        });
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
    
    public static void displayToggleMessage(boolean b, ILPCConfig tool){
        if(b) displayEnableMessage(tool);
        else displayDisableMessage(tool);
    }
}
