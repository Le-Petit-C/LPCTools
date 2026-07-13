package lpctools.lpcfymasaapi.interfaces;

import lpctools.util.javaex.ToBooleanFunction;

import java.util.function.BiFunction;

@SuppressWarnings("unused")
public interface IterableEx<U> extends Iterable<U> {
	default <V> V combineResults(V startValue, BiFunction<V, U, V> combiner) {
		V value = startValue;
		for (U u : this) value = combiner.apply(value, u);
		return value;
	}

	default boolean andCircuit(ToBooleanFunction<U> booleanFunction) {
		for (U u : this) {
			if (!booleanFunction.applyAsBoolean(u))
				return false;
		}
		return true;
	}

	default boolean andNonCircuit(ToBooleanFunction<U> booleanFunction) {
		boolean res = true;
		for (U u : this)
			res &= booleanFunction.applyAsBoolean(u);
		return res;
	}

	default boolean orCircuit(ToBooleanFunction<U> booleanFunction) {
		for (U u : this) {
			if (booleanFunction.applyAsBoolean(u))
				return true;
		}
		return false;
	}

	default boolean orNonCircuit(ToBooleanFunction<U> booleanFunction) {
		boolean res = false;
		for (U u : this)
			res |= booleanFunction.applyAsBoolean(u);
		return res;
	}
}
