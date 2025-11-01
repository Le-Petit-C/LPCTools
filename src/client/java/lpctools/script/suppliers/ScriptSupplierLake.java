package lpctools.script.suppliers;

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
import lpctools.script.suppliers.randoms.IRandomSupplierAllocator;
import lpctools.script.suppliers.randoms.Null;
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
	private static final HashMap<String, ScriptRegistration<?, ?>> suppliers = new HashMap<>();
	private static final HashMap<Class<? extends IScriptSupplier<?>>, ScriptRegistration<?, ?>> suppliersInverseMap = new HashMap<>();
	private static final HashMap<Class<?>, HashSet<ScriptRegistration<?, ?>>> suppliersFromClass = new HashMap<>();
	private static final HashMap<Class<?>, HashSet<Class<?>>> preciseSupplierTypeTree = new HashMap<>();
	private static final HashMap<Class<?>, Text> typeNameKeys = new HashMap<>();
	
	static {
		preciseSupplierTypeTree.put(Object.class, new HashSet<>());
	}
	
	//初始化typeNameKeys
	static{
		typeNameKeys.put(Object.class, 			Text.translatable("lpctools.script.typeName.Object"));
		typeNameKeys.put(Void.class, 			Text.translatable("lpctools.script.typeName.Void"));
		typeNameKeys.put(Entity.class, 			Text.translatable("lpctools.script.typeName.Entity"));
		typeNameKeys.put(PlayerEntity.class, 	Text.translatable("lpctools.script.typeName.PlayerEntity"));
		typeNameKeys.put(Boolean.class, 		Text.translatable("lpctools.script.typeName.Boolean"));
	}
	
	//注册suppliers
	static {
		//注册random suppliers，也就是无类型限制的Object类suppliers
		registerRandom("null", 				Text.translatable("lpctools.script.suppliers.randoms.null.name"), Null.class, Null::new);
		//注册void suppliers，也就是无返回值的基础操作
		registerPrecise("doNothing", 		Text.translatable("lpctools.script.suppliers.voids.doNothing.name"), Void.class, DoNothing.class, DoNothing::new);
		registerPrecise("runMultiple", 		Text.translatable("lpctools.script.suppliers.voids.runMultiple.name"), Void.class, RunMultiple.class, RunMultiple::new);
		registerPrecise("runIfElse", 		Text.translatable("lpctools.script.suppliers.voids.runIfElse.name"), Void.class, RunIfElse.class, RunIfElse::new);
		registerPrecise("doAttack", 		Text.translatable("lpctools.script.suppliers.voids.doAttack.name"), Void.class, DoAttack.class, DoAttack::new);
		registerPrecise("doItemUse", 		Text.translatable("lpctools.script.suppliers.voids.doItemUse.name"), Void.class, DoItemUse.class, DoItemUse::new);
		//注册entity suppliers
		registerPrecise("vehicleEntity", 	Text.translatable("lpctools.script.suppliers.entities.vehicleEntity.name"), Entity.class, VehicleEntity.class, VehicleEntity::new);
		//注册player entity suppliers
		registerPrecise("playerEntity", 	Text.translatable("lpctools.script.suppliers.playerEntities.mainPlayerEntity.name"), PlayerEntity.class, MainPlayerEntity.class, MainPlayerEntity::new);
		//注册boolean suppliers
		registerPrecise("notNull", 			Text.translatable("lpctools.script.suppliers.booleans.notNull.name"), Boolean.class, lpctools.script.suppliers.booleans.NotNull.class, lpctools.script.suppliers.booleans.NotNull::new);
		registerPrecise("and", 				Text.translatable("lpctools.script.suppliers.booleans.and.name"), Boolean.class, And.class, And::new);
		registerPrecise("or", 				Text.translatable("lpctools.script.suppliers.booleans.or.name"), Boolean.class, Or.class, Or::new);
		registerPrecise("equals", 			Text.translatable("lpctools.script.suppliers.booleans.equals.name"), Boolean.class, Equals.class, Equals::new);
	}
	
	public static <T> void chooseSupplier(Class<T> targetClass, IScriptWithSubScript parent, Consumer<IScriptSupplier<? extends T>> callback){
		Consumer<ScriptRegistration<?, ?>> consumer = reg->{
			var allocator = reg.tryAllocate(targetClass);
			if(allocator != null) callback.accept(allocator.allocate(parent));
		};
		ChooseScreen.openChooseScreen(getTypeName(targetClass), true, suppliers, buildChooseMap(targetClass, true), consumer);
	}
	
	public static String getSupplierId(IScriptSupplier<?> supplier){
		return suppliersInverseMap.get(supplier.getClass()).key;
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
	
	public static String getTypeName(Class<?> clazz){
		if(typeNameKeys.containsKey(clazz)) return typeNameKeys.get(clazz).getString();
		else return clazz.getName();
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
	
	
	private static HashMap<String, ?> buildChooseMap(Class<?> clazz, boolean withRandomExtra){
		LinkedHashMap<String, Object> res = new LinkedHashMap<>();
		if(withRandomExtra && clazz != Void.class && clazz != Object.class)
			res.put(getTypeName(Object.class), (Supplier<?>)()->buildChooseMap(Object.class, false));
		for(var target : suppliersFromClass.get(clazz))
			res.put(target.displayName.getString(), target.key);
		if(clazz == Object.class && !withRandomExtra){
			//由于Object是所有类型的基类，不可能由其他类继承而来，那么来到这里就只能是选择random suppliers了
			//那么就不再选择Object的子类，直接返回
			return res;
		}
		for(var target : preciseSupplierTypeTree.get(clazz)){
			if(target == Void.class) continue;
			res.put(getTypeName(target), (Supplier<?>)()->buildChooseMap(target, false));
		}
		return res;
	}
	
	private static boolean putType(Class<?> currentClass, Class<?> targetClass, HashSet<Class<?>> targetSet, HashSet<Class<?>> visitedClasses){
		if(!currentClass.isAssignableFrom(targetClass)) return false;
		if(visitedClasses.contains(currentClass)) return true;
		visitedClasses.add(currentClass);
		var currSet = preciseSupplierTypeTree.get(currentClass);
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
	
	private static <T, U extends IScriptSupplier<? extends T>> boolean register(String key, Class<T> clazz, ScriptRegistration<T, U> registration){
		if(suppliers.containsKey(key)) return false;
		suppliers.put(key, registration);
		suppliersInverseMap.put(registration.supplierClass, registration);
		suppliersFromClass.computeIfAbsent(clazz, v->new HashSet<>()).add(registration);
		if(!preciseSupplierTypeTree.containsKey(clazz)){
			HashSet<Class<?>> targetSet = new HashSet<>();
			preciseSupplierTypeTree.put(clazz, targetSet);
			putType(Object.class, clazz, targetSet, new HashSet<>());
		}
		return true;
	}
}
