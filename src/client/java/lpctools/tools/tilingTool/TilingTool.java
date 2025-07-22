package lpctools.tools.tilingTool;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lpctools.compact.CompactMain;
import lpctools.compact.litematica.LitematicaMethods;
import lpctools.lpcfymasaapi.configButtons.derivedConfigs.*;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.BooleanConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.*;
import lpctools.tools.ToolConfigs;
import lpctools.util.data.Box3i;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;
import static lpctools.tools.ToolUtils.*;
import static lpctools.tools.tilingTool.TilingToolData.*;
import static lpctools.util.DataUtils.notifyPlayer;

public class TilingTool {
    public static final BooleanHotkeyThirdListConfig TTConfig = new BooleanHotkeyThirdListConfig(ToolConfigs.toolConfigs, "TT", TilingTool::switchCallback);
    static {setLPCToolsToggleText(TTConfig);}
    static {listStack.push(TTConfig);}
    public static final ReachDistanceConfig reachDistance = addReachDistanceConfig();
    public static final LimitOperationSpeedConfig limitOperationSpeed = addLimitOperationSpeedConfig(false, 1);
    public static final RangeLimitConfig rangeLimit = addRangeLimitConfig();
    static {rangeLimit.setValueChangeCallback(()->shapeList = rangeLimit.buildShapeList());}
    public static final BooleanConfig offhandOperate = addBooleanConfig("offhandOperate", false);
    public static final BlockPosConfig cornerPos1 = addBlockPosConfig("cornerPos1", BlockPos.ORIGIN, null);
    public static final BlockPosConfig cornerPos2 = addBlockPosConfig("cornerPos2", BlockPos.ORIGIN, null);
    @SuppressWarnings("unused")
    public static final ButtonHotkeyConfig refreshButton = addButtonHotkeyConfig("refresh", null, TilingTool::refreshCallback);
    @SuppressWarnings("unused")
    public static final ButtonHotkeyConfig setByLitematicaButton = addButtonHotkeyConfig("setByLitematica", null, TilingTool::setByLitematica);
    public static final MultiBooleanConfig tilingDirection = addConfig(new MultiBooleanConfig(peekConfigList(), "tilingDirection", ImmutableList.of(true, false, true), null));
    private static final LinkedHashMap<String, AutoRefreshMode> autoRefreshDefaults = new LinkedHashMap<>();
    static {
        autoRefreshDefaults.put("lpctools.configs.tools.TT.autoRefresh.onFirstStart", AutoRefreshMode.ON_FIRST_START);
        autoRefreshDefaults.put("lpctools.configs.tools.TT.autoRefresh.onEveryEnable", AutoRefreshMode.ON_EVERY_ENABLE);
        autoRefreshDefaults.put("lpctools.configs.tools.TT.autoRefresh.noAutoRefresh", AutoRefreshMode.NO_AUTO_REFRESH);
    }
    private static final LinkedHashMap<String, AutoRefreshOperation> autoRefreshOperationDefaults = new LinkedHashMap<>();
    static {
        autoRefreshOperationDefaults.put("lpctools.configs.tools.TT.autoRefreshOperation.refreshButton", AutoRefreshOperation.REFRESH_BUTTON);
        autoRefreshOperationDefaults.put("lpctools.configs.tools.TT.autoRefreshOperation.litematicaThenRefresh", AutoRefreshOperation.LITEMATICA_THEN_REFRESH);
        autoRefreshOperationDefaults.put("lpctools.configs.tools.TT.autoRefreshOperation.litematicaOnly", AutoRefreshOperation.LITEMATICA_ONLY);
    }
    public static final ArrayOptionListConfig<AutoRefreshMode> autoRefresh = addArrayOptionListConfig("autoRefresh", autoRefreshDefaults);
    public static final ArrayOptionListConfig<AutoRefreshOperation> autoRefreshOperation = addArrayOptionListConfig("autoRefreshOperation", autoRefreshOperationDefaults);
    private static final LinkedHashMap<String, Runnable> litematicaButtonModeDefaults = new LinkedHashMap<>();
    static {
        litematicaButtonModeDefaults.put("lpctools.configs.tools.TT.litematicaButtonMode.cornersOnly", TilingTool::litematicaSetCoordinates);
        litematicaButtonModeDefaults.put("lpctools.configs.tools.TT.litematicaButtonMode.cornersAndBuffer", ()->{litematicaSetCoordinates();refreshCallback();});
        litematicaButtonModeDefaults.put("lpctools.configs.tools.TT.litematicaButtonMode.bufferDirectly", TilingTool::litematicaRefreshDirectly);
    }
    public static final ArrayOptionListConfig<Runnable> litematicaButtonMode = addArrayOptionListConfig("litematicaButtonMode", litematicaButtonModeDefaults);
    public static final MutableConfig<ObjectListConfig.BlockListConfig> vagueBlocksConfig = addMutableConfig("vagueBlocks", TTConfig.getFullTranslationKey(), ImmutableMap.of(
        "blocks", (parent, key)->new ObjectListConfig.BlockListConfig(parent, key, null, parent::onValueChanged)
    ), TilingTool::refreshVagueBlocks);
    static {
        vagueBlocksConfig.allocateAndAddConfig("blocks").setBlocks(List.of(Blocks.DIRT, Blocks.GRASS_BLOCK));
        vagueBlocksConfig.setCurrentAsDefault(false);
    }
    
    public enum AutoRefreshOperation implements Runnable{
        REFRESH_BUTTON(TilingTool::refreshCallback),
        LITEMATICA_THEN_REFRESH(()->{litematicaButtonMode.get().run(); refreshCallback();}),
        LITEMATICA_ONLY(()->litematicaButtonMode.get().run());
        public final Runnable operation;
        AutoRefreshOperation(Runnable operation){
            this.operation = operation;
        }
        @Override public void run() {operation.run();}
    }
    public enum AutoRefreshMode {
        ON_FIRST_START(true, false),
        ON_EVERY_ENABLE(false, true),
        NO_AUTO_REFRESH(false, false);
        public final boolean refreshOnExecuteNull, refreshOnToolEnabled;
        AutoRefreshMode(boolean refreshOnExecuteNull, boolean refreshOnToolEnabled){
            this.refreshOnExecuteNull = refreshOnExecuteNull;
            this.refreshOnToolEnabled = refreshOnToolEnabled;
        }
    }
    static {listStack.pop();}
    private static void switchCallback(){
        if(TTConfig.getBooleanValue()){
            if(executor == null) executor = new TilingToolExecutor();
        }
        else {
            if(executor != null){
                executor.close();
                executor = null;
            }
        }
    }
    static void refreshCallback(){refresh(getConfigBox());}
    private static void litematicaSetCoordinates(){
        if(CompactMain.getLitematicaInstance() instanceof LitematicaMethods methods){
            Box3i box = methods.getSelectionBox();
            if(box != null) setConfigBox(box);
            else notifyPlayer(Text.translatable("lpctools.compact.litematica.cantGetSelectionBox"), true);
        }
        else notifyPlayer(Text.translatable("lpctools.compact.missingLitematica"), true);
    }
    private static void litematicaRefreshDirectly(){
        if(CompactMain.getLitematicaInstance() instanceof LitematicaMethods methods){
            Box3i box = methods.getSelectionBox();
            if(box != null) refresh(box);
            else notifyPlayer(Text.translatable("lpctools.compact.litematica.cantGetSelectionBox"), true);
        }
        else notifyPlayer(Text.translatable("lpctools.compact.missingLitematica"), true);
    }
    private static void setByLitematica(){litematicaButtonMode.get().run();}
    private static void refreshVagueBlocks(){
        vagueBlocks.clear();
        for(ObjectListConfig.BlockListConfig config : vagueBlocksConfig.iterateConfigs())
            for(Block block : config.set)
                vagueBlocks.computeIfAbsent(block, v->new ArrayList<>()).add(config.set);
    }
    static void setConfigBox(Box3i box){
        cornerPos1.setPos(box.pos1);
        cornerPos2.setPos(box.pos2);
        TTConfig.getPage().markNeedUpdate();
    }
    static Box3i getConfigBox(Box3i box){
        cornerPos1.getPos(box.pos1);
        cornerPos2.getPos(box.pos2);
        return box;
    }
    static Box3i getConfigBox(){return getConfigBox(new Box3i());}
}
