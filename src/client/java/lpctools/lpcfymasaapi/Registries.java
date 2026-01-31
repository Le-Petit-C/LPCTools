package lpctools.lpcfymasaapi;

import com.google.common.collect.ImmutableMap;
import fi.dy.masa.malilib.event.RenderEventHandler;
import fi.dy.masa.malilib.interfaces.IRangeChangeListener;
import fi.dy.masa.malilib.interfaces.IRenderer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.MouseInput;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
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
    public static final UnregistrableRegistry<ClientWorldChunkSetBlockState> CLIENT_WORLD_CHUNK_SET_BLOCK_STATE = new UnregistrableRegistry<>(
        callbacks->(chunk, pos, lastState, newState)->callbacks.forEach(screen->screen.onClientWorldChunkSetBlockState(chunk, pos, lastState, newState)));
    public static final UnregistrableRegistry<GameOverlayRender> RENDER_GAME_OVERLAY = new UnregistrableRegistry<>(
        callbacks->(c, t, p, m)->callbacks.forEach(renderer->renderer.renderGameOverlay(c, t, p, m)));
    public static final UnregistrableRegistry<WorldPostDebugRender> RENDER_WORLD_POST_DEBUG = new UnregistrableRegistry<>(
        callbacks->c->callbacks.forEach(renderer->renderer.beforeDebugRender(c)));
    public static final UnregistrableRegistry<WorldPreWeatherRender> RENDER_WORLD_PRE_WEATHER = new UnregistrableRegistry<>(
        callbacks->c->callbacks.forEach(renderer->renderer.onRenderWorldPreWeather(c)));
    public static final UnregistrableRegistry<WorldLastRender> WORLD_RENDER_LAST = new UnregistrableRegistry<>(
        callbacks->c->callbacks.forEach(renderer->renderer.onLast(c)));
    public static final UnregistrableRegistry<TooltipComponentInsertFirstRender> RENDER_TOOLTIP_COMPONENT_INSERTION_FIRST = new UnregistrableRegistry<>(
        callbacks->(c, s, l)->callbacks.forEach(renderer->renderer.onRenderTooltipComponentInsertFirst(c, s, l)));
    public static final UnregistrableRegistry<TooltipComponentInsertMiddleRender> RENDER_TOOLTIP_COMPONENT_INSERTION_MIDDLE = new UnregistrableRegistry<>(
        callbacks->(c, s, l)->callbacks.forEach(renderer->renderer.onRenderTooltipComponentInsertMiddle(c, s, l)));
    public static final UnregistrableRegistry<TooltipComponentInsertLastRender> RENDER_TOOLTIP_COMPONENT_INSERTION_LAST = new UnregistrableRegistry<>(
        callbacks->(c, s, l)->callbacks.forEach(renderer->renderer.onRenderTooltipComponentInsertLast(c, s, l)));
    public static final UnregistrableRegistry<TooltipLastRender> RENDER_TOOLTIP_LAST = new UnregistrableRegistry<>(
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
    
    static{
        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register(AFTER_CLIENT_WORLD_CHANGE.run());
        ClientTickEvents.START_CLIENT_TICK.register(START_CLIENT_TICK.run());
        ClientTickEvents.END_CLIENT_TICK.register(END_CLIENT_TICK.run());
        ClientChunkEvents.CHUNK_LOAD.register(CLIENT_CHUNK_LOAD.run());
        ClientChunkEvents.CHUNK_UNLOAD.register(CLIENT_CHUNK_UNLOAD.run());
        var overlayRenderer = RENDER_GAME_OVERLAY.run();
        var worldPostDebugRenderer = RENDER_WORLD_POST_DEBUG.run();
        var worldPreWeatherRenderer = RENDER_WORLD_PRE_WEATHER.run();
        var worldLastRenderer = WORLD_RENDER_LAST.run();
        var toolTipComponentInsertFirstRenderer = RENDER_TOOLTIP_COMPONENT_INSERTION_FIRST.run();
        var toolTipComponentInsertMiddleRenderer = RENDER_TOOLTIP_COMPONENT_INSERTION_MIDDLE.run();
        var toolTipComponentInsertLastRenderer = RENDER_TOOLTIP_COMPONENT_INSERTION_LAST.run();
        var toolTipLastRenderer = RENDER_TOOLTIP_LAST.run();
        IRenderer malilibRenderer = new IRenderer() {
            @Override public void onRenderGameOverlayPostAdvanced(DrawContext drawContext, float partialTicks, Profiler profiler, MinecraftClient mc) {
                overlayRenderer.renderGameOverlay(drawContext, partialTicks, profiler, mc);
            }
            @Override public void onRenderWorldPostDebugRender(MatrixStack matrices, Frustum frustum, VertexConsumerProvider.Immediate immediate, Vec3d camera, Profiler profiler) {
                worldPostDebugRenderer.beforeDebugRender(new DebugRenderContext(matrices, frustum, immediate, camera, profiler));
            }
            @Override public void onRenderWorldPreWeather(Framebuffer fb, Matrix4f posMatrix, Matrix4f projMatrix, Frustum frustum, Camera camera, BufferBuilderStorage buffers, Profiler profiler) {
                worldPreWeatherRenderer.onRenderWorldPreWeather(new WorldRenderContext(fb, posMatrix, projMatrix, frustum, camera, buffers, profiler));
            }
            @Override public void onRenderWorldLastAdvanced(Framebuffer fb, Matrix4f posMatrix, Matrix4f projMatrix, Frustum frustum, Camera camera, BufferBuilderStorage buffers, Profiler profiler) {
                worldLastRenderer.onLast(new WorldRenderContext(fb, posMatrix, projMatrix, frustum, camera, buffers, profiler));
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
            @Override public void onRenderTooltipLast(DrawContext drawContext, ItemStack stack, int x, int y) {
                toolTipLastRenderer.onRenderTooltipLast(drawContext, stack, x, y);
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
        ResourceLoader.get(ResourceType.CLIENT_RESOURCES).registerReloader(lpcRegistryClientResourceReloadCallbackId,
            (SynchronousResourceReloader) manager -> CLIENT_RESOURCE_RELOAD.run().onResourceReload(manager));
    }
    
    public interface GameOverlayRender {
        void renderGameOverlay(DrawContext drawContext, float partialTicks, Profiler profiler, MinecraftClient mc);
    }
    public record DebugRenderContext(MatrixStack matrices, Frustum frustum, VertexConsumerProvider.Immediate immediate, Vec3d camera, Profiler profiler) {}
    public interface WorldPostDebugRender{
        void beforeDebugRender(DebugRenderContext context);
    }
    public record WorldRenderContext(Framebuffer fb, Matrix4f positionMatrix, Matrix4f projectionMatrix, Frustum frustum, Camera camera, BufferBuilderStorage buffers, Profiler profiler) {}
    public interface WorldPreWeatherRender {
        void onRenderWorldPreWeather(WorldRenderContext context);
    }
    public interface WorldLastRender {
        void onLast(WorldRenderContext context);
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
        void onRenderTooltipLast(DrawContext drawContext, ItemStack stack, int x, int y);
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
        void onInGameEndMouse(MouseInput input, int action);
    }
    public interface ResourceReloadCallback{
        void onResourceReload(ResourceManager manager);
    }
}
