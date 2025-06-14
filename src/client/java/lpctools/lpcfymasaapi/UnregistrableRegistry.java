package lpctools.lpcfymasaapi;

import java.util.LinkedHashSet;
import java.util.function.Function;

public class UnregistrableRegistry<T> {
    public final LinkedHashSet<T> callbacks = new LinkedHashSet<>();
    public Function<Iterable<T>, T> runner;
    public UnregistrableRegistry(Function<Iterable<T>, T> runner){this.runner = runner;}
    public boolean register(T callback){return callbacks.add(callback);}
    @SuppressWarnings("UnusedReturnValue")
    public boolean unregister(T callback){return callbacks.remove(callback);}
    public boolean isEmpty(){return callbacks.isEmpty();}
    public T run(){return runner.apply(callbacks);}
}
