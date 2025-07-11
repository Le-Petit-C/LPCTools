package lpctools.tools.slightXRay;

import fi.dy.masa.malilib.util.data.Color4f;
import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.ConfigListOptionListConfigEx;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.*;
import lpctools.lpcfymasaapi.configbutton.uniqueConfigs.BooleanHotkeyThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigBase;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigList;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.RangeLimitConfig;
import lpctools.mixin.client.SpriteContentsMixin;
import lpctools.tools.ToolConfigs;
import lpctools.util.DataUtils;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.Sprite;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.*;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;
import static lpctools.tools.ToolUtils.*;
import static lpctools.tools.slightXRay.SlightXRayData.*;
import static lpctools.util.DataUtils.*;

public class SlightXRay{
    public static final BooleanHotkeyThirdListConfig SXConfig = new BooleanHotkeyThirdListConfig(ToolConfigs.toolConfigs, "SX", SlightXRay::switchChanged);
    static {setLPCToolsToggleText(SXConfig);}
    static {listStack.push(SXConfig);}
    public static final ConfigListOptionListConfigEx<ConfigListWithColorMethod> defaultColorMethod = addConfigListOptionListConfigEx("defaultColorMethod", SlightXRay::refreshXRayBlocks);
    public static final ILPCConfigList byTextureColor = defaultColorMethod.addList(
        new ConfigListWithColorBase(defaultColorMethod, "byTextureColor", SlightXRay::getColorByTextureColor));
    public static final IntegerConfig defaultAlpha = addIntegerConfig(byTextureColor, "defaultAlpha", 127, 0, 255, SlightXRay::refreshXRayBlocks);
    public static final DoubleConfig saturationDelta = addDoubleConfig(byTextureColor, "saturationDelta", 1, -5, 5, SlightXRay::refreshXRayBlocks);
    public static final DoubleConfig brightnessDelta = addDoubleConfig(byTextureColor, "brightnessDelta", 1, -5, 5, SlightXRay::refreshXRayBlocks);
    public static final ILPCConfigList byDefaultColor = defaultColorMethod.addList(new ConfigListWithColorBase(defaultColorMethod, "byDefaultColor", SlightXRay::getColorByDefaultColor));
    public static final ColorConfig defaultColor = addColorConfig(byDefaultColor, "defaultColor", new Color4f(0.5f, 0.5f, 1.0f, 0.5f), SlightXRay::refreshXRayBlocks);
    public static final StringListConfig XRayBlocksConfig = addStringListConfig("XRayBlocks", defaultXRayBlockIds, SlightXRay::refreshXRayBlocks);
    public static final BooleanConfig useCullFace = addBooleanConfig("useCullFace", true);
    public static final RangeLimitConfig displayRange = addRangeLimitConfig(false);
    static {displayRange.setValueChangeCallback(()->{if(renderInstance != null) renderInstance.onRenderRangeChanged(displayRange);});}
    static {listStack.pop();}
    
    private static int getColorByTextureColor(Block block) {
        int alphaMask = defaultAlpha.getAsInt() << 24;
        try{
            BlockStateModel model = MinecraftClient.getInstance().getBlockRenderManager()
                .getModel(block.getDefaultState());
            Sprite particleSprite = model.particleSprite();
            float r = 0, g = 0, b = 0;
            float t = 0;
            for(NativeImage image : ((SpriteContentsMixin)particleSprite.getContents()).getMipmapLevelsImages()){
                for(int color : image.copyPixelsArgb()){
                    float k = (color >>> 24) / 255.0f;
                    r += (color & 0xff) * k;
                    g += ((color >>> 8) & 0xff) * k;
                    b += ((color >>> 16) & 0xff) * k;
                    t += k;
                }
            }
            if(t == 0) return alphaMask;
            int ri = Math.round(r / t);
            int gi = Math.round(g / t);
            int bi = Math.round(b / t);
            float[] hsb = Color.RGBtoHSB(ri, gi, bi, new float[3]);
            hsb[1] = (float) Math.tanh(atanh(hsb[1] * 2 - 1) + saturationDelta.getAsDouble()) * 0.5f + 0.5f;
            hsb[2] = (float) Math.tanh(atanh(hsb[2] * 2 - 1) + brightnessDelta.getAsDouble()) * 0.5f + 0.5f;
            return (Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]) & 0x00ffffff) | alphaMask;
        }
        catch (Exception e){return alphaMask;}
    }
    private static int getColorByDefaultColor(Block block){return DataUtils.argb2agbr(defaultColor.getIntegerValue());}
    
    public static double atanh(double x) {
        if (Math.abs(x) > 1) throw new IllegalArgumentException("atanh: input value out of bound [-1, 1]");
        return 0.5 * Math.log((1 + x) / (1 - x));
    }

    private static void refreshXRayBlocks(){
        if(XRayBlocksConfig == null) return;
        HashMap<Block, MutableInt> newBlocks = new HashMap<>();
        for(String str : XRayBlocksConfig){
            String[] splits = str.split(";");
            if(splits.length > 0 && splits.length < 3){
                Block block = getBlockFromId(splits[0], true);
                if(block == null) continue;
                Integer color = null;
                if(splits.length > 1) {
                    try {
                        color = Integer.parseUnsignedInt(splits[1], 16);
                    }catch (NumberFormatException e){
                        warnInvalidString(str);
                        continue;
                    }
                }
                if(color == null) color = defaultColorMethod.getCurrentUserdata().getColor(block);
                newBlocks.put(block, new MutableInt(color));
            }
            else warnInvalidString(str);
        }
        synchronized (XRayBlocks){
            if(XRayBlocks.keySet().equals(newBlocks.keySet())) {
                for(Map.Entry<Block, MutableInt> block : newBlocks.entrySet())
                    XRayBlocks.get(block.getKey()).setValue(block.getValue());
                if(renderInstance != null) renderInstance.resetRender();
                return;
            }
            XRayBlocks.clear();
            XRayBlocks.putAll(newBlocks);
            if(renderInstance != null) renderInstance.resetData();
        }
    }
    private static void warnInvalidString(String str){
        notifyPlayer(String.format("§eWarning: Invalid string \"%s\"", str), false);
    }
    private static void switchChanged() {
        if(SXConfig.getBooleanValue()){
            if(renderInstance == null)
                renderInstance = new RenderInstance(MinecraftClient.getInstance());
        }
        else {
            if(renderInstance != null) {
                renderInstance.close();
                renderInstance = null;
            }
        }
    }

    public interface DefaultColorMethod{ int getColor(Block block); }
    public interface ConfigListWithColorMethod extends ILPCConfigList, DefaultColorMethod{}
    public static class ConfigListWithColorBase extends LPCConfigList implements ConfigListWithColorMethod{
        public ConfigListWithColorBase(ILPCConfigBase parent, String nameKey, @NotNull DefaultColorMethod defaultColorMethod) {
            super(parent, nameKey);
            this.defaultColorMethod = defaultColorMethod;
        }
        @Override public int getColor(Block block) {
            return defaultColorMethod.getColor(block);
        }
        public final @NotNull DefaultColorMethod defaultColorMethod;
    }
}
