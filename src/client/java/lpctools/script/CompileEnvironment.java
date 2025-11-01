package lpctools.script;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class CompileEnvironment {
	private final HashMap<String, VariableReference> indexMap = new HashMap<>();
	private final Function<String, VariableReference> variableReferenceFactory =
			name -> new VariableReference(name, indexMap.size());
	public VariableReference getVariableReference(String name){
		return indexMap.computeIfAbsent(name, variableReferenceFactory);
	}
	public Supplier<RuntimeVariableMap> createRuntimeVariableMapSupplier(){
		int size = indexMap.size();
		return ()->new RuntimeVariableMap(size);
	}
	
	public static class VariableReference {
		public final String name;
		public final int index;
		VariableReference(String name, int index){
			this.name = name;
			this.index = index;
		}
		public void setValue(RuntimeVariableMap runtimeVariableMap, Object value){
			runtimeVariableMap.values[index] = value;
		}
		public Object getValue(RuntimeVariableMap runtimeVariableMap) {
			return runtimeVariableMap.values[index];
		}
	}
	
	public static class RuntimeVariableMap {
		private final Object[] values;
		private RuntimeVariableMap(int size){
			values = new Object[size];
		}
	}
}
