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
import lpctools.script.suppliers.Double.ConstantDouble;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import static lpctools.lpcfymasaapi.LPCConfigUtils.warnFailedLoadingConfig;

public class CompareDoubles extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IBooleanSupplier {
	protected final SupplierStorage<Double> double1 = ofStorage(Double.class, new ConstantDouble(this),
		Text.translatable("lpctools.script.suppliers.Boolean.compareDoubles.subSuppliers.double1.name"), "double1");
	CompareSign compareSign = CompareSign.EQUALS;
	protected final SupplierStorage<Double> double2 = ofStorage(Double.class, new ConstantDouble(this),
		Text.translatable("lpctools.script.suppliers.Boolean.compareDoubles.subSuppliers.double2.name"), "double2");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(double1, double2);
	
	public static final String compareSignJsonKey = "compareSign";
	
	public CompareDoubles(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @org.jetbrains.annotations.NotNull ScriptFunction<CompileEnvironment.RuntimeVariableMap, Boolean>
	compile(CompileEnvironment variableMap) {
		var integer1Supplier = double1.get().compile(variableMap);
		var sign = compareSign;
		var integer2Supplier = double2.get().compile(variableMap);
		return map->{
			var integer1 = integer1Supplier.scriptApply(map);
			if(integer1 == null) throw ScriptRuntimeException.nullPointer(this);
			var integer2 = integer2Supplier.scriptApply(map);
			if(integer2 == null) throw ScriptRuntimeException.nullPointer(this);
			return sign.compareDoubles(integer1, integer2);
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
		double1.getAsSubJsonElement(res);
		res.addProperty(compareSignJsonKey, compareSign.signString());
		double2.getAsSubJsonElement(res);
		return res;
	}
	
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {
		if(element == null) return;
		if(!(element instanceof JsonObject object)){
			warnFailedLoadingConfig(className, element);
			return;
		}
		double1.setValueFromSubJsonElement(object);
		if(object.get(compareSignJsonKey) instanceof JsonElement compareSignElement){
			if(compareSignElement instanceof JsonPrimitive compareSignPrimitive &&
				CompareSign.getCompareSign(compareSignPrimitive.getAsString()) instanceof CompareSign _compareSign){
				this.compareSign = _compareSign;
			}
			else warnFailedLoadingConfig(className + '.' + compareSignJsonKey, element);
		}
		double2.setValueFromSubJsonElement(object);
	}
}
