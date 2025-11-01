package lpctools.script;

import java.util.ArrayList;

public class RuntimeVariableMap {
	ArrayList<Object> values = new ArrayList<>();
	RuntimeVariableMap(CompileEnvironment compiledEnvironment){
		values.ensureCapacity(compiledEnvironment.variableCount());
	}
}
