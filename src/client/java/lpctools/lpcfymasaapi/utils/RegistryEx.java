package lpctools.lpcfymasaapi.utils;

import java.util.LinkedHashSet;
import java.util.function.Consumer;

public class RegistryEx<T> {
    public final LinkedHashSet<T> callbacks = new LinkedHashSet<>();
    public boolean register(T callback){return callbacks.add(callback);}
    public boolean unregister(T callback){return callbacks.remove(callback);}
    public void run(Consumer<T> runner){for (T callback : callbacks) runner.accept(callback);}
}
