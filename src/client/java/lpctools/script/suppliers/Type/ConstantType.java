package lpctools.script.suppliers.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import lpctools.lpcfymasaapi.screen.ChooseScreen;
import lpctools.script.*;
import lpctools.script.editScreen.ScriptDisplayWidget;
import lpctools.script.editScreen.WidthAutoAdjustButtonGeneric;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.suppliers.ScriptSupplierLake;
import lpctools.script.suppliers.ScriptType;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import static lpctools.lpcfymasaapi.LPCConfigUtils.warnFailedLoadingConfig;

public class ConstantType extends AbstractScript implements ITypeSupplier {
	
	protected ScriptType type = ScriptType.getType(Object.class);
	private Iterable<?> cachedWidgets = null;
	
	public ConstantType(IScriptWithSubScript parent) {super(parent);}
	
	@Override public @NotNull ScriptNotNullSupplier<ScriptType>
	compileNotNull(CompileEnvironment environment) {
		final ScriptType cachedType = type;
		return map->cachedType;
	}
	
	@Override public @Nullable Iterable<?> getWidgets() {
		if(cachedWidgets == null){
			var selectButton = new WidthAutoAdjustButtonGeneric(getDisplayWidget(), 0, 0, 20, type.name().getString(), null);
			selectButton.setActionListener((button, mouseButton)->{
				HashMap<String, ChooseScreen.OptionCallback<ConstantType>> options = new LinkedHashMap<>();
				LinkedHashMap<String, String> selectTree = new LinkedHashMap<>();
				for(var type : ScriptSupplierLake.typeIdMap.values()){
					options.put(type.name().getString(), (b, m, constantType) -> {
						constantType.type = type;
						selectButton.setDisplayString(type.name().getString());
						selectButton.updateDisplayString();
						constantType.applyToDisplayWidgetIfNotNull(ScriptDisplayWidget::markUpdateChain);
					});
					selectTree.put(type.name().getString(), type.name().getString());
				}
				ChooseScreen.openChooseScreen(Text.translatable("lpctools.script.suppliers.ScriptType.constantType.selectType.title").getString(),
					true, options, selectTree, this);
			});
			cachedWidgets = List.of(selectButton);
			//TODO: 切换语言后按钮更新文字
		}
		return cachedWidgets;
	}
	
	@Override public @Nullable JsonElement getAsJsonElement() {
		return new JsonPrimitive(type.id());
	}
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {
		if(element == null) return;
		if (element instanceof JsonPrimitive primitive) {
			var id = primitive.getAsString();
			if(ScriptSupplierLake.typeIdMap.containsKey(id)){
				type = ScriptSupplierLake.typeIdMap.get(id);
				return;
			}
		}
		warnFailedLoadingConfig("ConstantType", element);
	}
}
