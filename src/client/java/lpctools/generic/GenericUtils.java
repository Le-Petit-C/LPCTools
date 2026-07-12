package lpctools.generic;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;

import static lpctools.debugs.DebugConfigs.*;
import static lpctools.generic.GenericConfigs.*;
import static lpctools.generic.GenericData.*;
import static lpctools.util.DataUtils.*;

@SuppressWarnings("unused")
public class GenericUtils {
    static float zFightBias = (float)GenericConfigs.zFightBias.getDoubleValue();
    public static float zFightBias(){ return zFightBias; }
    public static boolean mayMobSpawnAt(@NotNull BlockGetter world, @Nullable LevelLightEngine light, BlockPos pos){
        BlockState block = world.getBlockState(pos);
        if(!block.getCollisionShape(world, pos).isEmpty()) return false;
        if(block.isSignalSource()) return false;
        if(block.getBlock() instanceof BaseRailBlock) return false;
        if(light != null && light.getRawBrightness(pos, 15) > spawnLightLevelLimit.getAsInt()) return false;
        int fluidLevel = block.getFluidState().getAmount();
        if(fluidLevel != 0){
            if(liquidPlacesAsCanSpawn.getAsBoolean()) return fluidLevel >= 8;
            else return false;
        }
        return mayMobSpawnOn(world.getBlockState(pos.below()));
    }
    //检测是不是可生成方块
    public static boolean mayMobSpawnOn(BlockState steppedBlock){
        if(extraNoSpawnBlocks.contains(steppedBlock.getBlock())) return false;
        if(extraSpawnBlocks.contains(steppedBlock.getBlock())) return true;
        if(!steppedBlock.canOcclude()) return false;
        if(steppedBlock.isSolidRender()) return true;
        return steppedBlock.isFaceSturdy(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, Direction.UP, SupportType.FULL);
    }
    
    private static void asyncTest(CompletableFuture<?> future){
        if(!showExecuteTime.getAsBoolean()) return;
        synchronized (synchronizeRunnableCountObject){
            if(runnableCount++ == 0){
                executeStartMillis = System.currentTimeMillis();
            }
        }
        future.whenComplete((result, e)->{
            boolean notifyPlayer;
            synchronized (synchronizeRunnableCountObject){
                notifyPlayer = (--runnableCount == 0);
            }
            if(notifyPlayer) clientMessage("Task done. Took "
                + (System.currentTimeMillis() - executeStartMillis)
                + " millis.", false);
        });
    }
    
    public static CompletableFuture<Void> runAsync(Runnable runnable){
        CompletableFuture<Void> ret;
        if(useIndependentThreadPool.getBooleanValue())
            ret = CompletableFuture.runAsync(runnable, threadPool);
        else ret = CompletableFuture.runAsync(runnable);
        asyncTest(ret);
        return ret;
    }
    public static <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier){
        CompletableFuture<T> ret;
        if(useIndependentThreadPool.getBooleanValue())
            ret = CompletableFuture.supplyAsync(supplier, threadPool);
        else ret = CompletableFuture.supplyAsync(supplier);
        asyncTest(ret);
        return ret;
    }
    public static CompletableFuture<Void> runAsync(Runnable runnable, double priority){
        CompletableFuture<Void> ret;
        if(useIndependentThreadPool.getBooleanValue()){
            ret = new CompletableFuture<>();
            threadPool.execute(()->{
                runnable.run();
                ret.complete(null);
                }, priority);
        }
        else ret = CompletableFuture.runAsync(runnable);//不用自定义线程池时不支持优先级
        asyncTest(ret);
        return ret;
    }
    public static <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier, double priority){
        CompletableFuture<T> ret;
        if(useIndependentThreadPool.getBooleanValue()){
            ret = new CompletableFuture<>();
            threadPool.execute(()->ret.complete(supplier.get()), priority);
        }
        else ret = CompletableFuture.supplyAsync(supplier);//不用自定义线程池时不支持优先级
        asyncTest(ret);
        return ret;
    }
    
    private static final Object synchronizeRunnableCountObject = new Object();
    private static int runnableCount;
    private static long executeStartMillis;
}
