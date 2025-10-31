package lpctools.script.suppliers;

import lpctools.lpcfymasaapi.screen.ChooseScreen;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.suppliers.randoms.IRandomSupplierAllocator;
import lpctools.script.suppliers.randoms.Null;
import lpctools.script.suppliers.voids.DoAttack;
import lpctools.script.suppliers.voids.DoItemUse;
import lpctools.script.suppliers.voids.DoNothing;
import lpctools.script.suppliers.voids.RunMultiple;
import net.minecraft.text.Text;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
		typeNameKeys.put(Object.class, Text.translatable("lpctools.script.typeName.Object"));
		typeNameKeys.put(Void.class, Text.translatable("lpctools.script.typeName.Void"));
	}
	
	//注册suppliers
	static {
		registerRandom("null", 			Text.translatable("lpctools.script.suppliers.randoms.null.name"), 		Null.class, Null::new);
		registerPrecise("doNothing", 	Text.translatable("lpctools.script.suppliers.voids.doNothing.name"), 	Void.class, DoNothing.class, DoNothing::new);
		registerPrecise("runMultiple", 	Text.translatable("lpctools.script.suppliers.voids.runMultiple.name"), 	Void.class, RunMultiple.class, RunMultiple::new);
		registerPrecise("doAttack", 	Text.translatable("lpctools.script.suppliers.voids.doAttack.name"), 	Void.class, DoAttack.class, DoAttack::new);
		registerPrecise("doItemUse", 	Text.translatable("lpctools.script.suppliers.voids.doItemUse.name"), 	Void.class, DoItemUse.class, DoItemUse::new);
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
	
	
	private static HashMap<String, ?> buildChooseMap(Class<?> clazz, boolean withRandomExtra){
		LinkedHashMap<String, Object> res = new LinkedHashMap<>();
		if(withRandomExtra && clazz != Object.class && clazz != Void.class){
			for(var target : suppliersFromClass.get(Object.class))
				res.put(target.displayName.getString(), target.key);
		}
		for(var target : suppliersFromClass.get(clazz))
			res.put(target.displayName.getString(), target.key);
		for(var target : preciseSupplierTypeTree.get(clazz))
			res.put(getTypeName(clazz), (Supplier<?>)()->buildChooseMap(target, false));
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
