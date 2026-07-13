package lpctools.script.suppliers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
			JsonObject object = ScriptSupplierLake.getJsonEntryFromSupplier(sub);
			if(res == null) res = new JsonArray();
			res.add(object);
		}
		return res;
	}
	protected void loadSubSuppliersFromJsonArray(JsonArray array){
		subScripts.clear();
		for(var element : array){
			var res = ScriptSupplierLake.loadSupplierFromJsonEntry(element, getArgumentClass(), this);
			if(res != null) subScripts.add(res);
			else warnFailedLoadingConfig("script_option", element);
		}
	}
}
