package lpctools.generic;

import fi.dy.masa.malilib.hotkeys.KeyAction;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.configButtons.derivedConfigs.ConfigOpenGuiConfig;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.HotkeyConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.*;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.BooleanConfig;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.IntegerConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import lpctools.util.CachedSupplier;
import lpctools.util.javaex.PriorityThreadPoolExecutor;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.phys.Vec3;

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
    private static final ILPCValueChangeCallback runSpawnConditionChanged = SPAWN_CONDITION_CHANGED.runner()::onSpawnConditionChanged;
    public static final LPCConfigList generic = new LPCConfigList(page, "generic");
    static {listStack.push(generic);}
    @SuppressWarnings("unused")
    public static final ConfigOpenGuiConfig configOpenGuiConfig = addConfigOpenGuiConfig("Z,C");
    public static final BooleanConfig pauseOnConfigPage = addBooleanConfig("pauseOnConfigPage", false);
    static {pauseOnConfigPage.setValueChangeCallback(()->pauseOnConfigPage.getPage().shouldPause = pauseOnConfigPage.getAsBoolean());}
    public static final IntegerConfig labelButtonDistance = addIntegerConfig("labelButtonDistance", 10, 0, 100);
    public static final IntegerConfig indentAll = addIntegerConfig("indentAll", 0, 0, 100);
    public static final IntegerConfig indentSpaces = addIntegerConfig("indentSpaces", 4, 0, 20);
    public static final IntegerConfig indentShift = addIntegerConfig("indentShift", 0, 0, 100);
    public static final BooleanConfig useLabelIndent = addBooleanConfig("useLabelIndent", false);
    static {useLabelIndent.setValueChangeCallback(()-> useLabelIndent.getPage().markNeedUpdate());}
    public static final BooleanThirdListConfig useIndependentThreadPool = addBooleanThirdListConfig("threadPool", true, null);
    public static final IntegerConfig threadCountConfig = addIntegerConfig(useIndependentThreadPool, "threadCount", Runtime.getRuntime().availableProcessors(),
        1, Runtime.getRuntime().availableProcessors() - 1, GenericConfigs::threadCountConfigCallback);
    public static final IntegerConfig spawnLightLevelLimit = addIntegerConfig("spawnLightLevelLimit", 0, 0, 15, runSpawnConditionChanged);
    public static final BooleanConfig liquidPlacesAsCanSpawn = addBooleanConfig("liquidPlacesAsCanSpawn", false, runSpawnConditionChanged);
    public static final BlockListConfig extraSpawnBlocks = addBlockListConfig("extraSpawnBlocks", defaultExtraSpawnBlocks, runSpawnConditionChanged);
    public static final BlockListConfig extraNoSpawnBlocks = addBlockListConfig("extraNoSpawnBlocks", defaultExtraNoSpawnBlocks, runSpawnConditionChanged);
    public static final Vector3dConfig hitBoxRequirement = addConfigEx(list->new Vector3dConfig(list, "hitBoxRequirement", new Vec3(Zombie.DEFAULT_BB_WIDTH, Zombie.DEFAULT_BB_HEIGHT, Zombie.DEFAULT_BB_WIDTH), GenericConfigs::hitBoxCallback));
    public static final BooleanConfig reachDistanceAlwaysUnlimited = addBooleanConfig("reachDistanceAlwaysUnlimited", false);
    public static final BooleanConfig playClickSoundFromModMenu = addBooleanConfig("playClickSoundFromModMenu", false);
    public static final HotkeyConfig horizontalScrollButton = addHotkeyConfig("horizontalScrollKey",
        KeybindSettings.create(KeybindSettings.Context.GUI, KeyAction.BOTH, true, false, false, false), null, null);
    static {addConfig(SelectionScreenConfigs.selectionScreenConfigs);}
    @SuppressWarnings("unused")
    public static final ButtonConfig clearLPCToolsCache = addButtonConfig("clearLPCToolsCache", (b, m)->CachedSupplier.clearAllCache());
    public static final IntegerConfig updateLimitPerFrame = addIntegerConfig("updateLimitPerFrame", 8192);
    public static final UniqueDoubleConfig zFightBias = addConfigEx(l->new UniqueDoubleConfig(l, "zFightBias", 1.0 / (1 << 18), 1.0 / (1 << 30), 1, GenericConfigs::zFightBiasCallback)).logMode();
    public static final UniqueIntegerConfig maxCommandLength = addConfigEx(l->new UniqueIntegerConfig(l, "maxCommandLength", 32767, 0, Integer.MAX_VALUE, null));
    static {threadCountConfig.onValueChanged();}
    static {listStack.pop();}
    
    private static double hitBoxClamp(double x) {
        return Math.min(Math.max(x, 0), 16);
    }
    private static void hitBoxCallback() {
        Vec3 pos = hitBoxRequirement.getPos();
        if(hitBoxClamp(pos.x()) != pos.x() || hitBoxClamp(pos.y()) != pos.y() || hitBoxClamp(pos.z()) != pos.z())
            hitBoxRequirement.setPos(new Vec3(hitBoxClamp(pos.x()), hitBoxClamp(pos.y()), hitBoxClamp(pos.z())));
        else runSpawnConditionChanged.onValueChanged();
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
    private static void zFightBiasCallback() {
        GenericUtils.zFightBias = (float)zFightBias.getDoubleValue();
    }
}
