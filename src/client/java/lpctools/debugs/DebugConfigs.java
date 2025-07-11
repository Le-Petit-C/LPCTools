package lpctools.debugs;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import lpctools.LPCTools;
import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.Registries;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.BooleanConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.HotkeyConfig;
import lpctools.lpcfymasaapi.configbutton.uniqueConfigs.*;
import lpctools.lpcfymasaapi.gl.MaskLayer;
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
import static lpctools.util.DataUtils.*;

@SuppressWarnings("unused")
public class DebugConfigs {
    public static final LPCConfigList debugs = new LPCConfigList(LPCTools.page, "debugs");
    static {listStack.push(debugs);}
    public static final BooleanConfig renderDebugShapes = addBooleanConfig(
        "renderDebugShapes", false, DebugConfigs::renderDebugShapesValueRefreshCallback);
    public static final BooleanConfig displayClickSlotArguments = addBooleanConfig("displayClickSlotArguments", false);
    public static final InstancedRenderTest instancedRenderTest = addConfig(new InstancedRenderTest(debugs));
    public static final HotkeyConfig keyActDebug = addHotkeyConfig("keyActDebug", "", DebugConfigs::keyActDebugCallback);
    public static final BooleanConfig showExecuteTime = addBooleanConfig("showExecuteTime", false);
    public static final HotkeyConfig getBlockStateHotkey = addHotkeyConfig("getBlockStateHotkey", "", DebugConfigs::getBlockStateHotkeyCallback);
    public static final BooleanConfig briefBlockState = addBooleanConfig("briefBlockState", true);
    public static final MandelbrotSetRender mandelbrotSetRender = addConfig(new MandelbrotSetRender(debugs));
    public static final BooleanHotkeyThirdListConfig booleanHotkeyThirdListTest =
        addBooleanHotkeyThirdListConfig(debugs, "booleanHotkeyThirdListTest", false, false, null, DebugConfigs::booleanHotkeyThirdListTestCallback, false);
    public static final ButtonConfig buttonConfigTest = addButtonConfig(booleanHotkeyThirdListTest, "button", DebugConfigs::buttonConfigTestCallback);
    private static final ImmutableList<MutableConfig.ConfigAllocator<?>> configSuppliers =ImmutableList.of(
        new MutableConfig.ConfigAllocator<>("button", ButtonConfig::new),
        new MutableConfig.ConfigAllocator<>("buttonHotkey", (parent, key)->new ButtonHotkeyConfig(parent, key, null, null)),
        new MutableConfig.ConfigAllocator<>("", (parent, key)->new MutableConfig(parent, key, getConfigSuppliers(), null)));
    public static final MutableConfig MUTABLE_CONFIG_TEST = booleanHotkeyThirdListTest.
        addConfig(new MutableConfig(booleanHotkeyThirdListTest, "mutable", configSuppliers, null));
    private static ImmutableList<MutableConfig.ConfigAllocator<?>> getConfigSuppliers(){return configSuppliers;}
    static {Registries.ON_SCREEN_CHANGED.register(newScreen -> buttonConfigTest.buttonName = null);}
    static {listStack.pop();}
    
    private static void booleanHotkeyThirdListTestCallback(){
        notifyPlayer("Value changed! current is " + booleanHotkeyThirdListTest.getBooleanValue(), false);
    }
    
    private static void buttonConfigTestCallback(ButtonBase button, int mouseButton){
        notifyPlayer("❤Ahh❤It's❤Button❤" + mouseButton + "❤", false);
        if(buttonConfigTest.buttonName == null) buttonConfigTest.buttonName = Text.translatable("lpctools.mew~").getString();
        else buttonConfigTest.buttonName = "❤" + buttonConfigTest.buttonName + "❤";
    }
    
    private static boolean keyActDebugCallback(KeyAction action, IKeybind bind){
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if(player == null) return false;
        player.setPitch(0);
        player.setYaw(0);
        return true;
    }
    private static void renderDebugShapes(WorldRenderContext context){
        try(MaskLayer layer = new MaskLayer()){
            layer.enableBlend().disableCullFace().enableDepthTest();
            RenderTest1.render(context, layer);
            RenderTest2.render(context, layer);
        }
    }
    private static final WorldRenderEvents.Last debugShapesRenderer = DebugConfigs::renderDebugShapes;
    private static void renderDebugShapesValueRefreshCallback(){
        Registries.WORLD_RENDER_LAST.register(debugShapesRenderer, renderDebugShapes.getAsBoolean());
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
