package lpctools.tools.furnaceMaintainer;

import lpctools.lpcfymasaapi.configButtons.derivedConfigs.ReachDistanceConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.*;
import lpctools.tools.ToolConfigs;
import lpctools.tools.ToolUtils;
import lpctools.util.DataUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractFurnaceScreen;
import net.minecraft.client.gui.screen.ingame.HopperScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

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
    private static void switchCallback(){
        if(FMConfig.getBooleanValue() && dataInstance != null){
            if(runner == null)
                runner = new FurnaceMaintainerRunner();
        }
        else {
            if(runner != null){
                runner.close();
                runner = null;
            }
        }
    }
    private static void detectFurnacesCallback(){
        if(dataInstance == null) dataInstance = new DataInstance();
        dataInstance.retestFurnaces();
    }
    private static void clearMarksCallback(){
        if(dataInstance != null) {
            dataInstance.close();
            dataInstance = null;
        }
    }
    private static void clearClientScreen(Screen screen){
        var mc = MinecraftClient.getInstance();
        if(mc.currentScreen == screen) mc.setScreen(null);
    }
    public static boolean screenCallback(Screen screen) {
        if(!FMConfig.getBooleanValue() || dataInstance == null || runner == null || runner.lastInteractedPos == null) return false;
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        ClientPlayerInteractionManager itm = client.interactionManager;
        if(player == null || itm == null) {
            FMConfig.setBooleanValue(false);
            return false;
        }
        // DataUtils.clientMessage(String.valueOf(player.currentScreenHandler.syncId), false);
        boolean operated;
        if(screen instanceof AbstractFurnaceScreen<?> screen1) {
            itm.clickSlot(screen1.getScreenHandler().syncId, 0, 0, SlotActionType.QUICK_MOVE, player);
            operated = true;
        }
        else if(screen instanceof HopperScreen screen1) {
            for(int i = 0; i < 5; ++i) itm.clickSlot(screen1.getScreenHandler().syncId, i, 0, SlotActionType.QUICK_MOVE, player);
            operated = true;
        }
        else operated = false;
        if(operated) {
            clearClientScreen(screen);
            dataInstance.highlightInstance.mark(runner.lastInteractedPos, null);
            runner.lastInteractedPos = null;
        }
        return operated;
    }
    public static void onBlockInteracted() {
        if(!FMConfig.getBooleanValue()) return;
        if(isFMInteracting) return;
        FMConfig.setBooleanValue(false);
        DataUtils.clientMessage(Text.translatable("lpctools.configs.tools.FM.unexpectedInteractBlock"), true);
    }
}
