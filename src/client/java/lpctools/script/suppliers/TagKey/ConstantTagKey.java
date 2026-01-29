package lpctools.script.suppliers.TagKey;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.suppliers.AbstractOperatorResultSupplier;
import lpctools.util.operatorUtils.ArrayListSignInfo;
import lpctools.util.operatorUtils.Operators;
import net.minecraft.registry.tag.TagKey;
import org.jetbrains.annotations.NotNull;

public class ConstantTagKey extends AbstractOperatorResultSupplier<ConstantTagKey.ITagKeySupplierOperator> implements ITagKeySupplier {
	public interface ITagKeySupplierOperator extends Operators.SignBase {
		TagKey<?> getTagKey();
	}
	
	public ConstantTagKey(IScriptWithSubScript parent) {
		super(parent, tagKeyInfo, 0);
	}
	
	protected final SupplierStorage<?>[] subSuppliers = ofStorages();
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@SuppressWarnings("rawtypes")
	@Override public @NotNull ScriptNotNullSupplier<TagKey>
	compileNotNull(CompileEnvironment environment) {
		var val = operatorSign.getTagKey();
		return map->val;
	}
	
	public static void addTagKey(TagKey<?> tagKey){
		tagKeyInfo.addSign(new ITagKeySupplierOperator() {
			@Override public String idString() { return tagKey.id().toString(); }
			@Override public TagKey<?> getTagKey() {return tagKey;}
		});
	}
	
	private static final ArrayListSignInfo<ITagKeySupplierOperator> tagKeyInfo
		= new ArrayListSignInfo<>(){
		@Override public String getDisplayString(ITagKeySupplierOperator key)
		{return key.getTagKey().id().toString();}
	};
}
