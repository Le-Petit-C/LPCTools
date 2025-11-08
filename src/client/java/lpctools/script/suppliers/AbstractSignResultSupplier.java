package lpctools.script.suppliers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import lpctools.script.IScriptWithSubScript;
import lpctools.util.Functions;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import static lpctools.lpcfymasaapi.LPCConfigUtils.warnFailedLoadingConfig;

public abstract class AbstractSignResultSupplier<T extends Functions.SignBase> extends AbstractSupplierWithTypeDeterminedSubSuppliers {
	protected T compareSign;
	protected final Functions.SignInfo<T> signInfo;
	protected final int signPosition;
	
	public static final String compareSignJsonKey = "compareSign";
	
	public AbstractSignResultSupplier(IScriptWithSubScript parent, T defaultSign, Functions.SignInfo<T> signInfo, int signPosition) {
		super(parent);
		compareSign = defaultSign;
		this.signInfo = signInfo;
		this.signPosition = signPosition;
	}
	
	@Override protected ArrayList<Object> buildWidgets(ArrayList<Object> res) {
		super.buildWidgets(res);
		var button = new ButtonGeneric(0, 0, 50, 20, compareSign.signString());
		button.setActionListener(
			(bt, mouseButton)->{
				compareSign = signInfo.cycleSign(compareSign, mouseButton == 0);
				button.setDisplayString(compareSign.signString());
			}
		);
		res.add(signPosition, button);
		return res;
	}
	
	@Override public @Nullable JsonElement getAsJsonElement() {
		JsonObject res = getASWTDSSAsJsonElement(this);
		res.addProperty(compareSignJsonKey, compareSign.signString());
		return res;
	}
	
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {
		if(element == null) return;
		if(!(element instanceof JsonObject object)){
			warnFailedLoadingConfig(className, element);
			return;
		}
		setASWTDSSValueFromJsonObject(this, object);
		if(object.get(compareSignJsonKey) instanceof JsonElement compareSignElement){
			if(compareSignElement instanceof JsonPrimitive compareSignPrimitive &&
				signInfo.get(compareSignPrimitive.getAsString()) instanceof T _compareSign){
				this.compareSign = _compareSign;
			}
			else warnFailedLoadingConfig(className + '.' + compareSignJsonKey, element);
		}
	}
}
