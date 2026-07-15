package lpctools.lpcfymasaapi;

import fi.dy.masa.malilib.event.RenderEventHandler;
import fi.dy.masa.malilib.interfaces.IRangeChangeListener;
import fi.dy.masa.malilib.interfaces.IRenderer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.List;

@SuppressWarnings("unused")
public class Registries {
    public static final UnregistrableRegistry<AfterClientWorldChange> AFTER_CLIENT_LEVEL_CHANGE = UnregistrableRegistry.fanOut(AfterClientWorldChange.class);
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
    public static final UnregistrableRegistry<WorldRenderEvents.Start> START = UnregistrableRegistry.fanOut(WorldRenderEvents.Start.class, WorldRenderEvents.START);
    public static final UnregistrableRegistry<WorldRenderEvents.AfterSetup> AFTER_SETUP = UnregistrableRegistry.fanOut(WorldRenderEvents.AfterSetup.class, WorldRenderEvents.AFTER_SETUP);
    public static final UnregistrableRegistry<WorldRenderEvents.BeforeEntities> BEFORE_ENTITIES = UnregistrableRegistry.fanOut(WorldRenderEvents.BeforeEntities.class, WorldRenderEvents.BEFORE_ENTITIES);
    public static final UnregistrableRegistry<WorldRenderEvents.AfterEntities> AFTER_ENTITIES = UnregistrableRegistry.fanOut(WorldRenderEvents.AfterEntities.class, WorldRenderEvents.AFTER_ENTITIES);
    public static final UnregistrableRegistry<WorldRenderEvents.BeforeBlockOutline> BEFORE_BLOCK_OUTLINE = new UnregistrableRegistry<>(
        callbacks->(context, outlineRenderState)->callbacks.andNonCircuit(callback->callback.beforeBlockOutline(context, outlineRenderState)), WorldRenderEvents.BEFORE_BLOCK_OUTLINE);
    public static final UnregistrableRegistry<WorldRenderEvents.BlockOutline> BLOCK_OUTLINE = new UnregistrableRegistry<>(
        callbacks->(context, outlineRenderState)->callbacks.andNonCircuit(callback->callback.onBlockOutline(context, outlineRenderState)), WorldRenderEvents.BLOCK_OUTLINE);
    public static final UnregistrableRegistry<WorldRenderEvents.DebugRender> BEFORE_DEBUG_RENDER = UnregistrableRegistry.fanOut(WorldRenderEvents.DebugRender.class, WorldRenderEvents.BEFORE_DEBUG_RENDER);
    public static final UnregistrableRegistry<WorldRenderEvents.AfterTranslucent> AFTER_TRANSLUCENT = UnregistrableRegistry.fanOut(WorldRenderEvents.AfterTranslucent.class, WorldRenderEvents.AFTER_TRANSLUCENT);
    public static final UnregistrableRegistry<WorldRenderEvents.Last> ON_LAST = UnregistrableRegistry.fanOut(WorldRenderEvents.Last.class, WorldRenderEvents.LAST);
    public static final UnregistrableRegistry<WorldRenderEvents.End> ON_END = UnregistrableRegistry.fanOut(WorldRenderEvents.End.class, WorldRenderEvents.END);
    public static final UnregistrableRegistry<ClientWorldChunkSetBlockState> CLIENT_WORLD_CHUNK_SET_BLOCK_STATE = UnregistrableRegistry.fanOut(ClientWorldChunkSetBlockState.class);
    public static final UnregistrableRegistry<GameOverlayRender> MASA_RENDER_GAME_OVERLAY = UnregistrableRegistry.fanOut(GameOverlayRender.class);
    public static final UnregistrableRegistry<OverlayLastRender> MASA_RENDER_OVERLAY_LAST = new UnregistrableRegistry<>(
        callbacks->(a, b, c)->callbacks.forEach(renderer->renderer.renderOverlayLast(a, b, c)));
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
        var overlayLastRenderer = MASA_RENDER_OVERLAY_LAST.runner();
        var overlayRenderer = MASA_RENDER_GAME_OVERLAY.runner();
        var worldLastRenderer = MASA_WORLD_RENDER_LAST.runner();
        var toolTipComponentInsertFirstRenderer = MASA_RENDER_TOOLTIP_COMPONENT_INSERTION_FIRST.runner();
        var toolTipComponentInsertMiddleRenderer = MASA_RENDER_TOOLTIP_COMPONENT_INSERTION_MIDDLE.runner();
        var toolTipComponentInsertLastRenderer = MASA_RENDER_TOOLTIP_COMPONENT_INSERTION_LAST.runner();
        var toolTipLastRenderer = MASA_RENDER_TOOLTIP_LAST.runner();
        IRenderer malilibRenderer = new IRenderer() {
            @Override public void onRenderGameOverlayLastDrawer(GuiGraphics drawContext, float partialTicks, ProfilerFiller profiler, Minecraft mc) {
                overlayLastRenderer.renderOverlayLast(drawContext, partialTicks, profiler);
            }
            @Override public void onRenderGameOverlayPost(GuiGraphics ctx) {
                overlayRenderer.renderGameOverlay(ctx);
            }
            @Override public void onRenderWorldLast(Matrix4f posMatrix, Matrix4f projMatrix) {
                worldLastRenderer.onLast(posMatrix, projMatrix);
            }
            @Override public void onRenderTooltipComponentInsertFirst(Item.TooltipContext context, ItemStack stack, List<Component> list) {
                toolTipComponentInsertFirstRenderer.onRenderTooltipComponentInsertFirst(context, stack, list);
            }
            @Override public void onRenderTooltipComponentInsertMiddle(Item.TooltipContext context, ItemStack stack, List<Component> list) {
                toolTipComponentInsertMiddleRenderer.onRenderTooltipComponentInsertMiddle(context, stack, list);
            }
            @Override public void onRenderTooltipComponentInsertLast(Item.TooltipContext context, ItemStack stack, List<Component> list) {
                toolTipComponentInsertLastRenderer.onRenderTooltipComponentInsertLast(context, stack, list);
            }
            @Override public void onRenderTooltipLast(GuiGraphics ctx, ItemStack stack, int x, int y) {
                toolTipLastRenderer.onRenderTooltipLast(ctx, stack, x, y);
            }

        };
        var malilibRenderEventHandler = RenderEventHandler.getInstance();
        malilibRenderEventHandler.registerGameOverlayRenderer(malilibRenderer);
        malilibRenderEventHandler.registerTooltipLastRenderer(malilibRenderer);
        malilibRenderEventHandler.registerWorldLastRenderer(malilibRenderer);
    }
    static {
        ResourceLocation lpcRegistryClientResourceReloadCallbackId = ResourceLocation.fromNamespaceAndPath("lpctools", "lpcfymasaapi_reload");
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override public ResourceLocation getFabricId() {return lpcRegistryClientResourceReloadCallbackId;}
            @Override public void onResourceManagerReload(ResourceManager manager) {CLIENT_RESOURCE_RELOAD.runner().onResourceReload(manager);}
        });
    }
    
    public interface GameOverlayRender {
        void renderGameOverlay(GuiGraphics ctx);
    }
    public interface OverlayLastRender {
        void renderOverlayLast(GuiGraphics ctx, float partialTicks, ProfilerFiller profiler);
    }
    public record MASAWorldRenderContext(Matrix4f positionMatrix, Matrix4f projectionMatrix, Frustum frustum, Camera camera, ProfilerFiller profiler) {}
    public interface WorldPreMainRender {
        void onRenderWorldPreMain(MASAWorldRenderContext context);
    }
    public interface WorldLastRender {
        void onLast(Matrix4f posMatrix, Matrix4f projMatrix);
    }
    public interface TooltipComponentInsertFirstRender {
        void onRenderTooltipComponentInsertFirst(Item.TooltipContext context, ItemStack stack, List<Component> list);
    }
    public interface TooltipComponentInsertMiddleRender {
        void onRenderTooltipComponentInsertMiddle(Item.TooltipContext context, ItemStack stack, List<Component> list);
    }
    public interface TooltipComponentInsertLastRender {
        void onRenderTooltipComponentInsertLast(Item.TooltipContext context, ItemStack stack, List<Component> list);
    }
    public interface TooltipLastRender {
        void onRenderTooltipLast(GuiGraphics ctx, ItemStack stack, int x, int y);
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
        void onInGameEndMouse(int button, int action, int mods);
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
    public interface AfterClientWorldChange{
        void afterWorldChange(Minecraft client, ClientLevel world);
    }
}
