package lpctools.script;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class CompileEnvironment {
	private final Object2IntOpenHashMap<String> indexMap = new Object2IntOpenHashMap<>();
	public int variableCount() {return indexMap.size();}
	public VariableReference getVariableReference(String name){
		int index = indexMap.computeIfAbsent(name, k -> indexMap.size());
		return new VariableReference(name, index);
	}
	
	public static class VariableReference {
		public final String name;
		public final int index;
		VariableReference(String name, int index){
			this.name = name;
			this.index = index;
		}
		public void setValue(RuntimeVariableMap runtimeVariableMap, Object value){
			runtimeVariableMap.values.set(index, value);
		}
		public Object getValue(RuntimeVariableMap runtimeVariableMap) {
			return runtimeVariableMap.values.get(index);
		}
	}
}
