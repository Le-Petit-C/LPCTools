package lpctools.scripts.runners.variables;

import fi.dy.masa.litematica.util.ToBooleanFunction;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lpctools.scripts.CompileFailedException;

import java.util.ArrayList;
import java.util.HashMap;

public class VariableMap{
    private final HashMap<String, ArrayList<Variable<?>>> map = new HashMap<>();
    private final Object2IntOpenHashMap<String> indexMap = new Object2IntOpenHashMap<>();
    private final ArrayList<Object2IntOpenHashMap<String>> stack = new ArrayList<>();
    private int stackTopIndex = 0;
    public int get(String key, ToBooleanFunction<Variable<?>> instanceofTest, String targetTypeDescription) throws CompileFailedException {
        Variable<?> res = map.get(key) instanceof ArrayList<Variable<?>> list ? list.getLast() : null;
        if(res == null) throw CompileFailedException.undefinedVariable(key);
        if(!instanceofTest.applyAsBoolean(res)) throw CompileFailedException.notExpectedType(key, res, targetTypeDescription);
        return indexMap.getInt(key);
    }
    public void put(String key, Variable<?> value){
        Object2IntOpenHashMap<String> stackMap = stack.getLast();
        stackMap.put(key, stackMap.getInt(key) + 1);
        map.computeIfAbsent(key, v->new ArrayList<>()).add(value);
        indexMap.put(key, stackTopIndex);
        ++stackTopIndex;
    }
    public void push(){stack.add(new Object2IntOpenHashMap<>());}
    public void pop(){
        stack.removeLast().forEach((key, value)->{
            ArrayList<Variable<?>> list = map.get(key);
            if(list.size() == value) map.remove(key);
            else for(int a = 0; a < value; ++a) list.removeLast();
        });
    }
}
