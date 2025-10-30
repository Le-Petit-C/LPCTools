package lpctools.script.suppliers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lpctools.script.AbstractScriptWithSubScriptMutable;
import lpctools.script.IScriptWithSubScript;

import java.util.function.Consumer;

import static lpctools.lpcfymasaapi.LPCConfigUtils.warnFailedLoadingConfig;

public abstract class AbstractSupplierWithSubScriptMutable<T, U> extends AbstractScriptWithSubScriptMutable<IScriptSupplier<? extends U>> implements IScriptSupplier<T> {
	public AbstractSupplierWithSubScriptMutable(IScriptWithSubScript parent){super(parent);}
	
	public abstract Class<U> getArgumentClass();
	
	@Override public void notifyInsertion(Consumer<IScriptSupplier<? extends U>> callback) {
		ScriptSupplierLake.chooseSupplier(getArgumentClass(), this, callback);
	}
	protected JsonArray getSubSuppliersAsJsonArray(){
		JsonArray res = null;
		for(var sub : getSubScripts()){
			JsonObject object = new JsonObject();
			object.addProperty("id", ScriptSupplierLake.getSupplierId(sub));
			object.add("data", sub.getAsJsonElement());
			if(res == null) res = new JsonArray();
			res.add(object);
		}
		return res;
	}
	protected void loadSubSuppliersFromJsonArray(JsonArray array){
		subScripts.clear();
		for(var element : array){
			boolean succeeded = false;
			if(element instanceof JsonObject object){
				if(object.get("id") instanceof JsonPrimitive id){
					var reg = ScriptSupplierLake.getSupplierRegistration(id.getAsString());
					var allocator = reg.tryAllocate(getArgumentClass());
					if(allocator != null){
						var res = allocator.allocate(this);
						subScripts.add(res);
						res.setValueFromJsonElement(object.get("data"));
						succeeded = true;
					}
				}
			}
			if(!succeeded) warnFailedLoadingConfig("script_option", element);
		}
	}
}
