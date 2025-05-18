package lpctools.lpcfymasaapi.configbutton.derivedConfigs;

import lpctools.generic.GenericConfigs;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.DoubleConfig;
import lpctools.lpcfymasaapi.implementations.ILPCConfigList;
import lpctools.util.AlgorithmUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public class ReachDistanceConfig extends DoubleConfig {
    public ReachDistanceConfig(ILPCConfigList parent) {
        super(parent, "reachDistance", 4.5, 0, 5);
    }
    public Iterable<BlockPos> iterateFromClosest(Vec3d center){
        return AlgorithmUtils.iterateFromClosestInDistance(center, getAsDouble());
    }
    public Iterable<BlockPos> iterateFromFurthest(Vec3d center){
        return AlgorithmUtils.iterateFromFurthestInDistance(center, getAsDouble());
    }
    @Override public void refreshName(boolean align) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if(player == null || GenericConfigs.reachDistanceAlwaysUnlimited.getAsBoolean())
            setMax(Integer.MAX_VALUE);
        else setMax(player.getBlockInteractionRange());
        super.refreshName(align);
    }
    @Override public @NotNull String getFullTranslationKey() {
        return "lpctools.configs.utils.reachDistance";
    }
}
