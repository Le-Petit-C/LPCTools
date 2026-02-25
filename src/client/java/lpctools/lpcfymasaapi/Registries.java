package lpctools.lpcfymasaapi;

import com.google.common.collect.ImmutableMap;
import fi.dy.masa.malilib.event.RenderEventHandler;
import fi.dy.masa.malilib.interfaces.IRangeChangeListener;
import fi.dy.masa.malilib.interfaces.IRenderer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.function.Consumer;

public class Registries {
    public static final UnregistrableRegistry<ClientWorldEvents.AfterClientWorldChange> AFTER_CLIENT_WORLD_CHANGE = new UnregistrableRegistry<>(
        callbacks->(client, world)->callbacks.forEach(screen->screen.afterWorldChange(client, world)));
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
    public static final UnregistrableRegistry<WorldRenderEvents.Start> START = new UnregistrableRegistry<>(
        callbacks->(context)->callbacks.forEach(callback->callback.onStart(context)));
    public static final UnregistrableRegistry<WorldRenderEvents.AfterSetup> AFTER_SETUP = new UnregistrableRegistry<>(
        callbacks->(context)->callbacks.forEach(callback->callback.afterSetup(context)));
    public static final UnregistrableRegistry<WorldRenderEvents.BeforeEntities> BEFORE_ENTITIES = new UnregistrableRegistry<>(
        callbacks->(context)->callbacks.forEach(callback->callback.beforeEntities(context)));
    public static final UnregistrableRegistry<WorldRenderEvents.AfterEntities> AFTER_ENTITIES = new UnregistrableRegistry<>(
        callbacks->(context)->callbacks.forEach(callback->callback.afterEntities(context)));
    public static final UnregistrableRegistry<WorldRenderEvents.BeforeBlockOutline> BEFORE_BLOCK_OUTLINE = new UnregistrableRegistry<>(
        callbacks->(context, outlineRenderState)->{
            boolean shouldRender = true;
            for(var callback : callbacks) if(!callback.beforeBlockOutline(context, outlineRenderState)) shouldRender = false;
            return shouldRender;
        });
    public static final UnregistrableRegistry<WorldRenderEvents.BlockOutline> BLOCK_OUTLINE = new UnregistrableRegistry<>(
        callbacks->(context, l)->{
            boolean shouldRender = true;
            for(var callback : callbacks) if(!callback.onBlockOutline(context, l)) shouldRender = false;
            return shouldRender;
        });
    public static final UnregistrableRegistry<WorldRenderEvents.DebugRender> BEFORE_DEBUG_RENDER = new UnregistrableRegistry<>(
        callbacks->(context)->callbacks.forEach(callback->callback.beforeDebugRender(context)));
    public static final UnregistrableRegistry<WorldRenderEvents.AfterTranslucent> AFTER_TRANSLUCENT = new UnregistrableRegistry<>(
        callbacks->(context)->callbacks.forEach(callback->callback.afterTranslucent(context)));
    public static final UnregistrableRegistry<WorldRenderEvents.Last> ON_LAST = new UnregistrableRegistry<>(
        callbacks->(context)->callbacks.forEach(callback->callback.onLast(context)));
    public static final UnregistrableRegistry<WorldRenderEvents.End> ON_END = new UnregistrableRegistry<>(
        callbacks->(context)->callbacks.forEach(callback->callback.onEnd(context)));
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
        callbacks->(button, action, mods)->callbacks.forEach(callback->callback.onInGameEndMouse(button, action, mods)));
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
    
    static{
        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register(AFTER_CLIENT_WORLD_CHANGE.run());
        ClientTickEvents.START_CLIENT_TICK.register(START_CLIENT_TICK.run());
        ClientTickEvents.END_CLIENT_TICK.register(END_CLIENT_TICK.run());
        ClientChunkEvents.CHUNK_LOAD.register(CLIENT_CHUNK_LOAD.run());
        ClientChunkEvents.CHUNK_UNLOAD.register(CLIENT_CHUNK_UNLOAD.run());
        WorldRenderEvents.START.register(START.run());
        WorldRenderEvents.AFTER_SETUP.register(AFTER_SETUP.run());
        WorldRenderEvents.BEFORE_ENTITIES.register(BEFORE_ENTITIES.run());
        WorldRenderEvents.AFTER_ENTITIES.register(AFTER_ENTITIES.run());
        WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register(BEFORE_BLOCK_OUTLINE.run());
        WorldRenderEvents.BLOCK_OUTLINE.register(BLOCK_OUTLINE.run());
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(BEFORE_DEBUG_RENDER.run());
        WorldRenderEvents.AFTER_TRANSLUCENT.register(AFTER_TRANSLUCENT.run());
        WorldRenderEvents.LAST.register(ON_LAST.run());
        WorldRenderEvents.END.register(ON_END.run());
        var overlayRenderer = MASA_RENDER_GAME_OVERLAY.run();
        var worldPreWeatherRenderer = MASA_RENDER_WORLD_PRE_WEATHER.run();
        var worldLastRenderer = MASA_WORLD_RENDER_LAST.run();
        var toolTipComponentInsertFirstRenderer = MASA_RENDER_TOOLTIP_COMPONENT_INSERTION_FIRST.run();
        var toolTipComponentInsertMiddleRenderer = MASA_RENDER_TOOLTIP_COMPONENT_INSERTION_MIDDLE.run();
        var toolTipComponentInsertLastRenderer = MASA_RENDER_TOOLTIP_COMPONENT_INSERTION_LAST.run();
        var toolTipLastRenderer = MASA_RENDER_TOOLTIP_LAST.run();
        IRenderer malilibRenderer = new IRenderer() {
            @Override public void onRenderGameOverlayPostAdvanced(DrawContext ctx, float partialTicks, Profiler profiler, MinecraftClient mc) {
                overlayRenderer.renderGameOverlay(ctx, partialTicks, profiler);
            }
            @Override public void onRenderWorldPreWeather(Framebuffer fb, Matrix4f posMatrix, Matrix4f projMatrix, Frustum frustum, Camera camera, BufferBuilderStorage buffers, Profiler profiler) {
                worldPreWeatherRenderer.onRenderWorldPreWeather(new MASAWorldRenderContext(fb, posMatrix, projMatrix, frustum, camera, buffers, profiler));
            }
            @Override public void onRenderWorldLastAdvanced(Framebuffer fb, Matrix4f posMatrix, Matrix4f projMatrix, Frustum frustum, Camera camera, BufferBuilderStorage buffers, Profiler profiler) {
                worldLastRenderer.onLast(new MASAWorldRenderContext(fb, posMatrix, projMatrix, frustum, camera, buffers, profiler));
            }
            @Override public void onRenderTooltipComponentInsertFirst(Item.TooltipContext context, ItemStack stack, Consumer<Text> list) {
                toolTipComponentInsertFirstRenderer.onRenderTooltipComponentInsertFirst(context, stack, list);
            }
            @Override public void onRenderTooltipComponentInsertMiddle(Item.TooltipContext context, ItemStack stack, Consumer<Text> list) {
                toolTipComponentInsertMiddleRenderer.onRenderTooltipComponentInsertMiddle(context, stack, list);
            }
            @Override public void onRenderTooltipComponentInsertLast(Item.TooltipContext context, ItemStack stack, Consumer<Text> list) {
                toolTipComponentInsertLastRenderer.onRenderTooltipComponentInsertLast(context, stack, list);
            }
            @Override public void onRenderTooltipLast(DrawContext ctx, ItemStack stack, int x, int y) {
                toolTipLastRenderer.onRenderTooltipLast(ctx, stack, x, y);
            }
            @Override public void onRegisterSpecialGuiRenderer(GuiRenderer guiRenderer, VertexConsumerProvider.Immediate immediate, MinecraftClient mc, ImmutableMap.Builder<Class<? extends SpecialGuiElementRenderState>, SpecialGuiElementRenderer<?>> builder) {
                IRenderer.super.onRegisterSpecialGuiRenderer(guiRenderer, immediate, mc, builder);
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
        Identifier lpcRegistryClientResourceReloadCallbackId = Identifier.of("lpctools", "lpcfymasaapi_reload");
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener( new SimpleSynchronousResourceReloadListener() {
            @Override public Identifier getFabricId() {return lpcRegistryClientResourceReloadCallbackId;}
            @Override public void reload(ResourceManager manager) {CLIENT_RESOURCE_RELOAD.run().onResourceReload(manager);}
        });
    }
    
    public interface GameOverlayRender {
        void renderGameOverlay(DrawContext ctx, float partialTicks, Profiler profiler);
    }
    public record MASAWorldRenderContext(Framebuffer fb, Matrix4f positionMatrix, Matrix4f projectionMatrix, Frustum frustum, Camera camera, BufferBuilderStorage buffers, Profiler profiler) {}
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
        void onRenderTooltipComponentInsertFirst(Item.TooltipContext context, ItemStack stack, Consumer<Text> list);
    }
    public interface TooltipComponentInsertMiddleRender {
        void onRenderTooltipComponentInsertMiddle(Item.TooltipContext context, ItemStack stack, Consumer<Text> list);
    }
    public interface TooltipComponentInsertLastRender {
        void onRenderTooltipComponentInsertLast(Item.TooltipContext context, ItemStack stack, Consumer<Text> list);
    }
    public interface TooltipLastRender {
        void onRenderTooltipLast(DrawContext ctx, ItemStack stack, int x, int y);
    }
    public interface ClientWorldChunkSetBlockState {//at RETURN
        void onClientWorldChunkSetBlockState(WorldChunk chunk, BlockPos pos, BlockState lastState, BlockState newState);
    }
    public interface ScreenChangeCallback{
        void onScreenChanged(Screen newScreen);
    }
    public interface ClientWorldChunkLightUpdated{
        void onClientWorldChunkLightUpdated(@NotNull ClientWorld world, @NotNull WorldChunk chunk);
    }
    public interface InGameEndMouse {
        void onInGameEndMouse(int button, int action, int mods);
    }
    public interface ResourceReloadCallback{
        void onResourceReload(ResourceManager manager);
    }
}
