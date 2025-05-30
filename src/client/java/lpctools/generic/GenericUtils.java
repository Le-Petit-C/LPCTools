package lpctools.generic;

import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.SideShapeType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.EmptyBlockView;
import net.minecraft.world.chunk.light.LightingProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

import static lpctools.debugs.DebugConfigs.*;
import static lpctools.generic.GenericConfigs.*;
import static lpctools.util.DataUtils.*;

public class GenericUtils {
    public static boolean mayMobSpawnAt(@NotNull BlockView world, @Nullable LightingProvider light, BlockPos pos){
        BlockState block = world.getBlockState(pos);
        if(!block.getCollisionShape(world, pos).isEmpty()) return false;
        if(block.emitsRedstonePower()) return false;
        if(block.getBlock() instanceof AbstractRailBlock) return false;
        if(light != null && light.getLight(pos, 15) > spawnLightLevelLimit.getAsInt()) return false;
        int fluidLevel = block.getFluidState().getLevel();
        if(fluidLevel != 0){
            if(liquidPlacesAsCanSpawn.getAsBoolean()) return fluidLevel >= 8;
            else return false;
        }
        return mayMobSpawnOn(world.getBlockState(pos.down()));
    }
    //检测是不是可生成方块
    public static boolean mayMobSpawnOn(BlockState steppedBlock){
        if(extraNoSpawnBlocks.contains(steppedBlock.getBlock())) return false;
        if(extraSpawnBlocks.contains(steppedBlock.getBlock())) return true;
        if(!steppedBlock.isOpaque()) return false;
        if(steppedBlock.isOpaqueFullCube()) return true;
        return steppedBlock.isSideSolid(EmptyBlockView.INSTANCE, BlockPos.ORIGIN, Direction.UP, SideShapeType.FULL);
    }
    
    //不应在主线程之外调用这个函数
    public static CompletableFuture<Void> runAsync(Runnable runnable){
        CompletableFuture<Void> ret;
        if(useIndependentThreadPool.getAsBoolean())
            ret = CompletableFuture.runAsync(runnable, threadPool);
        else ret = CompletableFuture.runAsync(runnable);
        if(showExecuteTime.getAsBoolean()){
            synchronized (synchronizeRunnableCountObject){
                if(runnableCount++ == 0){
                    executeStartMillis = System.currentTimeMillis();
                }
            }
            ret.whenComplete((result, e)->{
                boolean notifyPlayer;
                synchronized (synchronizeRunnableCountObject){
                    notifyPlayer = (--runnableCount == 0);
                }
                if(notifyPlayer) notifyPlayer("Task done. Took "
                        + (System.currentTimeMillis() - executeStartMillis)
                        + " millis.", false);
            });
        }
        return ret;
    }
    
    private static final Object synchronizeRunnableCountObject = new Object();
    private static int runnableCount;
    private static long executeStartMillis;
}
