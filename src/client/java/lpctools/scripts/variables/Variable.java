package lpctools.scripts.variables;

public interface Variable<T> extends ReadableVariable<T>{
     void setValue(T value);
}
