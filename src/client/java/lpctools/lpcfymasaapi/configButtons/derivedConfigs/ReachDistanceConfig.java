package lpctools.lpcfymasaapi.configButtons.derivedConfigs;

import lpctools.generic.GenericConfigs;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.DoubleConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigList;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import lpctools.util.AlgorithmUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ReachDistanceConfig extends DoubleConfig {
    public ReachDistanceConfig(ILPCConfigList parent) {this(parent, null);}
    public ReachDistanceConfig(ILPCConfigList parent, @Nullable ILPCValueChangeCallback callback) {
        super(parent, "reachDistance", 4.5, 0, 5, callback);
    }
    public Iterable<BlockPos> iterateFromClosest(Vec3d center){
        return AlgorithmUtils.iterateFromClosestInDistance(center, getAsDouble());
    }
    public Iterable<BlockPos> iterateFromFurthest(Vec3d center){
        return AlgorithmUtils.iterateFromFurthestInDistance(center, getAsDouble());
    }
    @Override public void refreshName() {
        ClientPlayerInteractionManager itm = MinecraftClient.getInstance().interactionManager;
        if(GenericConfigs.reachDistanceAlwaysUnlimited.getAsBoolean() || itm == null)
            setMax(Double.MAX_VALUE);
        else setMax(itm.getReachDistance());
        super.refreshName();
    }
    @Override public @NotNull String getFullTranslationKey() {
        return "lpctools.configs.utils.reachDistance";
    }
}
