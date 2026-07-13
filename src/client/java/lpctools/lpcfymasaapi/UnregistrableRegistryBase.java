package lpctools.lpcfymasaapi;

import lpctools.lpcfymasaapi.interfaces.IUnregistrableRegistryBase;
import lpctools.lpcfymasaapi.interfaces.IterableEx;
import net.fabricmc.fabric.api.event.Event;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.function.Function;

public class UnregistrableRegistryBase<RUNNER, SUBSCRIBER> implements IUnregistrableRegistryBase<RUNNER, SUBSCRIBER> {
	private final LinkedHashSet<SUBSCRIBER> callbacks = new LinkedHashSet<>();
	private final LinkedHashSet<SUBSCRIBER> newRegistrars = new LinkedHashSet<>();
	public final RUNNER runner;
	private Iterator<SUBSCRIBER> generateIterator() {
		applyNewRegistrars();
		return callbacks.iterator();
	}
	public UnregistrableRegistryBase(Function<IterableEx<SUBSCRIBER>, RUNNER> runner){
		this.runner = runner.apply(this::generateIterator);
	}
	public UnregistrableRegistryBase(Function<IterableEx<SUBSCRIBER>, RUNNER> runner, Event<RUNNER> autoRegisterEvent){
		this(runner);
		autoRegisterEvent.register(runner());
	}
	private void applyNewRegistrars() {
		if(newRegistrars.isEmpty()) return;
		for(var callback : newRegistrars) {
			if(callbacks.contains(callback)) callbacks.remove(callback);
			else callbacks.add(callback);
		}
		newRegistrars.clear();
	}
	private boolean newRegistrable(SUBSCRIBER callback, boolean add) {
		if(add) return newRegistrars.add(callback);
		else return newRegistrars.remove(callback);
	}
	@Override public boolean register(SUBSCRIBER callback, boolean register){
		return newRegistrable(callback, register != callbacks.contains(callback));
	}
	@Override public boolean isEmpty(){
		applyNewRegistrars();
		return callbacks.isEmpty();
	}
	@Override public RUNNER runner(){ return runner; }
}
