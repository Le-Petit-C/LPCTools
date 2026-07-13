package lpctools.util.javaex;

import java.util.function.Function;

public interface Object2BooleanFunction<T> extends Function<T, Boolean> {
    boolean getBoolean(T v);
    @Deprecated @Override default Boolean apply(T v){return getBoolean(v);}
}
