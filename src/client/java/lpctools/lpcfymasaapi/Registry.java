package lpctools.lpcfymasaapi;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;

@Deprecated
@SuppressWarnings({"UnusedReturnValue", "unused"})
public class Registry {
    public static boolean registerInGameEndMouseCallback(Registry.InGameEndMouse callback){
        return inGameEndMouseCallbacks.add(callback);
    }
    public static boolean unregisterInGameEndMouseCallback(Registry.InGameEndMouse callback){
        return inGameEndMouseCallbacks.remove(callback);
    }
    public static boolean registerWorldRenderStartCallback(WorldRenderEvents.Start callback){
        return worldRenderStartCallbacks.add(callback);
    }
    public static boolean unregisterWorldRenderStartCallback(WorldRenderEvents.Start callback){
        return worldRenderStartCallbacks.remove(callback);
    }
    public static boolean registerWorldRenderAfterSetupCallback(WorldRenderEvents.AfterSetup callback){
        return worldRenderAfterSetupCallbacks.add(callback);
    }
    public static boolean unregisterWorldRenderAfterSetupCallback(WorldRenderEvents.AfterSetup callback){
        return worldRenderAfterSetupCallbacks.remove(callback);
    }
    public static boolean registerWorldRenderBeforeEntitiesCallback(WorldRenderEvents.BeforeEntities callback){
        return worldRenderBeforeEntitiesCallbacks.add(callback);
    }
    public static boolean unregisterWorldRenderBeforeEntitiesCallback(WorldRenderEvents.BeforeEntities callback){
        return worldRenderBeforeEntitiesCallbacks.remove(callback);
    }
    public static boolean registerWorldRenderAfterEntitiesCallback(WorldRenderEvents.AfterEntities callback){
        return worldRenderAfterEntitiesCallbacks.add(callback);
    }
    public static boolean unregisterWorldRenderAfterEntitiesCallback(WorldRenderEvents.AfterEntities callback){
        return worldRenderAfterEntitiesCallbacks.remove(callback);
    }
    public static boolean registerWorldRenderBeforeDebugRenderCallback(WorldRenderEvents.DebugRender callback){
        return worldRenderBeforeDebugRenderCallbacks.add(callback);
    }
    public static boolean unregisterWorldRenderBeforeDebugRenderCallback(WorldRenderEvents.DebugRender callback){
        return worldRenderBeforeDebugRenderCallbacks.remove(callback);
    }
    public static boolean registerWorldRenderAfterTranslucentCallback(WorldRenderEvents.AfterTranslucent callback){
        return worldRenderAfterTranslucentCallbacks.add(callback);
    }
    public static boolean unregisterWorldRenderAfterTranslucentCallback(WorldRenderEvents.AfterTranslucent callback){
        return worldRenderAfterTranslucentCallbacks.remove(callback);
    }
    public static boolean registerWorldRenderLastCallback(WorldRenderEvents.Last callback){
        return worldRenderLastCallbacks.add(callback);
    }
    public static boolean unregisterWorldRenderLastCallback(WorldRenderEvents.Last callback){
        return worldRenderLastCallbacks.remove(callback);
    }
    public static boolean registerWorldRenderEndCallback(WorldRenderEvents.End callback){
        return worldRenderEndCallbacks.add(callback);
    }
    public static boolean unregisterWorldRenderEndCallback(WorldRenderEvents.End callback){
        return worldRenderEndCallbacks.remove(callback);
    }
    public static boolean registerClientChunkUnloadCallback(ClientChunkEvents.Unload callback){
        return clientChunkUnloadCallbacks.add(callback);
    }
    public static boolean unregisterClientChunkUnloadCallback(ClientChunkEvents.Unload callback){
        return clientChunkUnloadCallbacks.remove(callback);
    }
    public static boolean registerClientWorldChangeCallback(ClientWorldEvents.AfterClientWorldChange callback){
        return clientWorldChangeCallbacks.add(callback);
    }
    public static boolean unregisterClientWorldChangeCallback(ClientWorldEvents.AfterClientWorldChange callback){
        return clientWorldChangeCallbacks.remove(callback);
    }
    public static boolean registerClientWorldChunkSetBlockStateCallback(ClientWorldChunkSetBlockState callback){
        return clientWorldChunkSetBlockStateCallback.add(callback);
    }
    public static boolean unregisterClientWorldChunkSetBlockStateCallback(ClientWorldChunkSetBlockState callback){
        return clientWorldChunkSetBlockStateCallback.remove(callback);
    }
    public static boolean registerClientWorldChunkLightUpdatedCallback(ClientWorldChunkLightUpdated callback){
        return clientWorldChunkLightUpdatedCallback.add(callback);
    }
    public static boolean unregisterClientWorldChunkLightUpdatedCallback(ClientWorldChunkLightUpdated callback){
        return clientWorldChunkLightUpdatedCallback.remove(callback);
    }
    public static void runInGameEndMouseCallbacks(int button, int action, int mods){
        for(InGameEndMouse callback : inGameEndMouseCallbacks)
            callback.onInGameEndMouse(button, action, mods);
    }
    public static void runWorldRenderStartCallbacks(WorldRenderContext context){
        for(WorldRenderEvents.Start callback : worldRenderStartCallbacks)
            callback.onStart(context);
    }
    public static void runWorldRenderAfterSetupCallbacks(WorldRenderContext context){
        for(WorldRenderEvents.AfterSetup callback : worldRenderAfterSetupCallbacks)
            callback.afterSetup(context);
    }
    public static void runWorldRenderBeforeEntitiesCallbacks(WorldRenderContext context){
        for(WorldRenderEvents.BeforeEntities callback : worldRenderBeforeEntitiesCallbacks)
            callback.beforeEntities(context);
    }
    public static void runWorldRenderAfterEntitiesCallbacks(WorldRenderContext context){
        for(WorldRenderEvents.AfterEntities callback : worldRenderAfterEntitiesCallbacks)
            callback.afterEntities(context);
    }
    public static void runWorldRenderBeforeDebugRenderCallbacks(WorldRenderContext context){
        for(WorldRenderEvents.DebugRender callback : worldRenderBeforeDebugRenderCallbacks)
            callback.beforeDebugRender(context);
    }
    public static void runWorldRenderAfterTranslucentCallbacks(WorldRenderContext context){
        for(WorldRenderEvents.AfterTranslucent callback : worldRenderAfterTranslucentCallbacks)
            callback.afterTranslucent(context);
    }
    public static void runWorldRenderLastCallbacks(WorldRenderContext context){
        for(WorldRenderEvents.Last callback : worldRenderLastCallbacks)
            callback.onLast(context);
    }
    public static void runWorldRenderEndCallbacks(WorldRenderContext context){
        for(WorldRenderEvents.End callback : worldRenderEndCallbacks)
            callback.onEnd(context);
    }
    public static void runClientChunkUnloadCallbacks(ClientWorld world, WorldChunk chunk){
        for(ClientChunkEvents.Unload callback : clientChunkUnloadCallbacks)
            callback.onChunkUnload(world, chunk);
    }
    public static void runAfterClientWorldChange(MinecraftClient mc, ClientWorld world){
        for(ClientWorldEvents.AfterClientWorldChange callback : clientWorldChangeCallbacks)
            callback.afterWorldChange(mc, world);
    }
    public static void runClientWorldChunkSetBlockState(WorldChunk chunk, BlockPos pos, BlockState lastState, BlockState newState){
        for(ClientWorldChunkSetBlockState callback : clientWorldChunkSetBlockStateCallback)
            callback.onClientWorldChunkSetBlockState(chunk, pos, lastState, newState);
    }
    public static void runClientWorldChunkLightUpdated(WorldChunk chunk){
        for(ClientWorldChunkLightUpdated callback : clientWorldChunkLightUpdatedCallback)
            callback.onClientWorldChunkLightUpdated(chunk);
    }
    static void init(){
        WorldRenderEvents.START.register(Registry::runWorldRenderStartCallbacks);
        WorldRenderEvents.AFTER_SETUP.register(Registry::runWorldRenderAfterSetupCallbacks);
        WorldRenderEvents.BEFORE_ENTITIES.register(Registry::runWorldRenderBeforeEntitiesCallbacks);
        WorldRenderEvents.AFTER_ENTITIES.register(Registry::runWorldRenderAfterEntitiesCallbacks);
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(Registry::runWorldRenderBeforeDebugRenderCallbacks);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(Registry::runWorldRenderAfterTranslucentCallbacks);
        WorldRenderEvents.LAST.register(Registry::runWorldRenderLastCallbacks);
        WorldRenderEvents.END.register(Registry::runWorldRenderEndCallbacks);
        ClientChunkEvents.CHUNK_UNLOAD.register(Registry::runClientChunkUnloadCallbacks);
        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register(Registry::runAfterClientWorldChange);
    }
    @NotNull private static final LinkedHashSet<Registry.InGameEndMouse> inGameEndMouseCallbacks = new LinkedHashSet<>();
    @NotNull private static final LinkedHashSet<ClientTickEvents.StartTick> startClientTickCallbacks = new LinkedHashSet<>();
    @NotNull private static final LinkedHashSet<ClientTickEvents.EndTick> endClientTickCallbacks = new LinkedHashSet<>();
    @NotNull private static final LinkedHashSet<WorldRenderEvents.Start> worldRenderStartCallbacks = new LinkedHashSet<>();
    @NotNull private static final LinkedHashSet<WorldRenderEvents.AfterSetup> worldRenderAfterSetupCallbacks = new LinkedHashSet<>();
    @NotNull private static final LinkedHashSet<WorldRenderEvents.BeforeEntities> worldRenderBeforeEntitiesCallbacks = new LinkedHashSet<>();
    @NotNull private static final LinkedHashSet<WorldRenderEvents.AfterEntities> worldRenderAfterEntitiesCallbacks = new LinkedHashSet<>();
    @NotNull private static final LinkedHashSet<WorldRenderEvents.DebugRender> worldRenderBeforeDebugRenderCallbacks = new LinkedHashSet<>();
    @NotNull private static final LinkedHashSet<WorldRenderEvents.AfterTranslucent> worldRenderAfterTranslucentCallbacks = new LinkedHashSet<>();
    @NotNull private static final LinkedHashSet<WorldRenderEvents.Last> worldRenderLastCallbacks = new LinkedHashSet<>();
    @NotNull private static final LinkedHashSet<WorldRenderEvents.End> worldRenderEndCallbacks = new LinkedHashSet<>();
    @NotNull private static final LinkedHashSet<ClientChunkEvents.Unload> clientChunkUnloadCallbacks = new LinkedHashSet<>();
    @NotNull private static final LinkedHashSet<ClientWorldEvents.AfterClientWorldChange> clientWorldChangeCallbacks = new LinkedHashSet<>();
    @NotNull private static final LinkedHashSet<ClientWorldChunkSetBlockState> clientWorldChunkSetBlockStateCallback = new LinkedHashSet<>();
    @NotNull private static final LinkedHashSet<ClientWorldChunkLightUpdated> clientWorldChunkLightUpdatedCallback = new LinkedHashSet<>();
    
    public static boolean isClientChunkSetBlockStateCallbackEmpty(){return clientWorldChunkSetBlockStateCallback.isEmpty();}
    public interface InGameEndMouse {
        void onInGameEndMouse(int button, int action, int mods);
    }
    public interface ClientWorldChunkSetBlockState {//at RETURN
        void onClientWorldChunkSetBlockState(WorldChunk chunk, BlockPos pos, BlockState lastState, BlockState newState);
    }
    public interface ClientWorldChunkLightUpdated{
        void onClientWorldChunkLightUpdated(WorldChunk chunk);
    }
}
