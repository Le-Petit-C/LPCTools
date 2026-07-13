package lpctools.generic;

import lpctools.util.AlgorithmUtils;
import lpctools.util.data.minecraft.MutableAABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
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
    public static class MobSpawnTest {
        private final MutableAABB entityAABB = new MutableAABB();
        private final MutableAABB blockAABBPartCache = new MutableAABB();
        private final BlockPos.MutableBlockPos blockPosCache = new BlockPos.MutableBlockPos();
        private final Shapes.DoubleLineConsumer forAllBoxesTask =
            (x1, y1, z1, x2, y2, z2)->{
            blockAABBPartCache.set(x1, y1, z1, x2, y2, z2).moveAndSet(blockPosCache);
            if(entityAABB.intersects(blockAABBPartCache)) booleanCache = true;
        };
        private boolean booleanCache;
        private MobSpawnTest() {}
        public boolean mayMobSpawnAt(@NotNull BlockGetter world, @Nullable LevelLightEngine light, BlockPos pos){
            BlockState block = world.getBlockState(pos);
            // if(!block.getCollisionShape(world, pos).isEmpty()) return false;
            if(block.isSignalSource()) return false;
            if(block.getBlock() instanceof BaseRailBlock) return false;
            if(light != null && light.getRawBrightness(pos, 15) > spawnLightLevelLimit.getAsInt()) return false;
            Vec3 hitBoxSize = hitBoxRequirement.getPos();
            booleanCache = false;
            entityAABB.set(-hitBoxSize.x * 0.5, 0, -hitBoxSize.z * 0.5, hitBoxSize.x * 0.5, hitBoxSize.y, hitBoxSize.z * 0.5)
                .moveAndSet(0.5, 0.0, 0.5).moveAndSet(pos);
            for(BlockPos pos1 : AlgorithmUtils.iterateInBoxTouched(entityAABB)) {
                VoxelShape collisionShape = world.getBlockState(pos1).getCollisionShape(world, pos1);
                if(collisionShape.isEmpty()) continue;
                else {
                    blockPosCache.set(pos1);
                    collisionShape.forAllBoxes(forAllBoxesTask);
                }
                if(booleanCache) return false;
            }
            int fluidLevel = block.getFluidState().getAmount();
            if(fluidLevel != 0){
                if(liquidPlacesAsCanSpawn.getAsBoolean()) return fluidLevel >= 8;
                else return false;
            }
            return mayMobSpawnOn(world.getBlockState(pos.below()));
        }
    }
    public static MobSpawnTest createSpawnTest() { return new MobSpawnTest(); }
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
