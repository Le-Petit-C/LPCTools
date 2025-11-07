package lpctools.script.suppliers.Boolean;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptFunction;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import lpctools.script.suppliers.Integer.ConstantInteger;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import static lpctools.lpcfymasaapi.LPCConfigUtils.warnFailedLoadingConfig;

public class CompareIntegers extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IBooleanSupplier {
	protected final SupplierStorage<Integer> integer1 = ofStorage(Integer.class, new ConstantInteger(this),
		Text.translatable("lpctools.script.suppliers.Boolean.compareIntegers.subSuppliers.integer1.name"), "integer1");
	CompareSign compareSign = CompareSign.EQUALS;
	protected final SupplierStorage<Integer> integer2 = ofStorage(Integer.class, new ConstantInteger(this),
		Text.translatable("lpctools.script.suppliers.Boolean.compareIntegers.subSuppliers.integer2.name"), "integer2");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(integer1, integer2);
	
	public static final String compareSignJsonKey = "compareSign";
	
	public CompareIntegers(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @org.jetbrains.annotations.NotNull ScriptFunction<CompileEnvironment.RuntimeVariableMap, Boolean>
	compile(CompileEnvironment variableMap) {
		var integer1Supplier = integer1.get().compile(variableMap);
		var sign = compareSign;
		var integer2Supplier = integer2.get().compile(variableMap);
		return map->{
			var integer1 = integer1Supplier.scriptApply(map);
			if(integer1 == null) throw ScriptRuntimeException.nullPointer(this);
			var integer2 = integer2Supplier.scriptApply(map);
			if(integer2 == null) throw ScriptRuntimeException.nullPointer(this);
			return sign.compareIntegers(integer1, integer2);
		};
	}
	
	@Override protected ArrayList<Object> buildWidgets(ArrayList<Object> res) {
		super.buildWidgets(res);
		var button = new ButtonGeneric(0, 0, 50, 20, compareSign.signString());
		button.setActionListener(
			(bt, mouseButton)->{
				compareSign = compareSign.nextCompareSign();
				button.setDisplayString(compareSign.signString());
			}
		);
		res.add(1, button);
		return res;
	}
	
	@Override public @Nullable JsonElement getAsJsonElement() {
		JsonObject res = new JsonObject();
		integer1.getAsSubJsonElement(res);
		res.addProperty(compareSignJsonKey, compareSign.signString());
		integer2.getAsSubJsonElement(res);
		return res;
	}
	
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {
		if(element == null) return;
		if(!(element instanceof JsonObject object)){
			warnFailedLoadingConfig(className, element);
			return;
		}
		integer1.setValueFromSubJsonElement(object);
		if(object.get(compareSignJsonKey) instanceof JsonElement compareSignElement){
			if(compareSignElement instanceof JsonPrimitive compareSignPrimitive &&
				CompareSign.getCompareSign(compareSignPrimitive.getAsString()) instanceof CompareSign _compareSign){
				this.compareSign = _compareSign;
			}
			else warnFailedLoadingConfig(className + '.' + compareSignJsonKey, element);
		}
		integer2.setValueFromSubJsonElement(object);
	}
}
