package lpctools.lpcfymasaapi;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.NotNull;

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
    public static final UnregistrableRegistry<WorldRenderEvents.Start> WORLD_RENDER_START = new UnregistrableRegistry<>(
        callbacks->context->callbacks.forEach(callback->callback.onStart(context)));
    public static final UnregistrableRegistry<WorldRenderEvents.AfterSetup> WORLD_RENDER_AFTER_SETUP = new UnregistrableRegistry<>(
        callbacks->context->callbacks.forEach(callback->callback.afterSetup(context)));
    public static final UnregistrableRegistry<WorldRenderEvents.BeforeEntities> WORLD_RENDER_BEFORE_ENTITIES = new UnregistrableRegistry<>(
        callbacks->context->callbacks.forEach(callback->callback.beforeEntities(context)));
    public static final UnregistrableRegistry<WorldRenderEvents.AfterEntities> WORLD_RENDER_AFTER_ENTITIES = new UnregistrableRegistry<>(
        callbacks->context->callbacks.forEach(callback->callback.afterEntities(context)));
    public static final UnregistrableRegistry<WorldRenderEvents.BeforeBlockOutline> WORLD_RENDER_BEFORE_BLOCK_OUTLINE = new UnregistrableRegistry<>(
        callbacks->(context, hitResult)->callbacks.combineResults(true, (lastBoolean, callback)->callback.beforeBlockOutline(context,hitResult) ? lastBoolean : false));
    public static final UnregistrableRegistry<WorldRenderEvents.BlockOutline> WORLD_RENDER_BLOCK_OUTLINE = new UnregistrableRegistry<>(
        callbacks->(context, hitResult)->callbacks.combineResults(true, (lastBoolean, callback)->callback.onBlockOutline(context,hitResult) ? lastBoolean : false));
    public static final UnregistrableRegistry<WorldRenderEvents.DebugRender> WORLD_RENDER_BEFORE_DEBUG_RENDER = new UnregistrableRegistry<>(
        callbacks->context->callbacks.forEach(callback->callback.beforeDebugRender(context)));
    public static final UnregistrableRegistry<WorldRenderEvents.AfterTranslucent> WORLD_RENDER_AFTER_TRANSLUCENT = new UnregistrableRegistry<>(
        callbacks->context->callbacks.forEach(callback->callback.afterTranslucent(context)));
    public static final UnregistrableRegistry<WorldRenderEvents.Last> WORLD_RENDER_LAST = new UnregistrableRegistry<>(
        callbacks->context->callbacks.forEach(callback->callback.onLast(context)));
    public static final UnregistrableRegistry<WorldRenderEvents.End> WORLD_RENDER_END = new UnregistrableRegistry<>(
        callbacks->context->callbacks.forEach(callback->callback.onEnd(context)));
    public static final UnregistrableRegistry<ClientWorldChunkLightUpdated> CLIENT_CHUNK_LIGHT_LOAD = new UnregistrableRegistry<>(
        callbacks->(world, chunk)->callbacks.forEach(callback->callback.onClientWorldChunkLightUpdated(world, chunk)));
    public static final UnregistrableRegistry<InGameEndMouse> IN_GAME_END_MOUSE = new UnregistrableRegistry<>(
        callbacks->(button, action, mods)->callbacks.forEach(callback->callback.onInGameEndMouse(button, action, mods)));
    
    static{
        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register(AFTER_CLIENT_WORLD_CHANGE.run());
        ClientTickEvents.START_CLIENT_TICK.register(START_CLIENT_TICK.run());
        ClientTickEvents.END_CLIENT_TICK.register(END_CLIENT_TICK.run());
        ClientChunkEvents.CHUNK_LOAD.register(CLIENT_CHUNK_LOAD.run());
        ClientChunkEvents.CHUNK_UNLOAD.register(CLIENT_CHUNK_UNLOAD.run());
        WorldRenderEvents.START.register(WORLD_RENDER_START.run());
        WorldRenderEvents.AFTER_SETUP.register(WORLD_RENDER_AFTER_SETUP.run());
        WorldRenderEvents.BEFORE_ENTITIES.register(WORLD_RENDER_BEFORE_ENTITIES.run());
        WorldRenderEvents.AFTER_ENTITIES.register(WORLD_RENDER_AFTER_ENTITIES.run());
        WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register(WORLD_RENDER_BEFORE_BLOCK_OUTLINE.run());
        WorldRenderEvents.BLOCK_OUTLINE.register(WORLD_RENDER_BLOCK_OUTLINE.run());
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(WORLD_RENDER_BEFORE_DEBUG_RENDER.run());
        WorldRenderEvents.AFTER_TRANSLUCENT.register(WORLD_RENDER_AFTER_TRANSLUCENT.run());
        WorldRenderEvents.LAST.register(WORLD_RENDER_LAST.run());
        WorldRenderEvents.END.register(WORLD_RENDER_END.run());
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
}
