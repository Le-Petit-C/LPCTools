package lpctools.script.suppliers.Boolean;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptFunction;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import lpctools.script.suppliers.Random.Null;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import static lpctools.lpcfymasaapi.LPCConfigUtils.warnFailedLoadingConfig;

public class CompareObjects extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IBooleanSupplier {
	protected final SupplierStorage<Object> object1 = ofStorage(Object.class, new Null<>(this, Object.class),
		Text.translatable("lpctools.script.suppliers.Boolean.compareObjects.subSuppliers.object1.name"), "object1");
	CompareSign.ObjectCompareSign compareSign = CompareSign.EQUALS;
	protected final SupplierStorage<Object> object2 = ofStorage(Object.class, new Null<>(this, Object.class),
		Text.translatable("lpctools.script.suppliers.Boolean.compareObjects.subSuppliers.object2.name"), "object2");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(object1, object2);
	
	public static final String compareSignJsonKey = "compareSign";
	
	public CompareObjects(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @org.jetbrains.annotations.NotNull ScriptFunction<CompileEnvironment.RuntimeVariableMap, Boolean>
	compile(CompileEnvironment variableMap) {
		var object1Supplier = object1.get().compile(variableMap);
		var sign = compareSign;
		var object2Supplier = object2.get().compile(variableMap);
		return map->sign.compareObjects(object1Supplier.scriptApply(map), object2Supplier.scriptApply(map));
	}
	
	@Override protected ArrayList<Object> buildWidgets(ArrayList<Object> res) {
		super.buildWidgets(res);
		var button = new ButtonGeneric(0, 0, 50, 20, compareSign.signString());
		button.setActionListener(
			(bt, mouseButton)->{
				compareSign = compareSign.nextObjectCompareSign();
				button.setDisplayString(compareSign.signString());
			}
		);
		res.add(1, button);
		return res;
	}
	
	@Override public @Nullable JsonElement getAsJsonElement() {
		JsonObject res = new JsonObject();
		object1.getAsSubJsonElement(res);
		res.addProperty(compareSignJsonKey, compareSign.signString());
		object2.getAsSubJsonElement(res);
		return res;
	}
	
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {
		if(element == null) return;
		if(!(element instanceof JsonObject object)){
			warnFailedLoadingConfig(className, element);
			return;
		}
		object1.setValueFromSubJsonElement(object);
		if(object.get(compareSignJsonKey) instanceof JsonElement compareSignElement){
			if(compareSignElement instanceof JsonPrimitive compareSignPrimitive &&
				CompareSign.getObjectCompareSign(compareSignPrimitive.getAsString()) instanceof CompareSign.ObjectCompareSign objectCompareSign){
				this.compareSign = objectCompareSign;
			}
			else warnFailedLoadingConfig(className + '.' + compareSignJsonKey, element);
		}
		object2.setValueFromSubJsonElement(object);
	}
}
