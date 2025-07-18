package lpctools.scripts.variables.basic;

import lpctools.scripts.variables.ReadableVariable;

public interface ReadableIntVariable extends ReadableVariable<Integer> {
    int getAsInt();
    @Override default Integer getValue(){return getAsInt();}
}
