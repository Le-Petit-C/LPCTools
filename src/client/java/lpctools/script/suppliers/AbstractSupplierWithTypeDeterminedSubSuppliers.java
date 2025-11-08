package lpctools.script.suppliers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import lpctools.script.AbstractScriptWithSubScript;
import lpctools.script.IScript;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.editScreen.ScriptDisplayWidget;
import lpctools.script.editScreen.WidthAutoAdjustButtonGeneric;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static lpctools.lpcfymasaapi.LPCConfigUtils.warnFailedLoadingConfig;

public abstract class AbstractSupplierWithTypeDeterminedSubSuppliers extends AbstractScriptWithSubScript {
	protected @Nullable ArrayList<Object> widgets = null;
	protected final String className;
	private final HashMap<IScript, SupplierStorage<?>> storageMap = new HashMap<>();
	
	protected AbstractSupplierWithTypeDeterminedSubSuppliers(IScriptWithSubScript parent){
		super(parent);
		this.className = getClass().getSimpleName();
	}
	
	protected abstract SupplierStorage<?>[] getSubSuppliers();
	@Override public @NotNull List<? extends IScript> getSubScripts(){
		var subSuppliers = getSubSuppliers();
		ArrayList<IScript> res = new ArrayList<>(subSuppliers.length);
		for (SupplierStorage<?> subSupplier : subSuppliers) res.add(subSupplier.get());
		return res;
	}
	
	@Override public @Nullable Iterable<?> getWidgets() {
		if(widgets == null) widgets = buildWidgets(new ArrayList<>());
		return widgets;
	}
	
	@Override public @Nullable Text getSubScriptNamePrefix(IScript subScript) {
		var entry = storageMap.get(subScript);
		if(entry != null) return entry.argumentName;
		else return null;
	}
	
	public static JsonObject getASWTDSSAsJsonElement(AbstractSupplierWithTypeDeterminedSubSuppliers supplier){
		JsonObject res = new JsonObject();
		for(var entry : supplier.getSubSuppliers())
			entry.getAsSubJsonElement(res);
		return res;
	}
	
	public static void setASWTDSSValueFromJsonObject(AbstractSupplierWithTypeDeterminedSubSuppliers supplier, JsonObject object){
		for(var entry : supplier.getSubSuppliers())
			entry.setValueFromJsonElement(object.get(entry.jsonKey));
	}
	
	//不作只有一个元素时的优化（指不使用JsonObject再包装一层）
	//万一未来的某一天一些方法的参数数量会变呢
	@Override public @Nullable JsonElement getAsJsonElement() {
		return getASWTDSSAsJsonElement(this);
	}
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {
		if(element == null) return;
		if (!(element instanceof JsonObject object)) {
			warnFailedLoadingConfig(className, element);
			return;
		}
		setASWTDSSValueFromJsonObject(this, object);
	}
	
	protected ArrayList<Object> buildWidgets(ArrayList<Object> res){
		for(var storage : getSubSuppliers()){
			ButtonBase button = new WidthAutoAdjustButtonGeneric(getDisplayWidget(), 0, 0, 20, storage.argumentName.getString(), null);
			button.setActionListener((b, m)->storage.chooseSupplier(this));
			res.add(button);
			//但是这样的问题是用户更改语言后按钮名称不会更新
			//TODO
		}
		return res;
	}
	
	protected final <T> SupplierStorage<T> ofStorage(Class<T> clazz, IScriptSupplier<? extends T> supplier, Text argumentName, String jsonKey){
		return new SupplierStorage<>(clazz, supplier, argumentName, jsonKey);
	}
	
	protected class SupplierStorage<T> {
		private IScriptSupplier<? extends T> supplier;
		public final Class<T> clazz;
		public final Text argumentName;
		public final String jsonKey;
		public void set(IScriptSupplier<? extends T> supplier){
			storageMap.remove(this.supplier);
			this.supplier = supplier;
			storageMap.put(supplier, this);
		}
		public IScriptSupplier<? extends T> get(){return supplier;}
		public SupplierStorage(Class<T> clazz, IScriptSupplier<? extends T> supplier, Text argumentName, String jsonKey) {
			this.clazz = clazz;
			this.supplier = supplier;
			this.argumentName = argumentName;
			this.jsonKey = jsonKey;
			storageMap.put(supplier, this);
		}
		public JsonElement getAsJsonElement(){
			return ScriptSupplierLake.getJsonEntryFromSupplier(supplier);
		}
		public void getAsSubJsonElement(JsonObject object){
			object.add(jsonKey, getAsJsonElement());
		}
		public void setValueFromJsonElement(JsonElement element){
			ScriptSupplierLake.loadSupplierOrWarn(
				element, clazz, AbstractSupplierWithTypeDeterminedSubSuppliers.this,
				res -> supplier = res, className + '.' + jsonKey);
		}
		public void setValueFromSubJsonElement(JsonObject object){
			setValueFromJsonElement(object.get(jsonKey));
		}
		public void chooseSupplier(IScriptWithSubScript parent){
			ScriptSupplierLake.chooseSupplier(clazz, parent, s -> {
				set(s);
				parent.applyToDisplayWidgetIfNotNull(ScriptDisplayWidget::markUpdateChain);
			});
		}
	}
	
	protected static SupplierStorage<?>[] ofStorages(SupplierStorage<?>... storages){return storages;}
}