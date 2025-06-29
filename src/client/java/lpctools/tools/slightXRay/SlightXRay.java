package lpctools.tools.slightXRay;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import fi.dy.masa.malilib.util.Color4f;
import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.ConfigListOptionListConfigEx;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.ThirdListConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.*;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigBase;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigList;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.RangeLimitConfig;
import lpctools.mixin.client.SpriteContentsMixin;
import lpctools.util.DataUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.Sprite;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.lang.Math;
import java.util.*;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;
import static lpctools.tools.ToolUtils.*;
import static lpctools.util.DataUtils.*;

@SuppressWarnings("deprecation")
public class SlightXRay extends ThirdListConfig{
    static final @NotNull ImmutableList<Block> defaultXRayBlocks = ImmutableList.of(
        Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE,
        Blocks.DEEPSLATE_COAL_ORE, Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE,
        Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN, Blocks.ENDER_CHEST, Blocks.REINFORCED_DEEPSLATE,
        Blocks.BUDDING_AMETHYST, Blocks.CALCITE,
        Blocks.ANCIENT_DEBRIS
    );
    static final @NotNull HashMap<Block, MutableInt> XRayBlocks;
    static final @NotNull ImmutableList<String> defaultXRayBlockIds = idListFromBlockList(defaultXRayBlocks);
    public final BooleanHotkeyConfig slightXRay;
    public final ConfigListOptionListConfigEx<ConfigListWithColorMethod> defaultColorMethod;
    public final ColorConfig defaultColor;
    public final StringListConfig XRayBlocksConfig;
    public final RangeLimitConfig displayRange;
    public final DoubleConfig saturationDelta;
    public final DoubleConfig brightnessDelta;
    public final IntegerConfig defaultAlpha;
    public final BooleanConfig useCullFace;
    static {
        XRayBlocks = new HashMap<>();
        for(Block block : defaultXRayBlocks)
            XRayBlocks.put(block, new MutableInt(0));
    }
    
    public SlightXRay(ILPCConfigList parent) {
        super(parent, "SX", false);
        try(ConfigListLayer ignored = new ConfigListLayer(this)){
            slightXRay = addBooleanHotkeyConfig("slightXRay", false, null, this::switchChanged);
            setLPCToolsToggleText(slightXRay);
            defaultColorMethod = peekConfigList().addConfig(
                new ConfigListOptionListConfigEx<>(peekConfigList(), "defaultColorMethod", this::refreshXRayBlocks){
                    @Override public void setValueFromJsonElement(@NotNull JsonElement element) {
                        super.setValueFromJsonElement(element);
                        onValueChanged();
                    }
                });
            ILPCConfigList byTextureColor = defaultColorMethod.addList(
                new ConfigListWithColorBase(defaultColorMethod, "byTextureColor", this::getColorByTextureColor)
            );
            defaultAlpha = addIntegerConfig(byTextureColor, "defaultAlpha", 127, 0, 255, this::refreshXRayBlocks);
            saturationDelta = addDoubleConfig(byTextureColor, "saturationDelta", 1, -5, 5, this::refreshXRayBlocks);
            brightnessDelta = addDoubleConfig(byTextureColor, "brightnessDelta", 1, -5, 5, this::refreshXRayBlocks);
            ILPCConfigList byDefaultColor = defaultColorMethod.addList(
                new ConfigListWithColorBase(defaultColorMethod, "byDefaultColor", this::getColorByDefaultColor) {}
            );
            defaultColor = addColorConfig(byDefaultColor, "defaultColor", new Color4f(0.5f, 0.5f, 1.0f, 0.5f), this::refreshXRayBlocks);
            XRayBlocksConfig = addStringListConfig("XRayBlocks", defaultXRayBlockIds, this::refreshXRayBlocks);
            useCullFace = addBooleanConfig("useCullFace", true);
            displayRange = addRangeLimitConfig(false);
            displayRange.setValueChangeCallback(()->{if(renderInstance != null) renderInstance.onRenderRangeChanged(displayRange);});
        }
    }
    private int getColorByDefaultColor(Block block){
        return DataUtils.argb2agbr(defaultColor.getIntegerValue());
    }
    private int getColorByTextureColor(Block block) {
        int alphaMask = defaultAlpha.getAsInt() << 24;
        try{
            BakedModel model = MinecraftClient.getInstance().getBlockRenderManager()
                .getModel(block.getDefaultState());
            Sprite particleSprite = model.getParticleSprite();
            float r = 0, g = 0, b = 0;
            float t = 0;
            for(NativeImage image : ((SpriteContentsMixin)particleSprite.getContents()).getMipmapLevelsImages()){
                for(int color : image.copyPixelsRgba()){
                    float k = (color >>> 24) / 255.0f;
                    r += ((color >>> 16) & 0xff) * k;
                    g += ((color >>> 8) & 0xff) * k;
                    b += (color & 0xff) * k;
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
    
    public static double atanh(double x) {
        if (Math.abs(x) > 1) throw new IllegalArgumentException("atanh: input value out of bound [-1, 1]");
        return 0.5 * Math.log((1 + x) / (1 - x));
    }

    private void refreshXRayBlocks(){
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
    private void switchChanged() {
        if(slightXRay.getAsBoolean()){
            if(renderInstance == null)
                renderInstance = new RenderInstance(this, MinecraftClient.getInstance());
        }
        else {
            if(renderInstance != null) {
                renderInstance.close();
                renderInstance = null;
            }
        }
    }

    public interface DefaultColorMethod{
        int getColor(Block block);
    }
    public interface ConfigListWithColorMethod extends ILPCConfigList, DefaultColorMethod{}
    public static class ConfigListWithColorBase extends LPCConfigList implements ConfigListWithColorMethod{
        public ConfigListWithColorBase(ILPCConfigBase parent, String nameKey, @NotNull DefaultColorMethod defaultColorMethod) {
            super(parent, nameKey);
            this.defaultColorMethod = defaultColorMethod;
        }
        @Override public int getColor(Block block) {
            return defaultColorMethod.getColor(block);
        }
        @NotNull DefaultColorMethod defaultColorMethod;
    }
    private static @Nullable RenderInstance renderInstance;
}
