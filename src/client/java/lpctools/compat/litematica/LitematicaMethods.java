package lpctools.compat.litematica;

import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.malilib.util.LayerRange;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class LitematicaMethods {
    @Nullable public static LitematicaMethods getInstance(){
        if(isLoaded) return instance;
        isLoaded = true;
        return instance = createInstance();
    }
    public boolean isInsideRenderRange(BlockPos pos){
        LayerRange range = DataManager.getRenderLayerRange();
        int component = range.getAxis().choose(pos.getX(), pos.getY(), pos.getZ());
        return component >= range.getLayerMin() && component <= range.getLayerMax();
    }
    private static boolean isLoaded = false;
    @Nullable private static LitematicaMethods instance;
    @Nullable private static LitematicaMethods createInstance(){
        if(FabricLoader.getInstance().isModLoaded("litematica")) return new LitematicaMethods();
        else return null;
    }
}
