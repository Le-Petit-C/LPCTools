package lpctools.lpcfymasaapi.render.translucentShapes;

import lpctools.lpcfymasaapi.Registries;
import lpctools.lpcfymasaapi.UnregistrableRegistry;
import lpctools.lpcfymasaapi.interfaces.IUnregistrableRegistry;

import java.util.function.Function;

public enum RenderTiming implements IUnregistrableRegistry<IRenderCallback> {
	START_MAIN(Registries.START, callback->context->callback.render()),
	AFTER_SETUP(Registries.AFTER_SETUP, callback->context->callback.render()),
	BEFORE_ENTITIES(Registries.BEFORE_ENTITIES, callback->context->callback.render()),
	AFTER_ENTITIES(Registries.AFTER_ENTITIES, callback->context->callback.render()),
	BEFORE_BLOCK_OUTLINE(Registries.BEFORE_BLOCK_OUTLINE, callback->(context, outlineRenderState)->{callback.render(); return true;}),
	BLOCK_OUTLINE(Registries.BLOCK_OUTLINE, callback->(context, outlineRenderState)->{callback.render(); return true;}),
	BEFORE_DEBUG_RENDER(Registries.BEFORE_DEBUG_RENDER, callback->context->callback.render()),
	AFTER_TRANSLUCENT(Registries.AFTER_TRANSLUCENT, callback->context->callback.render()),
	ON_LAST(Registries.ON_LAST, callback->context->callback.render()),
	ON_END(Registries.ON_END, callback->context->callback.render());
	<T> RenderTiming(UnregistrableRegistry<T> registry, Function<IRenderCallback, T> mappingFunction) {
		this.registry = new UnregistrableRegistry<>(callbacks->()->callbacks.forEach(IRenderCallback::render));
		registry.register(mappingFunction.apply(this.registry.run()));
	}
	private final UnregistrableRegistry<IRenderCallback> registry;
	@Override public boolean register(IRenderCallback renderCallback, boolean register){ return registry.register(renderCallback, register); }
	@Override public boolean isEmpty() { return registry.isEmpty(); }
	@Override public IRenderCallback run() { return registry.run(); }
}
