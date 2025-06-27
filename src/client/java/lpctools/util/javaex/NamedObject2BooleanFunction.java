package lpctools.util.javaex;

public interface NamedObject2BooleanFunction<T> extends NamedFunction<T, Boolean>{
    boolean booleanApply(T v);
    @Deprecated @Override default Boolean apply(T v){return booleanApply(v);}
}
