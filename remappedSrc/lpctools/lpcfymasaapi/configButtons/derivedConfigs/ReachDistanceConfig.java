package lpctools.lpcfymasaapi.configButtons.derivedConfigs;

import lpctools.generic.GenericConfigs;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.DoubleConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigList;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import lpctools.util.AlgorithmUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ReachDistanceConfig extends DoubleConfig {
    public ReachDistanceConfig(ILPCConfigList parent) {this(parent, null);}
    public ReachDistanceConfig(ILPCConfigList parent, @Nullable ILPCValueChangeCallback callback) {
        super(parent, "reachDistance", 4.5, 0, 5, callback);
    }
    public Iterable<BlockPos> iterateFromClosest(Vec3 center){
        return AlgorithmUtils.iterateFromClosestInDistance(center, getAsDouble());
    }
    public Iterable<BlockPos> iterateFromFurthest(Vec3 center){
        return AlgorithmUtils.iterateFromFurthestInDistance(center, getAsDouble());
    }
    @Override public void refreshName() {
        LocalPlayer player = Minecraft.getInstance().player;
        if(GenericConfigs.reachDistanceAlwaysUnlimited.getAsBoolean() || player == null)
            setMax(Double.MAX_VALUE);
        else setMax(player.blockInteractionRange());
        super.refreshName();
    }
    @Override public @NotNull String getFullTranslationKey() {
        return "lpctools.configs.utils.reachDistance";
    }
}
