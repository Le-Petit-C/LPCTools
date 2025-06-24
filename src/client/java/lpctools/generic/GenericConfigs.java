package lpctools.generic;

import com.google.common.collect.ImmutableList;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.ConfigOpenGuiConfig;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.ThirdListConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.BooleanConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.IntegerConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.StringListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import lpctools.util.javaex.PriorityThreadPoolExecutor;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

import java.util.HashSet;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static lpctools.generic.GenericRegistry.*;
import static lpctools.lpcfymasaapi.LPCConfigStatics.*;
import static lpctools.util.DataUtils.*;

//TODO:生成条件：不检测脚下方块（蝙蝠）
//TODO:生成条件：天空光照/方块光照切换
//TODO:Shadow Config

public class GenericConfigs {
    private static final ILPCValueChangeCallback runSpawnConditionChanged = SPAWN_CONDITION_CHANGED.run()::onSpawnConditionChanged;
    public static void init(){
        configOpenGuiConfig = addConfigOpenGuiConfig("Z,C");
        spawnLightLevelLimit = addIntegerConfig("spawnLightLevelLimit", 0, 0, 15, runSpawnConditionChanged);
        liquidPlacesAsCanSpawn = addBooleanConfig("liquidPlacesAsCanSpawn", false,runSpawnConditionChanged);
        extraSpawnBlockIds = addStringListConfig("extraSpawnBlocks",
            idListFromBlockList(defaultExtraSpawnBlocks),
            ()->{
            extraSpawnBlocks.clear();
            extraSpawnBlocks.addAll(blockSetFromIds(extraSpawnBlockIds));
            runSpawnConditionChanged.onValueChanged();
        });
        extraNoSpawnBlockIds = addStringListConfig("extraNoSpawnBlocks",
            idListFromBlockList(defaultExtraNoSpawnBlocks),
            ()->{
            extraNoSpawnBlocks.clear();
            extraNoSpawnBlocks.addAll(blockSetFromIds(extraNoSpawnBlockIds));
            runSpawnConditionChanged.onValueChanged();
        });
        reachDistanceAlwaysUnlimited = addBooleanConfig("reachDistanceAlwaysUnlimited", false);
        useIndependentThreadPool = addThirdListConfig("threadPool", true);
        threadCountConfig = addIntegerConfig(useIndependentThreadPool, "threadCount", 4,
            1, Runtime.getRuntime().availableProcessors(),
            GenericConfigs::threadCountConfigCallback);
        threadCountConfig.onValueChanged();
    }
    
    private static void threadCountConfigCallback(){
        PriorityThreadPoolExecutor newPool;
        newPool = new PriorityThreadPoolExecutor(
            threadCountConfig.getAsInt(), threadCountConfig.getAsInt(),
            1, TimeUnit.SECONDS
        );
        ThreadPoolExecutor oldPool = threadPool;
        threadPool = newPool;
        if(oldPool != null) oldPool.close();
    }

    public static ConfigOpenGuiConfig configOpenGuiConfig;
    public static IntegerConfig spawnLightLevelLimit;
    public static BooleanConfig liquidPlacesAsCanSpawn;
    public static StringListConfig extraSpawnBlockIds;
    public static StringListConfig extraNoSpawnBlockIds;
    public static BooleanConfig reachDistanceAlwaysUnlimited;
    public static ThirdListConfig useIndependentThreadPool;
    public static IntegerConfig threadCountConfig;

    public static final ImmutableList<Block> defaultExtraSpawnBlocks = ImmutableList.of();
    public static final ImmutableList<Block> defaultExtraNoSpawnBlocks = ImmutableList.of(
        Blocks.BEDROCK
    );
    public static final HashSet<Block> extraSpawnBlocks = new HashSet<>(defaultExtraSpawnBlocks);
    public static final HashSet<Block> extraNoSpawnBlocks = new HashSet<>(defaultExtraNoSpawnBlocks);
    public static PriorityThreadPoolExecutor threadPool;
}
