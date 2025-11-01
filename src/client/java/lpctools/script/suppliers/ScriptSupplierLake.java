package lpctools.script.suppliers;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lpctools.lpcfymasaapi.screen.ChooseScreen;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.suppliers.booleans.And;
import lpctools.script.suppliers.booleans.Equals;
import lpctools.script.suppliers.booleans.Or;
import lpctools.script.suppliers.entities.VehicleEntity;
import lpctools.script.suppliers.entities.playerEntities.MainPlayerEntity;
import lpctools.script.suppliers.randoms.FromVariable;
import lpctools.script.suppliers.randoms.IRandomSupplierAllocator;
import lpctools.script.suppliers.randoms.Null;
import lpctools.script.suppliers.types.ConstantType;
import lpctools.script.suppliers.types.ObjectType;
import lpctools.script.suppliers.voids.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static lpctools.lpcfymasaapi.LPCConfigUtils.warnFailedLoadingConfig;

public class ScriptSupplierLake {
	public static final ImmutableMap<String, ScriptRegistration<?, ?>> suppliers;
	public static final ImmutableMap<Class<? extends IScriptSupplier<?>>, ScriptRegistration<?, ?>> suppliersInverseMap;
	public static final ImmutableMap<Class<?>, ImmutableSet<ScriptRegistration<?, ?>>> suppliersFromClass;
	public static final ImmutableMap<Class<?>, ImmutableSet<Class<?>>> preciseSupplierTypeTree;
	public static final ImmutableMap<Class<?>, ScriptType> typeMap;
	public static final ImmutableMap<String, ScriptType> typeIdMap;
	
	private static final LinkedHashMap<String, ScriptRegistration<?, ?>> suppliersTemp = new LinkedHashMap<>();
	private static final LinkedHashMap<Class<? extends IScriptSupplier<?>>, ScriptRegistration<?, ?>> suppliersInverseMapTemp = new LinkedHashMap<>();
	private static final LinkedHashMap<Class<?>, LinkedHashSet<ScriptRegistration<?, ?>>> suppliersFromClassTemp = new LinkedHashMap<>();
	private static final LinkedHashMap<Class<?>, LinkedHashSet<Class<?>>> preciseSupplierTypeTreeTemp = new LinkedHashMap<>();
	private static final LinkedHashMap<Class<?>, ScriptType> typeMapTemp = new LinkedHashMap<>();
	private static final LinkedHashMap<String, ScriptType> typeIdMapTemp = new LinkedHashMap<>();
	
	public static <T> void chooseSupplier(Class<T> targetClass, IScriptWithSubScript parent, Consumer<IScriptSupplier<? extends T>> callback){
		Consumer<ScriptRegistration<?, ?>> consumer = reg->{
			var allocator = reg.tryAllocate(targetClass);
			if(allocator != null) callback.accept(allocator.allocate(parent));
		};
		ChooseScreen.openChooseScreen(getTypeName(targetClass).getString(), true, suppliers, buildChooseMap(targetClass, true), consumer);
	}
	
	public static String getSupplierId(IScriptSupplier<?> supplier){
		return Objects.requireNonNull(suppliersInverseMap.get(supplier.getClass())).key;
	}
	
	public static ScriptRegistration<?, ?> getSupplierRegistration(IScriptSupplier<?> supplier){return suppliersInverseMap.get(supplier.getClass());}
	public static ScriptRegistration<?, ?> getSupplierRegistration(String key){return suppliers.get(key);}
	
	@SuppressWarnings("UnusedReturnValue")
	public static <T, U extends IScriptSupplier<T>> boolean registerPrecise(String key, Text displayName, Class<T> clazz, Class<U> supplierClass, IScriptSupplierAllocator<U> allocator){
		return register(key, clazz, ScriptRegistration.ofPrecise(key, displayName, clazz, supplierClass, allocator));
	}
	
	@SuppressWarnings("UnusedReturnValue")
	public static <U extends IScriptSupplier<?>> boolean registerRandom(String key, Text displayName, Class<U> supplierClass, IRandomSupplierAllocator allocator){
		return register(key, Object.class, ScriptRegistration.ofRandom(key, displayName, supplierClass, allocator));
	}
	
	public static Text getTypeName(Class<?> clazz){
		if(typeMap.containsKey(clazz)) return Objects.requireNonNull(typeMap.get(clazz)).name();
		else return Text.literal(clazz.getName());
	}
	
	public static JsonObject getJsonEntryFromSupplier(@NotNull IScriptSupplier<?> supplier){
		JsonObject res = new JsonObject();
		res.addProperty("id", getSupplierId(supplier));
		res.add("data", supplier.getAsJsonElement());
		return res;
	}
	
	public static @Nullable <T> IScriptSupplier<? extends T> loadSupplierFromJsonEntry(JsonElement element, Class<T> targetClass, IScriptWithSubScript parent){
		if(!(element instanceof JsonObject object)) return null;
		if(object.get("id") instanceof JsonPrimitive id){
			var reg = ScriptSupplierLake.getSupplierRegistration(id.getAsString());
			var allocator = reg.tryAllocate(targetClass);
			if(allocator != null){
				var res = allocator.allocate(parent);
				res.setValueFromJsonElement(object.get("data"));
				return res;
			}
		}
		return null;
	}
	
	public static <T> void loadSupplierOrWarn(JsonElement element, Class<T> targetClass, IScriptWithSubScript parent, Consumer<IScriptSupplier<? extends T>> setter, String warnKey){
		if(element == null) return;
		var res = ScriptSupplierLake.loadSupplierFromJsonEntry(element, targetClass, parent);
		if(res != null) setter.accept(res);
		else warnFailedLoadingConfig(warnKey, element);
	}
	
	private static LinkedHashMap<String, Object> buildChooseMap(Class<?> clazz, boolean withRandomExtra){
		LinkedHashMap<String, Object> res;
		var targetSet = suppliersFromClass.get(clazz);
		var supTypeSet = preciseSupplierTypeTree.get(clazz);
		//当clazz没有任何子类供应器时，直接返回Object的选择列表，也就是只选择random suppliers
		//clazz为Object.class时，targetSet和supTypeSet不应该为空，不会触发无限递归。但为了保险起见，还是加上判断
		if((targetSet == null || targetSet.isEmpty()) && (supTypeSet == null || supTypeSet.isEmpty()) && clazz != Object.class)
			res = buildChooseMap(Object.class, false);
		else{
			res = new LinkedHashMap<>();
			if(withRandomExtra && clazz != Void.class && clazz != Object.class)
				res.put(getTypeName(Object.class).getString(), (Supplier<?>)()->buildChooseMap(Object.class, false));
			if(targetSet != null)
				for(var target : targetSet)
					res.put(target.displayName.getString(), target.key);
			if(clazz == Object.class && !withRandomExtra){
				//由于Object是所有类型的基类，不可能由其他类继承而来，那么来到这里就只能是选择random suppliers了
				//那么就不再选择Object的子类，直接返回
				return res;
			}
			if(supTypeSet != null){
				for(var target : supTypeSet){
					if(target == Void.class) continue;
					res.put(getTypeName(target).getString(), (Supplier<?>)()->buildChooseMap(target, false));
				}
			}
		}
		return res;
	}
	
	static {
		preciseSupplierTypeTreeTemp.put(Object.class, new LinkedHashSet<>());
	}
	
	//注册类型
	static{
		registerType(Object.class, 			Text.translatable("lpctools.script.typeName.Object"), "object");
		registerType(Void.class, 			Text.translatable("lpctools.script.typeName.Void"), "void");
		registerType(Entity.class, 			Text.translatable("lpctools.script.typeName.Entity"), "entity");
		registerType(PlayerEntity.class, 	Text.translatable("lpctools.script.typeName.PlayerEntity"), "playerEntity");
		registerType(Boolean.class, 		Text.translatable("lpctools.script.typeName.Boolean"), "boolean");
		registerType(ScriptType.class, 		Text.translatable("lpctools.script.typeName.ScriptType"), "type");
	}
	
	//注册suppliers
	static {
		//注册random suppliers，也就是无类型限制的Object类suppliers
		registerRandom("null", 				Text.translatable("lpctools.script.suppliers.randoms.null.name"), Null.class, Null::new);
		registerRandom("fromVariable", 		Text.translatable("lpctools.script.suppliers.randoms.fromVariable.name"), FromVariable.class, FromVariable::new);
		//注册void suppliers，也就是无返回值的基础操作
		registerPrecise("doNothing", 		Text.translatable("lpctools.script.suppliers.voids.doNothing.name"), Void.class, DoNothing.class, DoNothing::new);
		registerPrecise("runMultiple", 		Text.translatable("lpctools.script.suppliers.voids.runMultiple.name"), Void.class, RunMultiple.class, RunMultiple::new);
		registerPrecise("runIfElse", 		Text.translatable("lpctools.script.suppliers.voids.runIfElse.name"), Void.class, RunIfElse.class, RunIfElse::new);
		registerPrecise("doAttack", 		Text.translatable("lpctools.script.suppliers.voids.doAttack.name"), Void.class, DoAttack.class, DoAttack::new);
		registerPrecise("doItemUse", 		Text.translatable("lpctools.script.suppliers.voids.doItemUse.name"), Void.class, DoItemUse.class, DoItemUse::new);
		registerPrecise("setVariable", 		Text.translatable("lpctools.script.suppliers.voids.setVariable.name"), Void.class, SetVariable.class, SetVariable::new);
		//注册entity suppliers
		registerPrecise("vehicleEntity", 	Text.translatable("lpctools.script.suppliers.entities.vehicleEntity.name"), Entity.class, VehicleEntity.class, VehicleEntity::new);
		//注册player entity suppliers
		registerPrecise("playerEntity", 	Text.translatable("lpctools.script.suppliers.playerEntities.mainPlayerEntity.name"), PlayerEntity.class, MainPlayerEntity.class, MainPlayerEntity::new);
		//注册boolean suppliers
		registerPrecise("notNull", 			Text.translatable("lpctools.script.suppliers.booleans.notNull.name"), Boolean.class, lpctools.script.suppliers.booleans.NotNull.class, lpctools.script.suppliers.booleans.NotNull::new);
		registerPrecise("and", 				Text.translatable("lpctools.script.suppliers.booleans.and.name"), Boolean.class, And.class, And::new);
		registerPrecise("or", 				Text.translatable("lpctools.script.suppliers.booleans.or.name"), Boolean.class, Or.class, Or::new);
		registerPrecise("equals", 			Text.translatable("lpctools.script.suppliers.booleans.equals.name"), Boolean.class, Equals.class, Equals::new);
		//注册type suppliers
		registerPrecise("objectType", 		Text.translatable("lpctools.script.suppliers.types.objectType.name"), ScriptType.class, ObjectType.class, ObjectType::new);
		registerPrecise("constantType", 	Text.translatable("lpctools.script.suppliers.types.constantType.name"), ScriptType.class, ConstantType.class, ConstantType::new);
	}
	
	//固定temp数据到不可变映射
	static {
		//suppliers
		suppliers = ImmutableMap.copyOf(suppliersTemp);
		suppliersTemp.clear();
		//suppliersInverseMap
		suppliersInverseMap = ImmutableMap.copyOf(suppliersInverseMapTemp);
		suppliersInverseMapTemp.clear();
		//suppliersFromClass
		LinkedHashMap<Class<?>, ImmutableSet<ScriptRegistration<?, ?>>> _suppliersFromClassTemp = new LinkedHashMap<>();
		for(var entry : suppliersFromClassTemp.entrySet())
			_suppliersFromClassTemp.put(entry.getKey(), ImmutableSet.copyOf(entry.getValue()));
		suppliersFromClass = ImmutableMap.copyOf(_suppliersFromClassTemp);
		suppliersFromClassTemp.clear();
		//preciseSupplierTypeTree
		LinkedHashMap<Class<?>, ImmutableSet<Class<?>>> _preciseSupplierTypeTreeTemp = new LinkedHashMap<>();
		for(var entry : preciseSupplierTypeTreeTemp.entrySet())
			_preciseSupplierTypeTreeTemp.put(entry.getKey(), ImmutableSet.copyOf(entry.getValue()));
		preciseSupplierTypeTree = ImmutableMap.copyOf(_preciseSupplierTypeTreeTemp);
		preciseSupplierTypeTreeTemp.clear();
		//typeMap
		typeMap = ImmutableMap.copyOf(typeMapTemp);
		typeMapTemp.clear();
		//typeIdMap
		typeIdMap = ImmutableMap.copyOf(typeIdMapTemp);
		typeIdMapTemp.clear();
	}
	
	private static boolean putType(Class<?> currentClass, Class<?> targetClass, LinkedHashSet<Class<?>> targetSet, HashSet<Class<?>> visitedClasses){
		if(!currentClass.isAssignableFrom(targetClass)) return false;
		if(visitedClasses.contains(currentClass)) return true;
		visitedClasses.add(currentClass);
		var currSet = preciseSupplierTypeTreeTemp.get(currentClass);
		boolean isDirect = true;
		ArrayList<Class<?>> removed = new ArrayList<>();
		for(var clazz : currSet){
			if(putType(clazz, targetClass, targetSet, visitedClasses))
				isDirect = false;
			else if(targetClass.isAssignableFrom(clazz)){
				targetSet.add(clazz);
				removed.add(clazz);
			}
		}
		removed.forEach(currSet::remove);
		if(isDirect) currSet.add(targetClass);
		return true;
	}
	
	private static <T, U extends IScriptSupplier<? extends T>> boolean register(String key, Class<? super T> clazz, ScriptRegistration<T, U> registration){
		if(!typeMapTemp.containsKey(clazz)){
			Class<? super T> superClass = Object.class;
			for(var type : typeMapTemp.keySet()){
				if(type.isAssignableFrom(clazz) && superClass.isAssignableFrom(type))
					//noinspection unchecked
					superClass = (Class<? super T>)type;
			}
			clazz = superClass;
		}
		if(suppliersTemp.containsKey(key)) return false;
		suppliersTemp.put(key, registration);
		suppliersInverseMapTemp.put(registration.supplierClass, registration);
		suppliersFromClassTemp.computeIfAbsent(clazz, v->new LinkedHashSet<>()).add(registration);
		return true;
	}
	
	private static void registerType(Class<?> basicClass, Text name, String id){
		var type = new ScriptType.BasicType(basicClass, name, id);
		typeMapTemp.put(basicClass, type);
		typeIdMapTemp.put(id, type);
		LinkedHashSet<Class<?>> set = new LinkedHashSet<>();
		putType(Object.class, basicClass, set, new HashSet<>());
		preciseSupplierTypeTreeTemp.put(basicClass, set);
	}
}
