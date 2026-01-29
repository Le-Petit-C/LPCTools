package lpctools.script.suppliers.Enum;

import com.google.common.collect.ImmutableMap;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import lpctools.lpcfymasaapi.screen.ChooseScreen;
import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.suppliers.AbstractOperatorResultSupplier;
import lpctools.util.DataUtils;
import lpctools.util.operatorUtils.DefaultedSignInfo;
import lpctools.util.operatorUtils.Operators;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;

public class ConstantEnum extends AbstractOperatorResultSupplier<ConstantEnum.IEnumSupplierOperator> implements IEnumSupplier {
	public interface IEnumSupplierOperator extends Operators.SignBase {
		@SuppressWarnings("unused")
		Class<? extends Enum<?>> getEnumClass();
		Enum<?> getEnum();
	}
	
	public ConstantEnum(IScriptWithSubScript parent) {
		super(parent, enumInfo.enumByClass.firstEntry().getValue().firstEntry().getValue().first(), enumInfo, 0);
	}
	
	protected final SupplierStorage<?>[] subSuppliers = ofStorages();
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@SuppressWarnings("rawtypes")
	@Override public @NotNull ScriptNotNullSupplier<Enum>
	compileNotNull(CompileEnvironment environment) {
		Enum<?> val = operatorSign.getEnum();
		return map->val;
	}
	
	/*public static DefaultedSignInfo<IEnumSupplierOperator> enumInfo
		= new DefaultedSignInfo<>();*/
	
	public static class EnumInfo implements Operators.ISignInfo<IEnumSupplierOperator> {
		private final HashMap<String, EnumSupplier> enumByName = new HashMap<>();
		private final TreeMap<String, TreeMap<Class<? extends Enum<?>>, TreeSet<EnumSupplier>>> enumByClass = new TreeMap<>();
		private @Nullable ImmutableMap<String, ImmutableMap<String, String>> chooseTreeCache;
		private static class EnumSupplier implements IEnumSupplierOperator, ChooseScreen.OptionCallback<Consumer<IEnumSupplierOperator>> {
			private final Enum<?> enumValue;
			private final Class<? extends Enum<?>> enumClass;
			private final String idString;
			EnumSupplier(Enum<?> enumValue, Class<? extends Enum<?>> enumClass){
				this.enumValue = enumValue;
				this.enumClass = enumClass;
				idString = enumValue.getClass().getName() + "." + enumValue.name();
			}
			@Override public void action(ButtonBase button, int mouseButton, Consumer<IEnumSupplierOperator> userData) {userData.accept(this);}
			@Override public Class<? extends Enum<?>> getEnumClass() {return enumClass;}
			@Override public Enum<?> getEnum() {return enumValue;}
			@Override public String idString() {return idString;}
		}
		private @NotNull ImmutableMap<String, ImmutableMap<String, String>> getChooseTree(){
			if(chooseTreeCache != null) return chooseTreeCache;
			ImmutableMap.Builder<String, ImmutableMap<String, String>> builder = ImmutableMap.builder();
			enumByClass.forEach((enumClassName, enumMap)->{
				// 如果只有一个枚举类使用此名称，显示此枚举类的名称，否则显示枚举类全路径
				if(enumMap.size() == 1){
					ImmutableMap.Builder<String, String> enumBuilder = ImmutableMap.builder();
					enumMap.firstEntry().getValue().forEach(supplier->enumBuilder.put(getDisplayString(supplier), supplier.idString()));
					builder.put(enumClassName, enumBuilder.build());
				} else {
					enumMap.forEach((enumClass, supplierSet)->{
						ImmutableMap.Builder<String, String> enumBuilder = ImmutableMap.builder();
						supplierSet.forEach(supplier->enumBuilder.put(getDisplayString(supplier), supplier.idString()));
						builder.put(enumClass.getName(), enumBuilder.build());
					});
				}
			});
			return chooseTreeCache = builder.build();
		}
		@Override public void mouseButtonClicked(IEnumSupplierOperator curr, boolean isLeftButton, Consumer<IEnumSupplierOperator> callback) {
			ChooseScreen.openChooseScreen("", true, true, enumByName, getChooseTree(), callback);
		}
		@Override public @Nullable ConstantEnum.IEnumSupplierOperator get(String idString) {
			if(enumByName.get(idString) instanceof IEnumSupplierOperator supplier) return supplier;
			else return DataUtils.findMostSimilar(enumByName, idString);
		}
		@Override public String getDisplayString(IEnumSupplierOperator sign) {
			return sign.getEnumClass().getSimpleName() + "." + sign.getEnum().name();
		}
		@Override public IEnumSupplierOperator getDefault() {
			return enumByClass.firstEntry().getValue().firstEntry().getValue().first();
		}
		
		public void registerEnum(Class<? extends Enum<?>> enumClass){
			Function<String, TreeMap<Class<? extends Enum<?>>, TreeSet<EnumSupplier>>> classMapFactory =
				k-> new TreeMap<>(Comparator.<Class<? extends Enum<?>>, String>comparing(Class::getName));
			Function<Class<? extends Enum<?>>, TreeSet<EnumSupplier>> enumSetFactory =
				k-> new TreeSet<>(Comparator.comparing(enumSupplier -> enumSupplier.enumValue.ordinal()));
			for(Enum<?> enumConstant : enumClass.getEnumConstants()){
				EnumSupplier supplier = new EnumSupplier(enumConstant, enumClass);
				enumByName.put(supplier.idString(), supplier);
				enumByClass
					.computeIfAbsent(enumClass.getSimpleName(), classMapFactory)
					.computeIfAbsent(enumClass, enumSetFactory)
					.add(supplier);
			}
			chooseTreeCache = null;
		}
	}
	
	public static EnumInfo enumInfo = new EnumInfo();
}
