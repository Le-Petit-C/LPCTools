package lpctools.script;

import fi.dy.masa.malilib.util.data.Color4f;
import lpctools.LPCTools;
import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.configButtons.derivedConfigs.ArrayOptionListConfig;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.BooleanConfig;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.ColorConfig;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.IntegerConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.ButtonConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.ThirdListConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.UniqueIntegerConfig;
import lpctools.lpcfymasaapi.interfaces.ButtonConsumer;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;

public class ScriptConfigs {
    public static final LPCConfigList script = new LPCConfigList(LPCTools.page, "scripts");
    static {listStack.push(script);}
    @SuppressWarnings("unused")
    public static final ConfigSizeConfig configSize = addConfig(new ConfigSizeConfig(peekConfigList()));
    public static final ColorConfig guidelineColor = addColorConfig("guidelineColor", Color4f.fromColor(0xffffffff));
    public static final IntegerConfig guidelineThickness = addIntegerConfig("guidelineThickness", 2, 0, 16);
    public static final ArrayOptionListConfig<DragVisualMode> dragVisualMode = addArrayOptionListConfig("dragVisualMode");
    static {
        String prefix = dragVisualMode.getFullPath().append('.').toString();
        for(var method : DragVisualMode.values())
            dragVisualMode.addOption(prefix + method.key, method.key, method);
    }
    public static final BooleanConfig dragBoundaryConstraint = addBooleanConfig("dragBoundaryConstraint", true);
    public static final ColorConfig moveHighlightColor = addColorConfig("moveHighlightColor", Color4f.fromColor(0x3fffffff));
    public static final ReservedDistanceConfig reservedDistance = addConfig(new ReservedDistanceConfig());
    
    @SuppressWarnings("unused")
    public static final ButtonConfig reloadScripts = addButtonConfig("reloadScripts", (button, mouseButton)->{
            ScriptsConfig.instance.setValueFromJsonElement(ScriptsConfig.instance.getAsJsonElement());
            script.getPage().applyToPageInstanceIfNotNull(page->page.cursorInfo(Text.translatable("lpctools.configs.scripts.reloadScripts.info"), 2000));
        });
    static {addConfig(ScriptsConfig.instance);}
    //static {addConfig(StaticVariables.instance);}
    static {listStack.pop();}
    
    
    public static class ConfigSizeConfig extends UniqueIntegerConfig {
        public ConfigSizeConfig(ILPCConfigReadable parent) {super(parent, "guiScale", 0, 0, 10, null);}
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
    
    public enum DragVisualMode {
        VERTICAL_DRAG ("vertical_drag", false, true),
        FREE_DRAG ("free_drag", true, true),
        POSITION_JUMP ("position_jump", false, false);
        public final String key;
        public final boolean moveX, moveY;
        DragVisualMode (String key, boolean moveX, boolean moveY){
            this.key = key;
            this.moveX = moveX;
            this.moveY = moveY;
        }
    }
    
    public static class ReservedDistanceConfig extends ThirdListConfig{
        public final UniqueIntegerConfig
            left = new UniqueIntegerConfig(this, "left", 40, 0, Integer.MAX_VALUE, null),
            top = new UniqueIntegerConfig(this, "top", 40, 0, Integer.MAX_VALUE, null),
            right = new UniqueIntegerConfig(this, "right", 40, 0, Integer.MAX_VALUE, null),
            bottom = new UniqueIntegerConfig(this, "bottom", 40, 0, Integer.MAX_VALUE, null);
        public ReservedDistanceConfig() {
            super(script, "reservedDistance", null);
            left.allowSlider = top.allowSlider = right.allowSlider = bottom.allowSlider = false;
            addConfigs(left, top, right, bottom);
        }
        
        @Override public void getButtonOptions(ButtonOptionArrayList res) {
            super.getButtonOptions(res);
            if(!isExpanded()){
                left.getButtonOptions(res);
                top.getButtonOptions(res);
                right.getButtonOptions(res);
                bottom.getButtonOptions(res);
            }
        }
    }
}
