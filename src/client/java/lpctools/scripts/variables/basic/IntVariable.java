package lpctools.scripts.variables.basic;

import lpctools.scripts.variables.Variable;

public interface IntVariable extends ReadableIntVariable, Variable<Integer> {
    void setAsInt(int value);
    @Override default void setValue(Integer value){setAsInt(value);}
}
