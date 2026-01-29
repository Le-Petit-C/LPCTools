package lpctools.script.suppliers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.editScreen.WidthAutoAdjustButtonGeneric;
import lpctools.util.operatorUtils.Operators;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import static lpctools.lpcfymasaapi.LPCConfigUtils.warnFailedLoadingConfig;

public abstract class AbstractOperatorResultSupplier<T extends Operators.SignBase> extends AbstractSupplierWithTypeDeterminedSubSuppliers {
	protected T operatorSign;
	protected final Operators.ISignInfo<T> signInfo;
	protected final int signPosition;
	
	public static final String compareSignJsonKey = "compareSign";
	
	public AbstractOperatorResultSupplier(IScriptWithSubScript parent, T defaultSign, Operators.ISignInfo<T> signInfo, int signPosition) {
		super(parent);
		operatorSign = defaultSign;
		this.signInfo = signInfo;
		this.signPosition = signPosition;
	}
	
	public AbstractOperatorResultSupplier(IScriptWithSubScript parent, Operators.ISignInfo<T> signInfo, int signPosition) {
		this(parent, signInfo.getDefault(), signInfo, signPosition);
	}
	
	@Override protected ArrayList<Object> buildWidgets(ArrayList<Object> res) {
		super.buildWidgets(res);
		var button = new WidthAutoAdjustButtonGeneric(getDisplayWidget(), 0, 0, 20, signInfo.getDisplayString(operatorSign), null);
		button.setActionListener(
			(bt, mouseButton)->signInfo.mouseButtonClicked(operatorSign, mouseButton == 0, val -> {
				operatorSign = val;
				button.setDisplayString(signInfo.getDisplayString(operatorSign));
			})
		);
		res.add(signPosition, button);
		return res;
	}
	
	@Override public @Nullable JsonElement getAsJsonElement() {
		JsonObject res = getASWTDSSAsJsonElement(this);
		res.addProperty(compareSignJsonKey, operatorSign.idString());
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
				this.operatorSign = _compareSign;
			}
			else warnFailedLoadingConfig(className + '.' + compareSignJsonKey, element);
		}
	}
}
