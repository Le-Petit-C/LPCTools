package lpctools.tools.furnaceMaintainer;

import lpctools.generic.GenericUtils;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.ReachDistanceConfig;
import lpctools.lpcfymasaapi.configbutton.uniqueConfigs.BooleanHotkeyThirdListConfig;
import lpctools.lpcfymasaapi.configbutton.uniqueConfigs.ButtonHotkeyConfig;
import lpctools.tools.ToolConfigs;
import lpctools.tools.ToolUtils;
import lpctools.util.AlgorithmUtils;
import lpctools.util.MathUtils;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;
import static lpctools.tools.furnaceMaintainer.FurnaceMaintainerData.*;

//TODO:未检测熔炉渲染

public class FurnaceMaintainer {
    public static final BooleanHotkeyThirdListConfig FMConfig = new BooleanHotkeyThirdListConfig(ToolConfigs.toolConfigs, "FM", FurnaceMaintainer::switchCallback);
    static {ToolUtils.setLPCToolsToggleText(FMConfig);}
    static {listStack.push(FMConfig);}
    public static final ReachDistanceConfig reachDistance = addReachDistanceConfig();
    @SuppressWarnings("unused")
    public static final ButtonHotkeyConfig detectFurnaces = addButtonHotkeyConfig("detectFurnaces", null, FurnaceMaintainer::detectFurnacesCallback);
    static {listStack.pop();}
    private static void switchCallback(){
        if(FMConfig.getBooleanValue()){
            if(runner == null)
                runner = new FurnaceMaintainerRunner();
        }
        else {
            if(runner != null){
                runner.close();
                runner = null;
            }
        }
    }
    private static void detectFurnacesCallback(){
        AlgorithmUtils.cancelTasks(detectTasks);
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        ClientWorld world = mc.world;
        if(player == null || world == null) return;
        for(Chunk chunk : AlgorithmUtils.iterateLoadedChunksFromClosest(world, player.getEyePos()))
            asyncDetectFurnace(chunk, MathUtils.squaredDistance(player.getPos(), chunk.getPos()));
    }
    private static void asyncDetectFurnace(Chunk chunk, double distanceSquared){
        detectTasks.add(GenericUtils.supplyAsync(()->detectFurnace(chunk), distanceSquared));
    }
    private static ArrayList<BlockPos> detectFurnace(Chunk chunk){
        ArrayList<BlockPos> result = new ArrayList<>();
        for(BlockPos pos : AlgorithmUtils.iterateInBox(0, chunk.getBottomY(), 0, 15, chunk.getBottomY() + chunk.getHeight() - 1, 15)){
            if(chunk.getBlockState(pos).getBlock() instanceof AbstractFurnaceBlock)
                result.add(pos.add(chunk.getPos().getStartPos()).toImmutable());
        }
        return result;
    }
}
