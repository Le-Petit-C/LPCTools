package lpctools.tools.slightXRay;

import fi.dy.masa.malilib.util.data.Color4f;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lpctools.lpcfymasaapi.configButtons.derivedConfigs.ConfigListOptionListConfigEx;
import lpctools.lpcfymasaapi.configButtons.derivedConfigs.RangeLimitConfig;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.*;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigList;
import lpctools.mixin.client.accessors.SpriteContentsAccessor;
import lpctools.tools.ToolUtils;
import lpctools.tools.ToolWithRunnerConfig;
import lpctools.util.DataUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.block.Block;
import org.apache.commons.lang3.mutable.MutableInt;

import java.awt.*;
import java.util.function.ToIntFunction;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;
import static lpctools.tools.slightXRay.SlightXRayData.*;
import static lpctools.util.DataUtils.*;

import com.mojang.blaze3d.platform.NativeImage;

// TODO
//  bug:开着SlightXRay同时渲染范围限制有效，此时进入世界时会有一些期望的范围外的方块被标注
//  暂时不知道如何修复
public class SlightXRay{
    public static final ToolWithRunnerConfig<SlightXRayRunner> SXConfig = ToolUtils.configBuilder("SX").withToolRunner(SlightXRayRunner::new).build();
    static {listStack.push(SXConfig);}
    public static final ColoredBlockListConfig XRayBlocksConfig = addConfigEx(l->new ColoredBlockListConfig(l, "XRayBlocks"));
    public static final ConfigListOptionListConfigEx<ToIntFunction<Block>> defaultColorMethod = addConfigListOptionListConfigEx("defaultColorMethod", XRayBlocksConfig::updateDefaultColor);
    public static final ILPCConfigList byTextureColor = defaultColorMethod.addList("byTextureColor", SlightXRay::getColorByTextureColor);
    public static final IntegerConfig defaultAlpha = addIntegerConfig(byTextureColor, "defaultAlpha", 127, 0, 255, XRayBlocksConfig::updateDefaultColor);
    public static final DoubleConfig saturationDelta = addDoubleConfig(byTextureColor, "saturationDelta", 1, -5, 5, XRayBlocksConfig::updateDefaultColor);
    public static final DoubleConfig brightnessDelta = addDoubleConfig(byTextureColor, "brightnessDelta", 1, -5, 5, XRayBlocksConfig::updateDefaultColor);
    public static final ILPCConfigList byDefaultColor = defaultColorMethod.addList("byDefaultColor", SlightXRay::getColorByDefaultColor);
    public static final ColorConfig defaultColor = addColorConfig(byDefaultColor, "defaultColor", new Color4f(0.5f, 0.5f, 1.0f, 0.5f), XRayBlocksConfig::updateDefaultColor);
    static {addConfig(XRayBlocksConfig);}
    public static final BooleanConfig useCullFace = addBooleanConfig("useCullFace", true, SXConfig.runnerApplyCallback(SlightXRayRunner::updateUseCullFace));
    public static final RangeLimitConfig displayRange = addRangeLimitConfig();
    static {displayRange.setValueChangeCallback(SXConfig.runnerApplyCallback(SlightXRayRunner::updateRangeLimit));}
    static {listStack.pop();}
    static {
        defaultXRayBlocks.forEach(block->XRayBlocksConfig.allocateAndAddConfig().setBlock(block));
        XRayBlocksConfig.setCurrentAsDefault(false);
    }
    
    private static boolean needRefreshXRayBlocks = true;
    
    public static void markNeedRefreshXRayBlocks(){ needRefreshXRayBlocks = true; }
    
    public static double atanh(double x) {
        if (Math.abs(x) > 1) throw new IllegalArgumentException("atanh: input value out of bound [-1, 1]");
        return 0.5 * Math.log((1 + x) / (1 - x));
    }
    
    private static int getColorByTextureColor(Block block) {
        int alphaMask = defaultAlpha.getAsInt() << 24;
        try{ // TODO: 延迟获取颜色（应该等到材质包加载完成之后。。。），否则会引发一大堆NullPointerException
            BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(block.defaultBlockState());
            TextureAtlasSprite particleSprite = model.particleMaterial().sprite();
            float r = 0, g = 0, b = 0;
            float t = 0;
            for(NativeImage image : ((SpriteContentsAccessor)particleSprite.contents()).getByMipLevel()){
                for(int color : image.getPixels()){
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
    private static int getColorByDefaultColor(Block block){return DataUtils.swapRedBlue(defaultColor.getIntegerValue());}

    public static void tryRefreshXRayBlocks(){
        if(!needRefreshXRayBlocks) return;
        needRefreshXRayBlocks = false;
        Object2IntOpenHashMap<Block> newBlocks = new Object2IntOpenHashMap<>();
        XRayBlocksConfig.iterateConfigs().forEach(c->{
            var block = c.getBlock();
            if(newBlocks.containsKey(block)) clientMessage(String.format("§eWarning: Repeat block \"%s\"", block.getName()), false);
            else newBlocks.put(block, DataUtils.swapRedBlue(c.getColor().getIntValue()));
        });
        if(XRayBlocks.keySet().equals(newBlocks.keySet())) {
            for(var block : newBlocks.object2IntEntrySet())
                XRayBlocks.get(block.getKey()).setValue(block.getIntValue());
            SXConfig.applyToRunnerIfPresent(SlightXRayRunner::refreshColor);
        }
        else {
            XRayBlocks.clear();
            for(var entry : newBlocks.object2IntEntrySet())
                XRayBlocks.put(entry.getKey(), new MutableInt(entry.getIntValue()));
            recordedXRayBlocks = null;
            SXConfig.applyToRunnerIfPresent(SlightXRayRunner::resetData);
        }
    }
}
