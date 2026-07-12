package lpctools.tools.furnaceMaintainer;

import lpctools.lpcfymasaapi.configButtons.derivedConfigs.ReachDistanceConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.*;
import lpctools.tools.ToolConfigs;
import lpctools.tools.ToolUtils;
import lpctools.util.DataUtils;
import net.minecraft.network.chat.Component;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;
import static lpctools.tools.furnaceMaintainer.FurnaceMaintainerData.*;

public class FurnaceMaintainer {
    public static final BooleanHotkeyThirdListConfig FMConfig = new BooleanHotkeyThirdListConfig(ToolConfigs.toolConfigs, "FM", FurnaceMaintainer::switchCallback);
    static {ToolUtils.setLPCToolsToggleText(FMConfig);}
    static {listStack.push(FMConfig);}
    public static final ReachDistanceConfig reachDistance = addReachDistanceConfig();
    public static final UniqueDoubleConfig operationSpeedLimit = addConfigEx(l->new UniqueDoubleConfig(l, "operationSpeedLimit", 1, 0, 1, null));
    @SuppressWarnings("unused")
    public static final ButtonHotkeyConfig detectFurnaces = addButtonHotkeyConfig("retestFurnaces", null, FurnaceMaintainer::detectFurnacesCallback);
    @SuppressWarnings("unused")
    public static final ButtonHotkeyConfig clearMarks = addButtonHotkeyConfig("clearMarks", null, FurnaceMaintainer::clearMarksCallback);
    public static final UniqueColorConfig markingColor = addConfigEx(l->
        new UniqueColorConfig(l, "markingColor", 0x7fff7f00, applyToDataInstanceCallback(DataInstance::refreshColor)));
    public static final UniqueBooleanConfig includesHopperAbove = addConfigEx(l->new UniqueBooleanConfig(l, "includesHopperAbove", true, null));
    public static final UniqueBooleanConfig renderXRays = addConfigEx(l->new UniqueBooleanConfig(l, "renderXRays", false, applyToDataInstanceCallback(DataInstance::refreshRenderXRays)));
    public static final UniqueBooleanConfig useCullFace = addConfigEx(l->new UniqueBooleanConfig(l, "useCullFace", true, applyToDataInstanceCallback(DataInstance::refreshUseCullFace)));
    
    static {listStack.pop();}
    private static void switchCallback() {
        if(FMConfig.getBooleanValue()) {
            if(runner == null)
                runner = new FurnaceMaintainerRunner();
            runner.registerAll(true);
        }
        else {
            if(runner != null) {
                runner.close();
                runner = null;
            }
        }
    }
    private static void detectFurnacesCallback() {
        if(dataInstance == null) dataInstance = new DataInstance();
        dataInstance.retestFurnaces();
    }
    private static void clearMarksCallback(){
        if(dataInstance != null) {
            dataInstance.close();
            dataInstance = null;
        }
    }
    public static void onBlockInteracted() {
        if(!FMConfig.getBooleanValue()) return;
        if(isFMInteracting) return;
        FMConfig.setBooleanValue(false);
        DataUtils.clientMessage(Component.translatable("lpctools.configs.tools.FM.unexpectedInteractBlock"), true);
    }
}
