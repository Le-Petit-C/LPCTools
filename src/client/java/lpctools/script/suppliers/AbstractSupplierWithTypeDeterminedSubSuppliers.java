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
	protected String className;
	private final HashMap<IScript, SupplierStorage<?>> storageMap = new HashMap<>();
	
	protected AbstractSupplierWithTypeDeterminedSubSuppliers(IScriptWithSubScript parent, String className){
		super(parent);
		this.className = className;
	}
	protected AbstractSupplierWithTypeDeterminedSubSuppliers(IScriptWithSubScript parent){
		this(parent, "");
		this.className = getClass().getSimpleName();
	}
	
	protected abstract SubSupplierEntry<?>[] getSubSuppliers();
	@Override public @NotNull List<? extends IScript> getSubScripts(){
		var subSuppliers = getSubSuppliers();
		ArrayList<IScript> res = new ArrayList<>(subSuppliers.length);
		for(int i = 0; i < subSuppliers.length; ++i)
			res.set(i, subSuppliers[i].storage.get());
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
	
	//不作只有一个元素时的优化（指不使用JsonObject再包装一层）
	//万一未来的某一天一些方法的参数数量会变呢
	@Override public @Nullable JsonElement getAsJsonElement() {
		JsonObject res = new JsonObject();
		for(var entry : getSubSuppliers())
			res.add(entry.jsonKey, ScriptSupplierLake.getJsonEntryFromSupplier(entry.storage.get()));
		return res;
	}
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {
		if(element == null) return;
		if (!(element instanceof JsonObject object)) {
			warnFailedLoadingConfig("AttackEntity", element);
			return;
		}
		for(var entry : getSubSuppliers())
			entry.storage.loadOrWarn(object.get(entry.jsonKey), className + '.' + entry.jsonKey);
	}
	
	protected ArrayList<Object> buildWidgets(ArrayList<Object> res){
		for(var entry : getSubSuppliers()){
			ButtonBase button = new WidthAutoAdjustButtonGeneric(getDisplayWidget(), 0, 0, 20, entry.storage.argumentName.getString(), null);
			button.setActionListener((b, m)->entry.chooseSupplier(this));
			res.add(button);
			//但是这样的问题是用户更改语言后按钮名称不会更新
			//TODO
		}
		return res;
	}
	
	public record SubSupplierEntry<T>(SupplierStorage<T> storage, String jsonKey) {
		public void chooseSupplier(IScriptWithSubScript parent){
			ScriptSupplierLake.chooseSupplier(storage.clazz, parent, s -> {
				storage.set(s);
				parent.applyToDisplayWidgetIfNotNull(ScriptDisplayWidget::markUpdateChain);
			});
		}
	}
	
	protected final <T> SupplierStorage<T> ofStorage(Class<T> clazz, IScriptSupplier<? extends T> supplier, Text argumentName){
		return new SupplierStorage<>(clazz, supplier, argumentName);
	}
	
	protected final SubSupplierBuilder subSupplierBuilder(){
		return new SubSupplierBuilder();
	}
	
	protected class SupplierStorage<T> {
		private IScriptSupplier<? extends T> supplier;
		public final Class<T> clazz;
		public final Text argumentName;
		public void set(IScriptSupplier<? extends T> supplier){
			storageMap.remove(this.supplier);
			this.supplier = supplier;
			storageMap.put(supplier, this);
		}
		public IScriptSupplier<? extends T> get(){return supplier;}
		public SupplierStorage(Class<T> clazz, IScriptSupplier<? extends T> supplier, Text argumentName) {
			this.clazz = clazz;
			this.supplier = supplier;
			this.argumentName = argumentName;
			storageMap.put(supplier, this);
		}
		public void loadOrWarn(JsonElement jsonElement, String warnString){
			ScriptSupplierLake.loadSupplierOrWarn(
				jsonElement, clazz, AbstractSupplierWithTypeDeterminedSubSuppliers.this,
				res -> supplier = res, warnString);
		}
	}
	
	protected static class SubSupplierBuilder{
		ArrayList<SubSupplierEntry<?>> entries = new ArrayList<>();
		public <T> SubSupplierBuilder addEntry(SupplierStorage<T> storage, String jsonKey) {
			entries.add(new SubSupplierEntry<>(storage, jsonKey));
			return this;
		}
		public SubSupplierEntry<?>[] build(){
			return entries.toArray(new SubSupplierEntry<?>[0]);
		}
	}
}