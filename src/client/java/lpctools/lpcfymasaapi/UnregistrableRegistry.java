package lpctools.lpcfymasaapi;

import lpctools.lpcfymasaapi.interfaces.IUnregistrableRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.function.Function;

public class UnregistrableRegistry<T> implements IUnregistrableRegistry<T> {
    public final @NotNull LinkedHashSet<T> callbacks = new LinkedHashSet<>();
    public final T runner;
    public UnregistrableRegistry(Function<IterableEx<T>, T> runner){
        this.runner = runner.apply(callbacks::iterator);
    }
    @Override public boolean register(T callback, boolean register){
        if(register) return callbacks.add(callback);
        else return callbacks.remove(callback);
    }
    @Override public boolean isEmpty(){return callbacks.isEmpty();}
    @Override public T run(){return runner;}
}
