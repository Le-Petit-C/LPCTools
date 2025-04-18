package lpctools.lpcfymasaapi;

import fi.dy.masa.malilib.event.RenderEventHandler;
import fi.dy.masa.malilib.interfaces.IRenderDispatcher;
import fi.dy.masa.malilib.interfaces.IRenderer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.LinkedHashSet;
import java.util.function.Consumer;

@SuppressWarnings({"UnusedReturnValue", "unused"})
public class Registry {
    public static boolean registerEndClientTickCallback(ClientTickEvents.EndTick callback){
        return endClientTickCallbacks.add(callback);
    }
    public static boolean unregisterEndClientTickCallback(ClientTickEvents.EndTick callback){
        return endClientTickCallbacks.remove(callback);
    }
    public static boolean registerInGameEndMouseCallback(Registry.InGameEndMouse callback){
        return inGameEndMouseCallbacks.add(callback);
    }
    public static boolean unregisterInGameEndMouseCallback(Registry.InGameEndMouse callback){
        return inGameEndMouseCallbacks.remove(callback);
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
    public static boolean registerMalilibRenderCallback(IRenderer callback){
        return malilibRenderCallbacks.add(callback);
    }
    public static boolean unregisterMalilibRenderCallback(IRenderer callback){
        return malilibRenderCallbacks.remove(callback);
    }
    public static boolean registerClientChunkLoadCallbacks(ClientChunkEvents.Load callback){
        return clientChunkLoadCallbacks.add(callback);
    }
    public static boolean unregisterClientChunkLoadCallbacks(ClientChunkEvents.Load callback){
        return clientChunkLoadCallbacks.remove(callback);
    }
    public static boolean registerClientChunkUnloadCallbacks(ClientChunkEvents.Unload callback){
        return clientChunkUnloadCallbacks.add(callback);
    }
    public static boolean unregisterClientChunkUnloadCallbacks(ClientChunkEvents.Unload callback){
        return clientChunkUnloadCallbacks.remove(callback);
    }
    public static boolean registerClientWorldChangeCallbacks(ClientWorldEvents.AfterClientWorldChange callback){
        return clientWorldChangeCallbacks.add(callback);
    }
    public static boolean unregisterClientWorldChangeCallbacks(ClientWorldEvents.AfterClientWorldChange callback){
        return clientWorldChangeCallbacks.add(callback);
    }
    public static void runEndClientTickCallbacks(MinecraftClient client){
        for(ClientTickEvents.EndTick callback : endClientTickCallbacks)
            callback.onEndTick(client);
    }
    public static void runInGameEndMouseCallbacks(int button, int action, int mods){
        for(InGameEndMouse callback : inGameEndMouseCallbacks)
            callback.onInGameEndMouse(button, action, mods);
    }
    public static void runWorldRenderLastCallbacks(WorldRenderContext context){
        for(WorldRenderEvents.Last callback : worldRenderLastCallbacks)
            callback.onLast(context);
    }
    public static void runWorldRenderEndCallbacks(WorldRenderContext context){
        for(WorldRenderEvents.End callback : worldRenderEndCallbacks)
            callback.onEnd(context);
    }
    public static void runClientChunkLoadCallbacks(ClientWorld world, WorldChunk chunk){
        for(ClientChunkEvents.Load callback : clientChunkLoadCallbacks)
            callback.onChunkLoad(world, chunk);
    }
    public static void runClientChunkUnloadCallbacks(ClientWorld world, WorldChunk chunk){
        for(ClientChunkEvents.Unload callback : clientChunkUnloadCallbacks)
            callback.onChunkUnload(world, chunk);
    }
    public static void runAfterClientWorldChange(MinecraftClient mc, ClientWorld world){
        for(ClientWorldEvents.AfterClientWorldChange callback : clientWorldChangeCallbacks)
            callback.afterWorldChange(mc, world);
    }
    static void init(){
        ClientTickEvents.END_CLIENT_TICK.register(Registry::runEndClientTickCallbacks);
        WorldRenderEvents.LAST.register(Registry::runWorldRenderLastCallbacks);
        WorldRenderEvents.END.register(Registry::runWorldRenderEndCallbacks);
        MalilibRenderer renderer = MalilibRenderer.getInstance();
        IRenderDispatcher handler = RenderEventHandler.getInstance();
        handler.registerWorldLastRenderer(renderer);
        handler.registerGameOverlayRenderer(renderer);
        handler.registerTooltipLastRenderer(renderer);
        handler.registerWorldPostDebugRenderer(renderer);
        handler.registerWorldPreWeatherRenderer(renderer);
        ClientChunkEvents.CHUNK_LOAD.register(Registry::runClientChunkLoadCallbacks);
        ClientChunkEvents.CHUNK_UNLOAD.register(Registry::runClientChunkUnloadCallbacks);
        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register(Registry::runAfterClientWorldChange);
    }
    private static class MalilibRenderer implements IRenderer{
        private static final MalilibRenderer renderer = new MalilibRenderer();
        private MalilibRenderer(){}
        public static MalilibRenderer getInstance() {return renderer;}
        @Override public void onRenderGameOverlayLastDrawer(DrawContext drawContext, float partialTicks, Profiler profiler, MinecraftClient mc) {
            for(IRenderer renderer : malilibRenderCallbacks) renderer.onRenderGameOverlayLastDrawer(drawContext, partialTicks, profiler, mc);
        }
        @Override public void onRenderGameOverlayPostAdvanced(DrawContext drawContext, float partialTicks, Profiler profiler, MinecraftClient mc) {
            for(IRenderer renderer : malilibRenderCallbacks) renderer.onRenderGameOverlayPostAdvanced(drawContext, partialTicks, profiler, mc);
        }
        @Override public void onRenderGameOverlayPost(DrawContext drawContext) {
            for(IRenderer renderer : malilibRenderCallbacks) renderer.onRenderGameOverlayPost(drawContext);
        }
        @Override public void onRenderWorldPostDebugRender(MatrixStack matrices, Frustum frustum, VertexConsumerProvider.Immediate immediate, Vec3d camera, Profiler profiler) {
            for(IRenderer renderer : malilibRenderCallbacks) renderer.onRenderWorldPostDebugRender(matrices, frustum, immediate, camera, profiler);
        }
        @Override public void onRenderWorldPreWeather(Framebuffer fb, Matrix4f posMatrix, Matrix4f projMatrix, Frustum frustum, Camera camera, Fog fog, BufferBuilderStorage buffers, Profiler profiler) {
            for(IRenderer renderer : malilibRenderCallbacks) renderer.onRenderWorldPreWeather(fb, posMatrix, projMatrix, frustum, camera, fog, buffers, profiler);
        }
        @Override public void onRenderWorldLastAdvanced(Framebuffer fb, Matrix4f posMatrix, Matrix4f projMatrix, Frustum frustum, Camera camera, Fog fog, BufferBuilderStorage buffers, Profiler profiler) {
            for(IRenderer renderer : malilibRenderCallbacks) renderer.onRenderWorldLastAdvanced(fb, posMatrix, projMatrix, frustum, camera, fog, buffers, profiler);
        }
        @Override public void onRenderWorldLast(Matrix4f posMatrix, Matrix4f projMatrix) {
            for(IRenderer renderer : malilibRenderCallbacks) renderer.onRenderWorldLast(posMatrix, projMatrix);
        }
        @Override public void onRenderTooltipComponentInsertFirst(Item.TooltipContext context, ItemStack stack, Consumer<Text> list) {
            for(IRenderer renderer : malilibRenderCallbacks) renderer.onRenderTooltipComponentInsertFirst(context, stack, list);
        }
        @Override public void onRenderTooltipComponentInsertMiddle(Item.TooltipContext context, ItemStack stack, Consumer<Text> list) {
            for(IRenderer renderer : malilibRenderCallbacks) renderer.onRenderTooltipComponentInsertMiddle(context, stack, list);
        }
        @Override public void onRenderTooltipComponentInsertLast(Item.TooltipContext context, ItemStack stack, Consumer<Text> list) {
            for(IRenderer renderer : malilibRenderCallbacks) renderer.onRenderTooltipComponentInsertLast(context, stack, list);
        }
        @Override public void onRenderTooltipLast(DrawContext drawContext, ItemStack stack, int x, int y) {
            for(IRenderer renderer : malilibRenderCallbacks) renderer.onRenderTooltipLast(drawContext, stack, x, y);
        }
    }
    @NotNull private static final LinkedHashSet<Registry.InGameEndMouse> inGameEndMouseCallbacks = new LinkedHashSet<>();
    @NotNull private static final LinkedHashSet<ClientTickEvents.EndTick> endClientTickCallbacks = new LinkedHashSet<>();
    @NotNull private static final LinkedHashSet<WorldRenderEvents.Last> worldRenderLastCallbacks = new LinkedHashSet<>();
    @NotNull private static final LinkedHashSet<WorldRenderEvents.End> worldRenderEndCallbacks = new LinkedHashSet<>();
    @NotNull private static final LinkedHashSet<IRenderer> malilibRenderCallbacks = new LinkedHashSet<>();
    @NotNull private static final LinkedHashSet<ClientChunkEvents.Load> clientChunkLoadCallbacks = new LinkedHashSet<>();
    @NotNull private static final LinkedHashSet<ClientChunkEvents.Unload> clientChunkUnloadCallbacks = new LinkedHashSet<>();
    @NotNull private static final LinkedHashSet<ClientWorldEvents.AfterClientWorldChange> clientWorldChangeCallbacks = new LinkedHashSet<>();

    public interface InGameEndMouse {
        void onInGameEndMouse(int button, int action, int mods);
    }
}
