package lpctools.scripts.runners.variables;

import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.util.ArrayList;

public class CompiledVariableList extends ArrayList<Object>{
	private final IntArrayList stack = new IntArrayList();
	public void push(){stack.add(size());}
	public void pop(){removeRange(stack.removeLast(), size());}
	@SuppressWarnings("unchecked")
	public <T> T getVariable(int index){return (T) get(index);}
}
