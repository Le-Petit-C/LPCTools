package lpctools.debugs;

import com.google.common.collect.ImmutableMap;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import lpctools.LPCTools;
import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.Registries;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.BooleanConfig;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.HotkeyConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.*;
import lpctools.lpcfymasaapi.gl.MaskLayer;
import lpctools.lpcfymasaapi.interfaces.ILPCUniqueConfigBase;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

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
        addBooleanHotkeyThirdListConfig(debugs, "booleanHotkeyThirdListTest", false, null, DebugConfigs::booleanHotkeyThirdListTestCallback);
    public static final UniqueIntegerConfig uniqueIntegerConfigTest = booleanHotkeyThirdListTest.addConfig(new UniqueIntegerConfig(booleanHotkeyThirdListTest, "uniqueIntegerConfigTest", 0));
    public static final ButtonConfig buttonConfigTest = addButtonConfig(booleanHotkeyThirdListTest, "button", DebugConfigs::buttonConfigTestCallback);
    private static final ImmutableMap<String, BiFunction<MutableConfig<ILPCUniqueConfigBase>, String, ILPCUniqueConfigBase>> configSuppliers =ImmutableMap.of(
        "button", (parent, key)->new ButtonConfig(parent, key){@Override public @NotNull String getFullTranslationKey() {return "lpctools.configs.debugs.booleanHotkeyThirdListTest.mutable.button";}},
        "buttonHotkey", (parent, key)->new ButtonHotkeyConfig(parent, key, null, null){@Override public @NotNull String getFullTranslationKey() {return "lpctools.configs.debugs.booleanHotkeyThirdListTest.mutable.buttonHotkey";}},
        "mutable", (parent, key)->new MutableConfig<>(parent, key, booleanHotkeyThirdListTest.getFullTranslationKey() + ".mutable", getConfigSuppliers(), null){@Override public @NotNull String getFullTranslationKey() {return "lpctools.configs.debugs.booleanHotkeyThirdListTest.mutable.mutable";}}
    );
    public static final MutableConfig<ILPCUniqueConfigBase> MUTABLE_CONFIG_TEST = booleanHotkeyThirdListTest.addConfig(new MutableConfig<>(booleanHotkeyThirdListTest, "mutable", booleanHotkeyThirdListTest.getFullTranslationKey() + ".mutable", configSuppliers, null));
    private static ImmutableMap<String, BiFunction<MutableConfig<ILPCUniqueConfigBase>, String, ILPCUniqueConfigBase>> getConfigSuppliers(){return configSuppliers;}
    static {Registries.ON_SCREEN_CHANGED.register(newScreen -> buttonConfigTest.buttonName = null);}
    static {listStack.pop();}
    static {
        debugs.addConfig(TimeTest.timeTest);
    }
    
    private static void booleanHotkeyThirdListTestCallback(){
        if(booleanHotkeyThirdListTest.isExpanded())
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
