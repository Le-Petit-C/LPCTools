package lpctools.script.suppliers.Vec3d;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import lpctools.script.AbstractScript;
import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.editScreen.ScriptDisplayWidget;
import lpctools.script.editScreen.WidthAutoAdjustTextField;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

import static lpctools.lpcfymasaapi.LPCConfigUtils.warnFailedLoadingConfig;

public class ConstantVec3d extends AbstractScript implements IVec3dSupplier {
	private final double[] val = new double[3];
	private static final char[] valDesc = {'x', 'y', 'z'};
	private WidthAutoAdjustTextField@Nullable [] textFields = null;
	public ConstantVec3d(IScriptWithSubScript parent) {super(parent);}
	@Override public @NotNull ScriptNotNullSupplier<Vec3d>
	compileNotNull(CompileEnvironment environment) {
		Vec3d res = new Vec3d(val[0], val[1], val[2]);
		return map->res;
	}
	
	@Override public @Nullable JsonElement getAsJsonElement() {
		JsonArray res = new JsonArray();
		for(var v : val) res.add(v);
		return res;
	}
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {
		if(element == null) return;
		if(!(element instanceof JsonArray array)){
			warnFailedLoadingConfig("ConstantVec3d", element);
			return;
		}
		int arrSize = array.size();
		for(int i = 0; i < 3; ++i){
			if(arrSize > i && array.get(i) instanceof JsonPrimitive primitive && primitive.isNumber()){
				val[i] = primitive.getAsDouble();
				if(textFields != null) textFields[i].setText(String.valueOf(val[i]));
			}
			else warnFailedLoadingConfig("ConstantVec3d." + valDesc[i], element);
		}
	}
	@Override public @Nullable Iterable<?> getWidgets() {return Arrays.asList(getTextFields());}
	
	private @NotNull WidthAutoAdjustTextField[] getTextFields(){
		if(textFields == null){
			textFields = new WidthAutoAdjustTextField[3];
			for(int i = 0; i < 3; ++i){
				final int idx = i;
				textFields[idx] = new WidthAutoAdjustTextField(
					getDisplayWidget(), 50, String.valueOf(val[idx]), text->{
					try {val[idx] = Double.parseDouble(text);
					} catch (NumberFormatException ignored) {}
					textFields[idx].setText(String.valueOf(val[idx]));
					applyToDisplayWidgetIfNotNull(ScriptDisplayWidget::markUpdateChain);
				});
			}
		}
		return textFields;
	}
}
