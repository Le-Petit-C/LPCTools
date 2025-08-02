package lpctools.lpcfymasaapi.interfaces;

import lpctools.lpcfymasaapi.UnregistrableRegistry;

@SuppressWarnings("UnusedReturnValue")
public interface IUnregistrableRegistry<T> {
    boolean register(T callback, boolean register);
    boolean isEmpty();
    T run();
    default boolean register(T callback){return register(callback, true);}
    default boolean unregister(T callback){return register(callback, false);}
    interface IterableEx<U> extends Iterable<U>{
        default <V> V combineResults(V startValue, UnregistrableRegistry.Combiner<V, U, V> combiner){
            V value = startValue;
            for(U u : this) value = combiner.combine(value, u);
            return value;
        }
    }
    interface Combiner<U, V, W>{ W combine(U v1, V v2);}
}
