package lpctools.debugs;

import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import lpctools.lpcfymasaapi.Registry;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.BooleanConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.HotkeyConfig;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import static lpctools.generic.GenericUtils.mayMobSpawnOn;
import static lpctools.lpcfymasaapi.LPCConfigStatics.*;

public class DebugConfigs {
    public static BooleanConfig renderDebugShapes;
    public static BooleanConfig displayClickSlotArguments;
    public static HotkeyConfig keyActDebug;
    public static BooleanConfig showExecuteTime;
    public static HotkeyConfig getBlockStateHotkey;
    public static BooleanConfig briefBlockState;
    public static void init(){
        RenderTest.init();
        renderDebugShapes = addBooleanConfig(
                "renderDebugShapes", false, DebugConfigs::renderDebugShapesValueRefreshCallback);
        displayClickSlotArguments = addBooleanConfig("displayClickSlotArguments", false);
        keyActDebug = addHotkeyConfig("keyActDebug", "", (action, bind)->{
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if(player == null) return false;
            player.setPitch(0);
            player.setYaw(0);
            return true;
        });
        showExecuteTime = addBooleanConfig("showExecuteTime", false);
        getBlockStateHotkey = addHotkeyConfig("getBlockStateHotkey", "", DebugConfigs::getBlockStateHotkeyCallback);
        briefBlockState = addBooleanConfig("briefBlockState", true);
    }
    
    private static void renderDebugShapes(WorldRenderContext context){
        RenderTest1.render(context);
        RenderTest2.render(context);
    }
    private static final WorldRenderEvents.Last debugShapesRenderer = DebugConfigs::renderDebugShapes;
    private static void renderDebugShapesValueRefreshCallback(){
        if(renderDebugShapes.getAsBoolean())
            Registry.registerWorldRenderLastCallback(debugShapesRenderer);
        else Registry.unregisterWorldRenderLastCallback(debugShapesRenderer);
    }
    private static boolean getBlockStateHotkeyCallback(KeyAction action, IKeybind keybind){
        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld world = client.world;
        ClientPlayerEntity player = client.player;
        if(world == null || player == null) return false;
        BlockPos pos = player.getBlockPos();
        BlockState state = world.getBlockState(pos);
        BlockState finalState;
        if(state.isAir()) finalState = world.getBlockState(pos.down());
        else finalState = state;
        if(briefBlockState.getAsBoolean()){
            String msg = "isOpaque:" + finalState.isOpaque() + '\n' +
                "isTransparent:" + finalState.isTransparent() + '\n' +
                "isOpaqueFullCube:" + finalState.isOpaqueFullCube() + '\n' +
                "mayMobSpawnOn:" + mayMobSpawnOn(finalState) + '\n';
            player.sendMessage(Text.of(msg), false);
        }
        else player.sendMessage(Text.of(finalState.toString()), false);
        return true;
    }
}
