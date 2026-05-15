package lpctools.lpcfymasaapi;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import fi.dy.masa.malilib.event.RenderEventHandler;
import fi.dy.masa.malilib.interfaces.IRangeChangeListener;
import fi.dy.masa.malilib.interfaces.IRenderer;
import fi.dy.masa.malilib.render.GuiContext;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;

import java.util.function.Consumer;

public class Registries {
    public static final UnregistrableRegistry<ClientLevelEvents.AfterClientLevelChange> AFTER_CLIENT_WORLD_CHANGE = new UnregistrableRegistry<>(
        callbacks->(client, world)->callbacks.forEach(screen->screen.afterLevelChange(client, world)));
    public static final UnregistrableRegistry<ScreenChangeCallback> ON_SCREEN_CHANGED = new UnregistrableRegistry<>(
        callbacks->newScreen->callbacks.forEach(screen->screen.onScreenChanged(newScreen)));
    public static final UnregistrableRegistry<ClientTickEvents.StartTick> START_CLIENT_TICK = new UnregistrableRegistry<>(
        callbacks->mc->callbacks.forEach(screen->screen.onStartTick(mc)));
    public static final UnregistrableRegistry<ClientTickEvents.EndTick> END_CLIENT_TICK = new UnregistrableRegistry<>(
        callbacks->mc->callbacks.forEach(screen->screen.onEndTick(mc)));
    public static final UnregistrableRegistry<ClientChunkEvents.Load> CLIENT_CHUNK_LOAD = new UnregistrableRegistry<>(
        callbacks->(world, chunk)->callbacks.forEach(screen->screen.onChunkLoad(world, chunk)));
    public static final UnregistrableRegistry<ClientChunkEvents.Unload> CLIENT_CHUNK_UNLOAD = new UnregistrableRegistry<>(
        callbacks->(world, chunk)->callbacks.forEach(screen->screen.onChunkUnload(world, chunk)));
    public static final UnregistrableRegistry<WorldPreMainRender> PRE_MAIN = new UnregistrableRegistry<>(
        callbacks->context->callbacks.forEach(callback->callback.onRenderWorldPreMain(context)));
    public static final UnregistrableRegistry<LevelRenderEvents.AfterBlockOutlineExtraction> AFTER_BLOCK_OUTLINE_EXTRACTION = new UnregistrableRegistry<>(
        callbacks->(context, result)->callbacks.forEach(callback->callback.afterBlockOutlineExtraction(context, result)));
    public static final UnregistrableRegistry<LevelRenderEvents.EndExtraction> END_EXTRACTION = new UnregistrableRegistry<>(
        callbacks->(context)->callbacks.forEach(callback->callback.endExtraction(context)));
    public static final UnregistrableRegistry<LevelRenderEvents.StartMain> START_MAIN = new UnregistrableRegistry<>(
        callbacks->(context)->callbacks.forEach(callback->callback.startMain(context)));
    public static final UnregistrableRegistry<LevelRenderEvents.BeforeEntities> BEFORE_ENTITIES = new UnregistrableRegistry<>(
        callbacks->(context)->callbacks.forEach(callback->callback.beforeEntities(context)));
    public static final UnregistrableRegistry<LevelRenderEvents.AfterEntities> AFTER_ENTITIES = new UnregistrableRegistry<>(
        callbacks->(context)->callbacks.forEach(callback->callback.afterEntities(context)));
    public static final UnregistrableRegistry<LevelRenderEvents.DebugRender> BEFORE_DEBUG_RENDER = new UnregistrableRegistry<>(
        callbacks->(context)->callbacks.forEach(callback->callback.beforeDebugRender(context)));
    public static final UnregistrableRegistry<LevelRenderEvents.BeforeTranslucent> BEFORE_TRANSLUCENT = new UnregistrableRegistry<>(
        callbacks->(context)->callbacks.forEach(callback->callback.beforeTranslucent(context)));
    public static final UnregistrableRegistry<LevelRenderEvents.BeforeBlockOutline> BEFORE_BLOCK_OUTLINE = new UnregistrableRegistry<>(
        callbacks->(context, outlineRenderState)->{
            boolean shouldRender = true;
            for(var callback : callbacks) if(!callback.beforeBlockOutline(context, outlineRenderState)) shouldRender = false;
            return shouldRender;
        });
    public static final UnregistrableRegistry<LevelRenderEvents.EndMain> END_MAIN = new UnregistrableRegistry<>(
        callbacks->(context)->callbacks.forEach(callback->callback.endMain(context)));
    public static final UnregistrableRegistry<ClientWorldChunkSetBlockState> CLIENT_WORLD_CHUNK_SET_BLOCK_STATE = new UnregistrableRegistry<>(
        callbacks->(chunk, pos, lastState, newState)->callbacks.forEach(screen->screen.onClientWorldChunkSetBlockState(chunk, pos, lastState, newState)));
    public static final UnregistrableRegistry<GameOverlayRender> MASA_RENDER_GAME_OVERLAY = new UnregistrableRegistry<>(
        callbacks->(a, b, c)->callbacks.forEach(renderer->renderer.renderGameOverlay(a, b, c)));
    public static final UnregistrableRegistry<WorldPreWeatherRender> MASA_RENDER_WORLD_PRE_WEATHER = new UnregistrableRegistry<>(
        callbacks->c->callbacks.forEach(renderer->renderer.onRenderWorldPreWeather(c)));
    public static final UnregistrableRegistry<WorldLastRender> MASA_WORLD_RENDER_LAST = new UnregistrableRegistry<>(
        callbacks->c->callbacks.forEach(renderer->renderer.onLast(c)));
    public static final UnregistrableRegistry<TooltipComponentInsertFirstRender> MASA_RENDER_TOOLTIP_COMPONENT_INSERTION_FIRST = new UnregistrableRegistry<>(
        callbacks->(c, s, l)->callbacks.forEach(renderer->renderer.onRenderTooltipComponentInsertFirst(c, s, l)));
    public static final UnregistrableRegistry<TooltipComponentInsertMiddleRender> MASA_RENDER_TOOLTIP_COMPONENT_INSERTION_MIDDLE = new UnregistrableRegistry<>(
        callbacks->(c, s, l)->callbacks.forEach(renderer->renderer.onRenderTooltipComponentInsertMiddle(c, s, l)));
    public static final UnregistrableRegistry<TooltipComponentInsertLastRender> MASA_RENDER_TOOLTIP_COMPONENT_INSERTION_LAST = new UnregistrableRegistry<>(
        callbacks->(c, s, l)->callbacks.forEach(renderer->renderer.onRenderTooltipComponentInsertLast(c, s, l)));
    public static final UnregistrableRegistry<TooltipLastRender> MASA_RENDER_TOOLTIP_LAST = new UnregistrableRegistry<>(
        callbacks->(c, s, x, y)->callbacks.forEach(renderer->renderer.onRenderTooltipLast(c, s, x, y)));
    public static final UnregistrableRegistry<ClientWorldChunkLightUpdated> CLIENT_CHUNK_LIGHT_LOAD = new UnregistrableRegistry<>(
        callbacks->(world, chunk)->callbacks.forEach(callback->callback.onClientWorldChunkLightUpdated(world, chunk)));
    public static final UnregistrableRegistry<InGameEndMouse> IN_GAME_END_MOUSE = new UnregistrableRegistry<>(
        callbacks->(input, action)->callbacks.forEach(callback->callback.onInGameEndMouse(input, action)));
    public static final UnregistrableRegistry<IRangeChangeListener> LITEMATICA_RANGE_CHANGED = new UnregistrableRegistry<>(
        callbacks->new IRangeChangeListener() {
            @Override public void updateAll() {
                callbacks.forEach(IRangeChangeListener::updateAll);}
            @Override public void updateBetweenX(int minX, int maxX) {
                callbacks.forEach(callback->callback.updateBetweenX(minX, maxX));}
            @Override public void updateBetweenY(int minY, int maxY) {
                callbacks.forEach(callback->callback.updateBetweenY(minY, maxY));}
            @Override public void updateBetweenZ(int minZ, int maxZ) {
                callbacks.forEach(callback->callback.updateBetweenX(minZ, maxZ));}
        });
    public static final UnregistrableRegistry<ResourceReloadCallback> CLIENT_RESOURCE_RELOAD = new UnregistrableRegistry<>(
        callbacks->manager->callbacks.forEach(callback->callback.onResourceReload(manager)));
    public static final UnregistrableRegistry<BetweenRenderFrames> BETWEEN_RENDER_FRAMES = new UnregistrableRegistry<>(
        callbacks->()->callbacks.forEach(BetweenRenderFrames::betweenFrames));
    
    static{
        ClientLevelEvents.AFTER_CLIENT_LEVEL_CHANGE.register(AFTER_CLIENT_WORLD_CHANGE.runner());
        ClientTickEvents.START_CLIENT_TICK.register(START_CLIENT_TICK.runner());
        ClientTickEvents.END_CLIENT_TICK.register(END_CLIENT_TICK.runner());
        ClientChunkEvents.CHUNK_LOAD.register(CLIENT_CHUNK_LOAD.runner());
        ClientChunkEvents.CHUNK_UNLOAD.register(CLIENT_CHUNK_UNLOAD.runner());
        LevelRenderEvents.AFTER_BLOCK_OUTLINE_EXTRACTION.register(AFTER_BLOCK_OUTLINE_EXTRACTION.runner());
        LevelRenderEvents.END_EXTRACTION.register(END_EXTRACTION.runner());
        LevelRenderEvents.START_MAIN.register(START_MAIN.runner());
        LevelRenderEvents.BEFORE_ENTITIES.register(BEFORE_ENTITIES.runner());
        LevelRenderEvents.AFTER_ENTITIES.register(AFTER_ENTITIES.runner());
        LevelRenderEvents.BEFORE_DEBUG_RENDER.register(BEFORE_DEBUG_RENDER.runner());
        LevelRenderEvents.BEFORE_TRANSLUCENT.register(BEFORE_TRANSLUCENT.runner());
        LevelRenderEvents.BEFORE_BLOCK_OUTLINE.register(BEFORE_BLOCK_OUTLINE.runner());
        LevelRenderEvents.END_MAIN.register(END_MAIN.runner());
        var toolTipComponentInsertFirstRenderer = MASA_RENDER_TOOLTIP_COMPONENT_INSERTION_FIRST.runner();
        var toolTipComponentInsertMiddleRenderer = MASA_RENDER_TOOLTIP_COMPONENT_INSERTION_MIDDLE.runner();
        var toolTipComponentInsertLastRenderer = MASA_RENDER_TOOLTIP_COMPONENT_INSERTION_LAST.runner();
        var toolTipLastRenderer = MASA_RENDER_TOOLTIP_LAST.runner();
        IRenderer malilibRenderer = new IRenderer() {
            @Override public void onExtractGuiOverlayPost(GuiContext ctx, float partialTicks, ProfilerFiller profiler) {}
            
            @Override public void onExtractWorldPreWeather(DeltaTracker deltaTracker, Camera camera, float ticks, ProfilerFiller profiler) {}
            
            @Override public void onRenderWorldPreWeather(RenderTarget fb, Matrix4fc modelViewMatrix, CameraRenderState cameraState, Frustum culling, RenderBuffers buffers, GpuBufferSlice terrainFog, Vector4f fogColor, ProfilerFiller profiler) {}
            
            @Override public void onExtractWorldLast(DeltaTracker deltaTracker, Camera camera, float ticks, ProfilerFiller profiler) {}
            
            @Override public void onRenderWorldLast(RenderTarget fb, Matrix4fc modelViewMatrix, CameraRenderState cameraState, Frustum culling, RenderBuffers buffers, GpuBufferSlice terrainFog, Vector4f fogColor, ProfilerFiller profiler) {}
            
            @Override public void onRenderTooltipComponentInsertFirst(Item.TooltipContext context, ItemStack stack, Consumer<Component> list) {
                toolTipComponentInsertFirstRenderer.onRenderTooltipComponentInsertFirst(context, stack, list);
            }
            @Override public void onRenderTooltipComponentInsertMiddle(Item.TooltipContext context, ItemStack stack, Consumer<Component> list) {
                toolTipComponentInsertMiddleRenderer.onRenderTooltipComponentInsertMiddle(context, stack, list);
            }
            @Override public void onRenderTooltipComponentInsertLast(Item.TooltipContext context, ItemStack stack, Consumer<Component> list) {
                toolTipComponentInsertLastRenderer.onRenderTooltipComponentInsertLast(context, stack, list);
            }
            @Override public void onRenderTooltipLast(GuiContext ctx, ItemStack stack, int x, int y) {
                toolTipLastRenderer.onRenderTooltipLast(ctx, stack, x, y);
            }
            
            @Override public void onRegisterSpecialGuiRenderer(GuiRenderer guiRenderer, MultiBufferSource.BufferSource immediate, Minecraft mc, ImmutableMap.Builder<Class<? extends net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState>, PictureInPictureRenderer<?>> builder) {
            
            }
        };
        var malilibRenderEventHandler = RenderEventHandler.getInstance();
        malilibRenderEventHandler.registerInGameGuiRenderer(malilibRenderer);
        malilibRenderEventHandler.registerTooltipLastRenderer(malilibRenderer);
        malilibRenderEventHandler.registerWorldPreWeatherRenderer(malilibRenderer);
        malilibRenderEventHandler.registerWorldLastRenderer(malilibRenderer);
        malilibRenderEventHandler.registerSpecialGuiRenderer(malilibRenderer);
    }
    static {
        Identifier lpcRegistryClientResourceReloadCallbackId = Identifier.fromNamespaceAndPath("lpctools", "lpcfymasaapi_reload");
        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloader(lpcRegistryClientResourceReloadCallbackId,
            (ResourceManagerReloadListener) manager -> CLIENT_RESOURCE_RELOAD.runner().onResourceReload(manager));
    }
    
    public interface GameOverlayRender {
        void renderGameOverlay(GuiContext ctx, float partialTicks, ProfilerFiller profiler);
    }
    public record MASAWorldRenderContext(RenderTarget fb, Matrix4f positionMatrix, Matrix4f projectionMatrix, Frustum frustum, Camera camera, RenderBuffers buffers, ProfilerFiller profiler) {}
    public interface WorldPreMainRender {
        void onRenderWorldPreMain(MASAWorldRenderContext context);
    }
    public interface WorldPreWeatherRender {
        void onRenderWorldPreWeather(MASAWorldRenderContext context);
    }
    public interface WorldLastRender {
        void onLast(MASAWorldRenderContext context);
    }
    public interface TooltipComponentInsertFirstRender {
        void onRenderTooltipComponentInsertFirst(Item.TooltipContext context, ItemStack stack, Consumer<Component> list);
    }
    public interface TooltipComponentInsertMiddleRender {
        void onRenderTooltipComponentInsertMiddle(Item.TooltipContext context, ItemStack stack, Consumer<Component> list);
    }
    public interface TooltipComponentInsertLastRender {
        void onRenderTooltipComponentInsertLast(Item.TooltipContext context, ItemStack stack, Consumer<Component> list);
    }
    public interface TooltipLastRender {
        void onRenderTooltipLast(GuiContext ctx, ItemStack stack, int x, int y);
    }
    public interface ClientWorldChunkSetBlockState {//at RETURN
        void onClientWorldChunkSetBlockState(LevelChunk chunk, BlockPos pos, @Nullable BlockState lastState, @Nullable BlockState newState);
    }
    public interface ScreenChangeCallback{
        void onScreenChanged(Screen newScreen);
    }
    public interface ClientWorldChunkLightUpdated{
        void onClientWorldChunkLightUpdated(@NotNull ClientLevel world, @NotNull LevelChunk chunk);
    }
    public interface InGameEndMouse {
        void onInGameEndMouse(MouseButtonInfo input, int action);
    }
    public interface ResourceReloadCallback{
        void onResourceReload(ResourceManager manager);
    }
    public interface BetweenRenderFrames {
        void betweenFrames();
    }
}
