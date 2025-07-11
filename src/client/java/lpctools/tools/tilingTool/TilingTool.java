package lpctools.tools.tilingTool;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lpctools.compact.CompactMain;
import lpctools.compact.litematica.LitematicaMethods;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.*;
import lpctools.lpcfymasaapi.configbutton.uniqueConfigs.BooleanHotkeyThirdListConfig;
import lpctools.lpcfymasaapi.configbutton.uniqueConfigs.ButtonHotkeyConfig;
import lpctools.lpcfymasaapi.configbutton.uniqueConfigs.MutableConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfig;
import lpctools.tools.ToolConfigs;
import lpctools.util.data.Box3i;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.LinkedHashMap;

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
    public static final BlockPosConfig cornerPos1 = addBlockPosConfig("cornerPos1", BlockPos.ORIGIN, false);
    public static final BlockPosConfig cornerPos2 = addBlockPosConfig("cornerPos2", BlockPos.ORIGIN, false);
    @SuppressWarnings("unused")
    public static final ButtonHotkeyConfig refreshButton = addButtonHotkeyConfig("refresh", null, TilingTool::refreshCallback);
    @SuppressWarnings("unused")
    public static final ButtonHotkeyConfig setByLitematicaButton = addButtonHotkeyConfig("setByLitematica", null, TilingTool::setByLitematica);
    private static final LinkedHashMap<String, AutoResetMode> autoRefreshDefaults = new LinkedHashMap<>();
    static {
        autoRefreshDefaults.put("lpctools.configs.tools.TT.autoRefresh.onFirstStart", AutoResetMode.ON_FIRST_START);
        autoRefreshDefaults.put("lpctools.configs.tools.TT.autoRefresh.onEveryEnable", AutoResetMode.ON_EVERY_ENABLE);
        autoRefreshDefaults.put("lpctools.configs.tools.TT.autoRefresh.noAutoRefresh", AutoResetMode.NO_AUTO_REFRESH);
    }
    public static final ArrayOptionListConfig<AutoResetMode> autoRefresh = addArrayOptionListConfig("autoRefresh", autoRefreshDefaults);
    private static final LinkedHashMap<String, Runnable> litematicaButtonModeDefaults = new LinkedHashMap<>();
    static {
        litematicaButtonModeDefaults.put("lpctools.configs.tools.TT.litematicaButtonMode.cornersOnly", TilingTool::litematicaSetCoordinates);
        litematicaButtonModeDefaults.put("lpctools.configs.tools.TT.litematicaButtonMode.cornersAndBuffer", ()->{litematicaSetCoordinates();refreshCallback();});
        litematicaButtonModeDefaults.put("lpctools.configs.tools.TT.litematicaButtonMode.bufferDirectly", TilingTool::litematicaRefreshDirectly);
    }
    public static final ArrayOptionListConfig<Runnable> litematicaButtonMode = addArrayOptionListConfig("litematicaButtonMode", litematicaButtonModeDefaults);
    public static final MutableConfig vagueBlocksConfig = addMutableConfig("vagueBlocks", ImmutableList.of(
        new MutableConfig.ConfigAllocator<>("blocks", null,
            (parent, key, user)->new ObjectListConfig.BlockListConfig(parent, key, user, parent::onValueChanged))
    ), ImmutableMap.of("blocks", ImmutableList.of(Blocks.DIRT, Blocks.GRASS_BLOCK)), TilingTool::refreshVagueBlocks);
    
    public enum AutoResetMode{
        ON_FIRST_START(true, false),
        ON_EVERY_ENABLE(false, true),
        NO_AUTO_REFRESH(false, false);
        public final boolean refreshOnExecuteNull, refreshOnToolEnabled;
        AutoResetMode(boolean refreshOnExecuteNull, boolean refreshOnToolEnabled){
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
        for(ILPCConfig config : vagueBlocksConfig.iterateConfigs())
            if(config instanceof ObjectListConfig.BlockListConfig blockListConfig)
                for(Block block : blockListConfig.set)
                    vagueBlocks.computeIfAbsent(block, v->new ArrayList<>()).add(blockListConfig.set);
    }
    static void setConfigBox(Box3i box){
        cornerPos1.setPos(box.pos1);
        cornerPos2.setPos(box.pos2);
        TTConfig.getPage().updateIfCurrent();
    }
    static Box3i getConfigBox(Box3i box){
        cornerPos1.getPos(box.pos1);
        cornerPos2.getPos(box.pos2);
        return box;
    }
    static Box3i getConfigBox(){return getConfigBox(new Box3i());}
}
