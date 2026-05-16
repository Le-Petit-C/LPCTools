package lpctools.lpcfymasaapi.render.translucentShapes;

import lpctools.lpcfymasaapi.Registries;
import lpctools.lpcfymasaapi.UnregistrableRegistry;
import lpctools.lpcfymasaapi.interfaces.IUnregistrableRegistry;

import java.util.function.Function;

public enum RenderTiming implements IUnregistrableRegistry<IRenderCallback> {
	START_MAIN(Registries.START_MAIN, callback->_->callback.render()),
	AFTER_ENTITIES(Registries.AFTER_TRANSLUCENT_FEATURES, callback->_->callback.render()),
	BEFORE_TRANSLUCENT(Registries.BEFORE_TRANSLUCENT_TERRAIN, callback->_->callback.render()),
	BEFORE_BLOCK_OUTLINE(Registries.BEFORE_BLOCK_OUTLINE, callback->(_, _)->{callback.render(); return true;}),
	END_MAIN(Registries.END_MAIN, callback->_->callback.render());
	<T> RenderTiming(UnregistrableRegistry<T> registry, Function<IRenderCallback, T> mappingFunction) {
		this.registry = new UnregistrableRegistry<>(callbacks->()->callbacks.forEach(IRenderCallback::render));
		registry.register(mappingFunction.apply(this.registry.runner()));
	}
	private final UnregistrableRegistry<IRenderCallback> registry;
	@Override public boolean register(IRenderCallback renderCallback, boolean register){ return registry.register(renderCallback, register); }
	@Override public boolean isEmpty() { return registry.isEmpty(); }
	@Override public IRenderCallback runner() { return registry.runner(); }
}
