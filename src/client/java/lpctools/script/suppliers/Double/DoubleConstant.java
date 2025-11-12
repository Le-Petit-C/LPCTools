package lpctools.script.suppliers.Double;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import lpctools.script.AbstractScript;
import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptDoubleSupplier;
import lpctools.util.Functions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static lpctools.lpcfymasaapi.LPCConfigUtils.warnFailedLoadingConfig;

public class DoubleConstant extends AbstractScript implements IDoubleSupplier {
	public DoubleConstant(IScriptWithSubScript parent) {super(parent);}
	
	protected Functions.DoubleConstant sign;
	protected @Nullable ButtonGeneric button;
	
	@Override public @Nullable Iterable<?> getWidgets() {
		if(button == null){
			button = new ButtonGeneric(0, 0, 50, 20, sign.signString());
			button.setActionListener(
				(bt, mouseButton)->{
					sign = Functions.doubleConstantInfo.cycleSign(sign, mouseButton == 0);
					button.setDisplayString(sign.signString());
				}
			);
		}
		return List.of(button);
	}
	
	@Override public @Nullable JsonElement getAsJsonElement() {
		return new JsonPrimitive(sign.signString());
	}
	
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {
		if(element == null) return;
		if(element instanceof JsonPrimitive primitive &&
			Functions.doubleConstantInfo.get(primitive.getAsString()) instanceof Functions.DoubleConstant _sign){
			this.sign = _sign;
		}
		else warnFailedLoadingConfig("DoubleConstant", element);
	}
	
	@Override public @NotNull ScriptDoubleSupplier
	compileDouble(CompileEnvironment environment) {
		double val = sign.getDouble();
		return map->val;
	}
}
