package lpctools.scripts;

import com.google.common.collect.ImmutableSortedMap;
import lpctools.LPCTools;
import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.MutableConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.UniqueIntegerConfig;
import lpctools.lpcfymasaapi.interfaces.ButtonConsumer;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import net.minecraft.client.MinecraftClient;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;

public class ScriptConfigs {
    public static final LPCConfigList script = new LPCConfigList(LPCTools.page, "scripts");
    static {listStack.push(script);}
    @SuppressWarnings("unused")
    public static final ConfigSizeConfig configSize = addConfig(new ConfigSizeConfig(peekConfigList()));
    public static final MutableConfig<ScriptConfig> scripts = addMutableConfig("scripts", script.getFullTranslationKey(), ImmutableSortedMap.of(
        "script", ScriptConfig::new
    ), null);
    static {listStack.pop();}
    public static class ConfigSizeConfig extends UniqueIntegerConfig {
        public ConfigSizeConfig(ILPCConfigReadable parent) {
            super(parent, "guiScale", 0, 0, 10, null);
        }
        @Override public void addButtons(int x, int y, float zLevel, int labelWidth, int configWidth, ButtonConsumer consumer) {
            super.addButtons(x, y, zLevel, labelWidth, configWidth, consumer);
            intValue = MinecraftClient.getInstance().options.getGuiScale().getValue();
        }
        @Override public void onValueChanged() {
            super.onValueChanged();
            int value = intValue;
            MinecraftClient mc = MinecraftClient.getInstance();
            mc.send(()->mc.options.getGuiScale().setValue(value));
        }
    }
}
