package lpctools.lpcfymasaapi;

import lpctools.lpcfymasaapi.interfaces.IUnregistrableRegistry;

import java.util.LinkedHashSet;
import java.util.function.Function;

public class UnregistrableRegistry<T> implements IUnregistrableRegistry<T> {
    public final LinkedHashSet<T> callbacks = new LinkedHashSet<>();
    private final LinkedHashSet<T> newRegistrables = new LinkedHashSet<>();
    public final T runner;
    public UnregistrableRegistry(Function<IterableEx<T>, T> runner){
        this.runner = runner.apply(callbacks::iterator);
    }
    private void applyNewRegistrable() {
        for(var callback : newRegistrables) {
            if(callbacks.contains(callback)) callbacks.remove(callback);
            else callbacks.add(callback);
        }
        newRegistrables.clear();
    }
    private boolean newRegistrable(T callback, boolean add) {
        if(add) return newRegistrables.add(callback);
        else return newRegistrables.remove(callback);
    }
    @Override public boolean register(T callback, boolean register){
        return newRegistrable(callback, register != callbacks.contains(callback));
    }
    @Override public boolean isEmpty(){
        applyNewRegistrable();
        return callbacks.isEmpty();
    }
    @Override public T run(){
        applyNewRegistrable();
        return runner;
    }
}
