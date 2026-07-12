package lpctools.lpcfymasaapi;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import fi.dy.masa.malilib.event.RenderEventHandler;
import fi.dy.masa.malilib.interfaces.IRangeChangeListener;
import fi.dy.masa.malilib.interfaces.IRenderer;
import fi.dy.masa.malilib.render.GuiContext;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.function.Consumer;

@SuppressWarnings("unused")
public class Registries {
    public static final UnregistrableRegistry<ClientLevelEvents.AfterClientLevelChange> AFTER_CLIENT_LEVEL_CHANGE = UnregistrableRegistry.fanOut(
        ClientLevelEvents.AfterClientLevelChange.class, ClientLevelEvents.AFTER_CLIENT_LEVEL_CHANGE);
    // orCircuit: 返回 true 表示终止 screen change — 不能使用 fanOut
    public static final UnregistrableRegistry<BeforeScreenChangeCallback> BEFORE_SCREEN_CHANGE = new UnregistrableRegistry<>(
        callbacks->screen->callbacks.orCircuit(callback->callback.beforeScreenChange(screen)));
    public static final UnregistrableRegistryBase<Runnable, ScreenChangedCallback> ON_SCREEN_CHANGED = new UnregistrableRegistryBase<>(
        callbacks->()-> {
            Minecraft mc = Minecraft.getInstance();
            Screen screen = mc.screen;
            loop:
            while(true) {
                for(ScreenChangedCallback screenChangedCallback : callbacks) {
                    screenChangedCallback.onScreenChanged(screen);
                    if(mc.screen != screen) {
                        screen = mc.screen;
                        continue loop;
                    }
                }
                break;
            }
        });
    public static final UnregistrableRegistry<ContainerContentInitializedCallback> CLIENT_CONTAINER_CONTENT_INITIALIZED = UnregistrableRegistry.fanOut(ContainerContentInitializedCallback.class);
    public static final UnregistrableRegistry<ClientTickEvents.StartTick> START_CLIENT_TICK = UnregistrableRegistry.fanOut(ClientTickEvents.StartTick.class, ClientTickEvents.START_CLIENT_TICK);
    public static final UnregistrableRegistry<ClientTickEvents.EndTick> END_CLIENT_TICK = UnregistrableRegistry.fanOut(ClientTickEvents.EndTick.class, ClientTickEvents.END_CLIENT_TICK);
    public static final UnregistrableRegistry<ClientChunkEvents.Load> CLIENT_CHUNK_LOAD = UnregistrableRegistry.fanOut(ClientChunkEvents.Load.class, ClientChunkEvents.CHUNK_LOAD);
    public static final UnregistrableRegistry<ClientChunkEvents.Unload> CLIENT_CHUNK_UNLOAD = UnregistrableRegistry.fanOut(ClientChunkEvents.Unload.class, ClientChunkEvents.CHUNK_UNLOAD);
    public static final UnregistrableRegistry<WorldPreMainRender> PRE_MAIN = UnregistrableRegistry.fanOut(WorldPreMainRender.class);
    public static final UnregistrableRegistry<LevelRenderEvents.AfterBlockOutlineExtraction> AFTER_BLOCK_OUTLINE_EXTRACTION = UnregistrableRegistry.fanOut(LevelRenderEvents.AfterBlockOutlineExtraction.class, LevelRenderEvents.AFTER_BLOCK_OUTLINE_EXTRACTION);
    public static final UnregistrableRegistry<LevelRenderEvents.EndExtraction> END_EXTRACTION = UnregistrableRegistry.fanOut(LevelRenderEvents.EndExtraction.class, LevelRenderEvents.END_EXTRACTION);
    public static final UnregistrableRegistry<LevelRenderEvents.StartMain> START_MAIN = UnregistrableRegistry.fanOut(LevelRenderEvents.StartMain.class, LevelRenderEvents.START_MAIN);
    public static final UnregistrableRegistry<LevelRenderEvents.AfterOpaqueTerrain> AFTER_OPAQUE_TERRAIN = UnregistrableRegistry.fanOut(LevelRenderEvents.AfterOpaqueTerrain.class, LevelRenderEvents.AFTER_OPAQUE_TERRAIN);
    public static final UnregistrableRegistry<LevelRenderEvents.AfterSolidFeatures> AFTER_SOLID_FEATURES = UnregistrableRegistry.fanOut(LevelRenderEvents.AfterSolidFeatures.class, LevelRenderEvents.AFTER_SOLID_FEATURES);
    public static final UnregistrableRegistry<LevelRenderEvents.AfterTranslucentFeatures> AFTER_TRANSLUCENT_FEATURES = UnregistrableRegistry.fanOut(LevelRenderEvents.AfterTranslucentFeatures.class, LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES);
    public static final UnregistrableRegistry<LevelRenderEvents.BeforeBlockOutline> BEFORE_BLOCK_OUTLINE = new UnregistrableRegistry<>(
        callbacks->(context, outlineRenderState)->callbacks.andNonCircuit(callback->callback.beforeBlockOutline(context, outlineRenderState)), LevelRenderEvents.BEFORE_BLOCK_OUTLINE);
    public static final UnregistrableRegistry<LevelRenderEvents.BeforeGizmos> BEFORE_GIZMOS = UnregistrableRegistry.fanOut(LevelRenderEvents.BeforeGizmos.class, LevelRenderEvents.BEFORE_GIZMOS);
    public static final UnregistrableRegistry<LevelRenderEvents.BeforeTranslucentTerrain> BEFORE_TRANSLUCENT_TERRAIN = UnregistrableRegistry.fanOut(LevelRenderEvents.BeforeTranslucentTerrain.class, LevelRenderEvents.BEFORE_TRANSLUCENT_TERRAIN);
    public static final UnregistrableRegistry<LevelRenderEvents.AfterTranslucentTerrain> AFTER_TRANSLUCENT_TERRAIN = UnregistrableRegistry.fanOut(LevelRenderEvents.AfterTranslucentTerrain.class, LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN);
    public static final UnregistrableRegistry<LevelRenderEvents.EndMain> END_MAIN = UnregistrableRegistry.fanOut(LevelRenderEvents.EndMain.class, LevelRenderEvents.END_MAIN);
    public static final UnregistrableRegistry<ClientWorldChunkSetBlockState> CLIENT_WORLD_CHUNK_SET_BLOCK_STATE = UnregistrableRegistry.fanOut(ClientWorldChunkSetBlockState.class);
    public static final UnregistrableRegistry<GameOverlayRender> MASA_RENDER_GAME_OVERLAY = UnregistrableRegistry.fanOut(GameOverlayRender.class);
    public static final UnregistrableRegistry<WorldPreWeatherRender> MASA_RENDER_WORLD_PRE_WEATHER = UnregistrableRegistry.fanOut(WorldPreWeatherRender.class);
    public static final UnregistrableRegistry<WorldLastRender> MASA_WORLD_RENDER_LAST = UnregistrableRegistry.fanOut(WorldLastRender.class);
    public static final UnregistrableRegistry<TooltipComponentInsertFirstRender> MASA_RENDER_TOOLTIP_COMPONENT_INSERTION_FIRST = UnregistrableRegistry.fanOut(TooltipComponentInsertFirstRender.class);
    public static final UnregistrableRegistry<TooltipComponentInsertMiddleRender> MASA_RENDER_TOOLTIP_COMPONENT_INSERTION_MIDDLE = UnregistrableRegistry.fanOut(TooltipComponentInsertMiddleRender.class);
    public static final UnregistrableRegistry<TooltipComponentInsertLastRender> MASA_RENDER_TOOLTIP_COMPONENT_INSERTION_LAST = UnregistrableRegistry.fanOut(TooltipComponentInsertLastRender.class);
    public static final UnregistrableRegistry<TooltipLastRender> MASA_RENDER_TOOLTIP_LAST = UnregistrableRegistry.fanOut(TooltipLastRender.class);
    public static final UnregistrableRegistry<ClientWorldChunkLightUpdated> CLIENT_CHUNK_LIGHT_LOAD = UnregistrableRegistry.fanOut(ClientWorldChunkLightUpdated.class);
    public static final UnregistrableRegistry<InGameEndMouse> IN_GAME_END_MOUSE = UnregistrableRegistry.fanOut(InGameEndMouse.class);
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
    public static final UnregistrableRegistry<ResourceReloadCallback> CLIENT_RESOURCE_RELOAD = UnregistrableRegistry.fanOut(ResourceReloadCallback.class);
    public static final UnregistrableRegistry<BetweenRenderFrames> BETWEEN_RENDER_FRAMES = UnregistrableRegistry.fanOut(BetweenRenderFrames.class);
    public static final UnregistrableRegistry<ClientEntityEvents.Load> CLIENT_ENTITY_LOAD = UnregistrableRegistry.fanOut(ClientEntityEvents.Load.class, ClientEntityEvents.ENTITY_LOAD);
    public static final UnregistrableRegistry<ClientEntityEvents.Unload> CLIENT_ENTITY_UNLOAD = UnregistrableRegistry.fanOut(ClientEntityEvents.Unload.class, ClientEntityEvents.ENTITY_UNLOAD);
    
    static{
        var overlayRenderer = MASA_RENDER_GAME_OVERLAY.runner();
        var worldPreWeatherRenderer = MASA_RENDER_WORLD_PRE_WEATHER.runner();
        var worldLastRenderer = MASA_WORLD_RENDER_LAST.runner();
        var toolTipComponentInsertFirstRenderer = MASA_RENDER_TOOLTIP_COMPONENT_INSERTION_FIRST.runner();
        var toolTipComponentInsertMiddleRenderer = MASA_RENDER_TOOLTIP_COMPONENT_INSERTION_MIDDLE.runner();
        var toolTipComponentInsertLastRenderer = MASA_RENDER_TOOLTIP_COMPONENT_INSERTION_LAST.runner();
        var toolTipLastRenderer = MASA_RENDER_TOOLTIP_LAST.runner();
        IRenderer malilibRenderer = new IRenderer() {
            @Override public void onRenderGameOverlayPostAdvanced(GuiContext ctx, float partialTicks, ProfilerFiller profiler) {
                overlayRenderer.renderGameOverlay(ctx, partialTicks, profiler);
            }
            @Override public void onRenderWorldPreWeather(RenderTarget fb, Matrix4f posMatrix, Matrix4f projMatrix, Frustum frustum, Camera camera, RenderBuffers buffers, ProfilerFiller profiler) {
                worldPreWeatherRenderer.onRenderWorldPreWeather(new MASAWorldRenderContext(fb, posMatrix, projMatrix, frustum, camera, buffers, profiler));
            }
            @Override public void onRenderWorldLastAdvanced(RenderTarget fb, Matrix4f posMatrix, Matrix4f projMatrix, Frustum frustum, Camera camera, RenderBuffers buffers, ProfilerFiller profiler) {
                worldLastRenderer.onLast(new MASAWorldRenderContext(fb, posMatrix, projMatrix, frustum, camera, buffers, profiler));
            }
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

        };
        var malilibRenderEventHandler = RenderEventHandler.getInstance();
        malilibRenderEventHandler.registerGameOverlayRenderer(malilibRenderer);
        malilibRenderEventHandler.registerTooltipLastRenderer(malilibRenderer);
        malilibRenderEventHandler.registerWorldPostDebugRenderer(malilibRenderer);
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
    public interface BeforeScreenChangeCallback {
        // 返回true表示终止此次screen change，false表示可以继续
        boolean beforeScreenChange(Screen newScreen);
    }
    public interface ScreenChangedCallback {
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
    public interface ContainerContentInitializedCallback {
        void onContainerContentInitialized(AbstractContainerMenu menu);
    }
}
