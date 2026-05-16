package lpctools.lpcfymasaapi.interfaces;

import lpctools.lpcfymasaapi.UnregistrableRegistry;
import lpctools.util.javaex.ToBooleanFunction;

@SuppressWarnings("UnusedReturnValue")
public interface IUnregistrableRegistry<T> {
    boolean register(T callback, boolean register);
    boolean isEmpty();
    T runner();
    default boolean register(T callback){return register(callback, true);}
    default boolean unregister(T callback){return register(callback, false);}
    @SuppressWarnings("unused")
	interface IterableEx<U> extends Iterable<U>{
        default <V> V combineResults(V startValue, UnregistrableRegistry.Combiner<V, U, V> combiner){
            V value = startValue;
            for(U u : this) value = combiner.combine(value, u);
            return value;
        }
        default boolean andCircuit(ToBooleanFunction<U> booleanFunction) {
            for(U u : this) {
                if(!booleanFunction.applyAsBoolean(u))
                    return false;
            }
            return true;
        }
        default boolean andNonCircuit(ToBooleanFunction<U> booleanFunction) {
            boolean res = true;
            for(U u : this)
                res &= booleanFunction.applyAsBoolean(u);
            return res;
        }
        default boolean orCircuit(ToBooleanFunction<U> booleanFunction) {
            for(U u : this) {
                if(booleanFunction.applyAsBoolean(u))
                    return true;
            }
            return false;
        }
        default boolean orNonCircuit(ToBooleanFunction<U> booleanFunction) {
            boolean res = false;
            for(U u : this)
                res |= booleanFunction.applyAsBoolean(u);
            return res;
        }
    }
    interface Combiner<U, V, W>{ W combine(U v1, V v2);}
}
