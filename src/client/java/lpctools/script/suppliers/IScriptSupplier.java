package lpctools.script.suppliers;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScript;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.*;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public interface IScriptSupplier<T> extends IScript {
	@Override default Text getName(){
		return ScriptSupplierLake.getSupplierRegistration(this).displayName;
	}
	
	Class<? extends T> getSuppliedClass();
	@NotNull ScriptNullableSupplier<T>
	compile(CompileEnvironment environment);
	
	@NotNull default ScriptNotNullSupplier<T>
	compileCheckedNotNull(CompileEnvironment variableMap){
		if(this instanceof IScriptSupplierNotNull<T> scriptSupplierNotNull)
			return scriptSupplierNotNull.compileNotNull(variableMap);
		else {
			var func = compile(variableMap);
			if(func instanceof ScriptNotNullSupplier<T> notNullFunction)
				return notNullFunction;
			else return map->{
				var res = func.scriptApply(map);
				if(res == null) throw ScriptRuntimeException.nullPointer(this);
				return res;
			};
		}
	}
	
	//static之后再有一个default+非static的只是为了方便调用
	
	static @NotNull ScriptBooleanSupplier
	staticCompileCheckedBoolean(IScriptSupplier<? extends Boolean> script, CompileEnvironment environment){
		if(script instanceof IScriptSupplierBoolean scriptSupplierDouble)
			return scriptSupplierDouble.compileBoolean(environment);
		else {
			var func = script.compile(environment);
			if(func instanceof ScriptBooleanSupplier booleanFunction)
				return booleanFunction;
			else if(func instanceof ScriptNotNullSupplier<? extends Boolean> booleanFunction)
				return booleanFunction::scriptApply;
			else return map->{
				var res = func.scriptApply(map);
				if(res == null) throw ScriptRuntimeException.nullPointer(script);
				return res;
			};
		}
	}
	
	default @NotNull ScriptBooleanSupplier
	compileCheckedBoolean(IScriptSupplier<? extends Boolean> script, CompileEnvironment environment) {
		return staticCompileCheckedBoolean(script, environment);
	}
	
	static @NotNull ScriptIntegerSupplier
	staticCompileCheckedInteger(IScriptSupplier<? extends Integer> script, CompileEnvironment environment){
		if(script instanceof IScriptSupplierInteger scriptSupplierInteger)
			return scriptSupplierInteger.compileInteger(environment);
		else {
			var func = script.compile(environment);
			if(func instanceof ScriptIntegerSupplier integerFunction)
				return integerFunction;
			else if(func instanceof ScriptNotNullSupplier<? extends Integer> integerFunction)
				return integerFunction::scriptApply;
			else return map->{
				var res = func.scriptApply(map);
				if(res == null) throw ScriptRuntimeException.nullPointer(script);
				return res;
			};
		}
	}
	
	default @NotNull ScriptIntegerSupplier
	compileCheckedInteger(IScriptSupplier<? extends Integer> script, CompileEnvironment environment){
		return staticCompileCheckedInteger(script, environment);
	}
	
	static @NotNull ScriptDoubleSupplier
	staticCompileCheckedDouble(IScriptSupplier<? extends Double> script, CompileEnvironment environment){
		if(script instanceof IScriptSupplierDouble scriptSupplierDouble)
			return scriptSupplierDouble.compileDouble(environment);
		else {
			var func = script.compile(environment);
			if(func instanceof ScriptDoubleSupplier doubleFunction)
				return doubleFunction;
			else if(func instanceof ScriptNotNullSupplier<? extends Double> doubleFunction)
				return doubleFunction::scriptApply;
			else return map->{
				var res = func.scriptApply(map);
				if(res == null) throw ScriptRuntimeException.nullPointer(script);
				return res;
			};
		}
	}
	
	default @NotNull ScriptDoubleSupplier
	compileCheckedDouble(IScriptSupplier<? extends Double> script, CompileEnvironment environment){
		return staticCompileCheckedDouble(script, environment);
	}
}
