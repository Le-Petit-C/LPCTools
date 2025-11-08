package lpctools.script.suppliers.BlockPos;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import lpctools.script.AbstractScript;
import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.editScreen.ScriptDisplayWidget;
import lpctools.script.editScreen.WidthAutoAdjustTextField;
import lpctools.script.runtimeInterfaces.ScriptFunction;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

import static lpctools.lpcfymasaapi.LPCConfigUtils.warnFailedLoadingConfig;

public class ConstantBlockPos extends AbstractScript implements IBlockPosSupplier {
	private final int[] val = new int[3];
	private static final char[] valDesc = {'x', 'y', 'z'};
	private WidthAutoAdjustTextField @Nullable [] textFields = null;
	public ConstantBlockPos(IScriptWithSubScript parent) {super(parent);}
	@Override public @NotNull ScriptFunction<CompileEnvironment.RuntimeVariableMap, BlockPos>
	compile(CompileEnvironment variableMap) {
		BlockPos res = new BlockPos(val[0], val[1], val[2]);
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
				val[i] = primitive.getAsInt();
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
					try {val[idx] = Integer.parseInt(text);
					} catch (NumberFormatException ignored) {}
					applyToDisplayWidgetIfNotNull(ScriptDisplayWidget::markUpdateChain);
					textFields[idx].setText(String.valueOf(val[idx]));
				});
			}
		}
		return textFields;
	}
}
