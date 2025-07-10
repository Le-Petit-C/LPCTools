package lpctools.tools.tilingTool;

import lpctools.compact.CompactMain;
import lpctools.compact.litematica.LitematicaMethods;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.BlockPosConfig;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.LimitOperationSpeedConfig;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.ReachDistanceConfig;
import lpctools.lpcfymasaapi.configbutton.uniqueConfigs.BooleanHotkeyThirdListConfig;
import lpctools.lpcfymasaapi.configbutton.uniqueConfigs.ButtonHotkeyConfig;
import lpctools.tools.ToolConfigs;
import lpctools.util.data.Box3i;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;
import static lpctools.tools.ToolUtils.*;
import static lpctools.tools.tilingTool.TilingToolData.*;
import static lpctools.util.DataUtils.notifyPlayer;

//TODO:投影选区设置模式：仅替换配置坐标/替换配置坐标并刷新储存/仅刷新储存
//TODO:模糊方块配置

public class TilingTool {
    public static final BooleanHotkeyThirdListConfig TTConfig = new BooleanHotkeyThirdListConfig(ToolConfigs.toolConfigs, "TT", TilingTool::switchCallback);
    static {setLPCToolsToggleText(TTConfig);}
    static {listStack.push(TTConfig);}
    @SuppressWarnings("unused")
    public static final ButtonHotkeyConfig refreshButton = addButtonHotkeyConfig("refresh", null, TilingTool::refreshCallback);
    public static final ReachDistanceConfig reachDistance = addReachDistanceConfig();
    public static final LimitOperationSpeedConfig limitOperationSpeed = addLimitOperationSpeedConfig(false, 1);
    public static final BlockPosConfig cornerPos1 = addBlockPosConfig("cornerPos1", BlockPos.ORIGIN, false);
    public static final BlockPosConfig cornerPos2 = addBlockPosConfig("cornerPos2", BlockPos.ORIGIN, false);
    @SuppressWarnings("unused")
    public static final ButtonHotkeyConfig setByLitematicaButton = addButtonHotkeyConfig("setByLitematica", null, TilingTool::setByLitematica);
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
    private static void refreshCallback(){refresh(getConfigBox());}
    private static void setByLitematica(){
        if(CompactMain.getLitematicaInstance() instanceof LitematicaMethods methods){
            Box3i box = methods.getSelectionBox();
            if(box != null) setConfigBox(box);
            else notifyPlayer(Text.translatable("lpctools.compact.litematica.cantGetSelectionBox"), true);
        }
        else notifyPlayer(Text.translatable("lpctools.compact.missingLitematica"), true);
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
