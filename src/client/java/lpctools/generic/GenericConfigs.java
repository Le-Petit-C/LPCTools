package lpctools.generic;

import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.configButtons.derivedConfigs.ConfigOpenGuiConfig;
import lpctools.lpcfymasaapi.configButtons.derivedConfigs.ObjectListConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.BooleanThirdListConfig;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.BooleanConfig;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.IntegerConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import lpctools.util.javaex.PriorityThreadPoolExecutor;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static lpctools.LPCTools.*;
import static lpctools.generic.GenericData.*;
import static lpctools.generic.GenericRegistry.*;
import static lpctools.lpcfymasaapi.LPCConfigStatics.*;

//TODO:生成条件：不检测脚下方块（蝙蝠）
//TODO:生成条件：天空光照/方块光照切换
//TODO:Shadow Config

public class GenericConfigs {
    private static final ILPCValueChangeCallback runSpawnConditionChanged = SPAWN_CONDITION_CHANGED.run()::onSpawnConditionChanged;
    public static final LPCConfigList generic = new LPCConfigList(page, "generic");
    static {listStack.push(generic);}
    @SuppressWarnings("unused")
    public static final ConfigOpenGuiConfig configOpenGuiConfig = addConfigOpenGuiConfig("Z,C");
    public static final IntegerConfig labelButtonDistance = addIntegerConfig("labelButtonDistance", 10, 0, 100);
    public static final IntegerConfig indentAll = addIntegerConfig("indentAll", 0, 0, 100);
    public static final IntegerConfig indentSpaces = addIntegerConfig("indentSpaces", 4, 0, 20);
    public static final IntegerConfig indentShift = addIntegerConfig("indentShift", 0, 0, 100);
    public static final BooleanConfig useLabelIndent = addBooleanConfig("useLabelIndent", false);
    static {useLabelIndent.setValueChangeCallback(()-> useLabelIndent.getPage().updateIfCurrent());}
    public static final BooleanThirdListConfig useIndependentThreadPool = addBooleanThirdListConfig("threadPool", true, null);
    public static final IntegerConfig threadCountConfig = addIntegerConfig(useIndependentThreadPool, "threadCount", 4,
        1, Runtime.getRuntime().availableProcessors(), GenericConfigs::threadCountConfigCallback);
    public static final IntegerConfig spawnLightLevelLimit = addIntegerConfig("spawnLightLevelLimit", 0, 0, 15, runSpawnConditionChanged);
    public static final BooleanConfig liquidPlacesAsCanSpawn = addBooleanConfig("liquidPlacesAsCanSpawn", false,runSpawnConditionChanged);
    public static final ObjectListConfig.BlockListConfig extraSpawnBlocks = addBlockListConfig("extraSpawnBlocks", defaultExtraSpawnBlocks);
    public static final ObjectListConfig.BlockListConfig extraNoSpawnBlocks = addBlockListConfig("extraNoSpawnBlocks", defaultExtraNoSpawnBlocks);
    public static final BooleanConfig reachDistanceAlwaysUnlimited = addBooleanConfig("reachDistanceAlwaysUnlimited", false);
    static {threadCountConfig.onValueChanged();}
    static {listStack.pop();}
    
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
}
