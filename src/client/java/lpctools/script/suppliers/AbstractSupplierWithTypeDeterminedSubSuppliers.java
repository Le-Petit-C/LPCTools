package lpctools.script.suppliers;

import fi.dy.masa.malilib.gui.button.ButtonBase;
import lpctools.script.AbstractScriptWithSubScript;
import lpctools.script.IScript;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.editScreen.ScriptDisplayWidget;
import lpctools.script.editScreen.WidthAutoAdjustButtonGeneric;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

public abstract class AbstractSupplierWithTypeDeterminedSubSuppliers extends AbstractScriptWithSubScript {
	
	protected @Nullable ArrayList<Object> widgets = null;
	private final HashMap<IScript, SupplierStorage<?>> storageMap = new HashMap<>();
	
	protected AbstractSupplierWithTypeDeterminedSubSuppliers(IScriptWithSubScript parent){super(parent);}
	
	protected abstract List<SubSupplierEntry<?>> getSubSuppliers();
	
	@Override public @Nullable Iterable<?> getWidgets() {
		if(widgets == null) widgets = buildWidgets(new ArrayList<>());
		return widgets;
	}
	
	@Override public @Nullable Text getSubScriptNamePrefix(IScript subScript) {
		var entry = storageMap.get(subScript);
		if(entry != null) return entry.argumentName;
		else return null;
	}
	
	protected ArrayList<Object> buildWidgets(ArrayList<Object> res){
		for(var entry : getSubSuppliers()){
			ButtonBase button = new WidthAutoAdjustButtonGeneric(getDisplayWidget(), 0, 0, 20, entry.storageSupplier.get().argumentName.getString(), null);
			button.setActionListener((b, m)->entry.chooseSupplier(this));
			res.add(button);
			//但是这样的问题是用户更改语言后按钮名称不会更新
			//TODO
		}
		return res;
	}
	
	public record SubSupplierEntry<T>(Class<T> suppliedClass, Supplier<SupplierStorage<T>> storageSupplier) {
		public void chooseSupplier(IScriptWithSubScript parent){
			ScriptSupplierLake.chooseSupplier(suppliedClass, parent, s -> {
				storageSupplier.get().set(s);
				parent.applyToDisplayWidgetIfNotNull(ScriptDisplayWidget::markUpdateChain);
			});
		}
	}
	
	public class SupplierStorage<T> {
		private IScriptSupplier<? extends T> supplier;
		public final Text argumentName;
		public void set(IScriptSupplier<? extends T> supplier){
			storageMap.remove(this.supplier);
			this.supplier = supplier;
			storageMap.put(supplier, this);
		}
		public IScriptSupplier<? extends T> get(){return supplier;}
		public SupplierStorage(IScriptSupplier<? extends T> supplier, Text argumentName) {
			this.supplier = supplier;
			this.argumentName = argumentName;
			storageMap.put(supplier, this);
		}
	}
	
	public <T> SupplierStorage<T> ofStorage(IScriptSupplier<? extends T> supplier, Text argumentName){
		return new SupplierStorage<>(supplier, argumentName);
	}
}
