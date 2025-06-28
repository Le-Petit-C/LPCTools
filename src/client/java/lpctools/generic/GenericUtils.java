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
import java.util.function.Supplier;

import static lpctools.debugs.DebugConfigs.*;
import static lpctools.generic.GenericConfigs.*;
import static lpctools.util.DataUtils.*;

@SuppressWarnings("unused")
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
        //if(steppedBlock.isOpaqueFullCube()) return true;
        return steppedBlock.isSideSolid(EmptyBlockView.INSTANCE, BlockPos.ORIGIN, Direction.UP, SideShapeType.FULL);
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
            if(notifyPlayer) notifyPlayer("Task done. Took "
                + (System.currentTimeMillis() - executeStartMillis)
                + " millis.", false);
        });
    }
    
    public static CompletableFuture<Void> runAsync(Runnable runnable){
        CompletableFuture<Void> ret;
        if(useIndependentThreadPool.getAsBoolean())
            ret = CompletableFuture.runAsync(runnable, threadPool);
        else ret = CompletableFuture.runAsync(runnable);
        asyncTest(ret);
        return ret;
    }
    public static <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier){
        CompletableFuture<T> ret;
        if(useIndependentThreadPool.getAsBoolean())
            ret = CompletableFuture.supplyAsync(supplier, threadPool);
        else ret = CompletableFuture.supplyAsync(supplier);
        asyncTest(ret);
        return ret;
    }
    public static CompletableFuture<Void> runAsync(Runnable runnable, double priority){
        CompletableFuture<Void> ret;
        if(useIndependentThreadPool.getAsBoolean()){
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
        if(useIndependentThreadPool.getAsBoolean()){
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
