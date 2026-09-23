package lpctools.lpcfymasaapi.interfaces;

import lpctools.util.javaex.ToBooleanFunction;

import java.util.function.BiFunction;

@SuppressWarnings("unused")
public interface IterableEx<U> extends Iterable<U> {
	static <U, V> V combineResults(Iterable<U> iterable, V startValue, BiFunction<V, U, V> combiner) {
		V value = startValue;
		for (U u : iterable) value = combiner.apply(value, u);
		return value;
	}
	default <V> V combineResults(V startValue, BiFunction<V, U, V> combiner) { return combineResults(this, startValue, combiner); }

	static <U> boolean andCircuit(Iterable<U> iterable, ToBooleanFunction<U> booleanFunction) {
		for (U u : iterable) {
			if (!booleanFunction.applyAsBoolean(u))
				return false;
		}
		return true;
	}
	default boolean andCircuit(ToBooleanFunction<U> booleanFunction) { return andCircuit(this, booleanFunction); }

	static <U> boolean andNonCircuit(Iterable<U> iterable, ToBooleanFunction<U> booleanFunction) {
		boolean res = true;
		for (U u : iterable)
			res &= booleanFunction.applyAsBoolean(u);
		return res;
	}
	default boolean andNonCircuit(ToBooleanFunction<U> booleanFunction) { return andNonCircuit(this, booleanFunction); }

	static <U> boolean orCircuit(Iterable<U> iterable, ToBooleanFunction<U> booleanFunction) {
		for (U u : iterable) {
			if (booleanFunction.applyAsBoolean(u))
				return true;
		}
		return false;
	}
	default boolean orCircuit(ToBooleanFunction<U> booleanFunction) { return orCircuit(this, booleanFunction); }

	static <U> boolean orNonCircuit(Iterable<U> iterable, ToBooleanFunction<U> booleanFunction) {
		boolean res = false;
		for (U u : iterable)
			res |= booleanFunction.applyAsBoolean(u);
		return res;
	}
	default boolean orNonCircuit(ToBooleanFunction<U> booleanFunction) { return orNonCircuit(this, booleanFunction); }
}
