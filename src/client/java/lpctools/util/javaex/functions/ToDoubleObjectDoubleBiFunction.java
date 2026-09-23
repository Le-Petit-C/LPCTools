package lpctools.util.javaex.functions;

import java.util.function.ToDoubleBiFunction;

public interface ToDoubleObjectDoubleBiFunction<T> extends ToDoubleBiFunction<T, Double> {
	default @Deprecated @Override double applyAsDouble(T t, Double aDouble) { return applyAsDouble(t, (double) aDouble); }
	double applyAsDouble(T t, double aDouble);
}
