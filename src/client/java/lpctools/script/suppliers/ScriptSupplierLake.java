package lpctools.script.suppliers;

import lpctools.lpcfymasaapi.screen.ChooseScreen;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.suppliers.randoms.IRandomSupplierAllocator;
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
	private static final HashMap<Class<?>, HashSet<Class<?>>> supplierTypeTree = new HashMap<>();
	private static final HashMap<Class<?>, Text> typeNameKeys = new HashMap<>();
	
	static {
		supplierTypeTree.put(Object.class, new HashSet<>());
	}
	
	//初始化typeNameKeys
	static{
		typeNameKeys.put(Object.class, Text.translatable("lpctools.script.typeName.Object"));
		typeNameKeys.put(Void.class, Text.translatable("lpctools.script.typeName.Void"));
	}
	
	//注册suppliers
	static {
		registerPrecise("doNothing", Void.class, DoNothing.class, DoNothing::new);
		registerPrecise("runMultiple", Void.class, RunMultiple.class, RunMultiple::new);
		registerPrecise("doAttack", Void.class, DoAttack.class, DoAttack::new);
		registerPrecise("doItemUse", Void.class, DoItemUse.class, DoItemUse::new);
	}
	
	public static <T> void chooseSupplier(Class<T> targetClass, IScriptWithSubScript parent, Consumer<IScriptSupplier<? extends T>> callback){
		Consumer<ScriptRegistration<?, ?>> consumer = reg->{
			var allocator = reg.tryAllocate(targetClass);
			if(allocator != null) callback.accept(allocator.allocate(parent));
		};
		ChooseScreen.openChooseScreen(getTypeName(targetClass), true, suppliers, buildChooseMap(targetClass), consumer);
	}
	
	public static String getSupplierId(IScriptSupplier<?> supplier){
		return suppliersInverseMap.get(supplier.getClass()).key;
	}
	
	public static ScriptRegistration<?, ?> getSupplierRegistration(String key){return suppliers.get(key);}
	
	@SuppressWarnings("UnusedReturnValue")
	public static <T, U extends IScriptSupplier<T>> boolean registerPrecise(String key, Class<T> clazz, Class<U> supplierClass, IScriptSupplierAllocator<U> allocator){
		return register(key, clazz, ScriptRegistration.ofPrecise(key, clazz, supplierClass, allocator));
	}
	
	@SuppressWarnings("UnusedReturnValue")
	public static <U extends IScriptSupplier<Object>> boolean registerRandom(String key, Class<U> supplierClass, IRandomSupplierAllocator allocator){
		return register(key, Object.class, ScriptRegistration.ofRandom(key, supplierClass, allocator));
	}
	
	public static String getTypeName(Class<?> clazz){
		if(typeNameKeys.containsKey(clazz)) return typeNameKeys.get(clazz).getString();
		else return clazz.getName();
	}
	
	
	private static HashMap<String, ?> buildChooseMap(Class<?> clazz){
		HashMap<String, Object> res = new HashMap<>();
		for(var target : suppliersFromClass.get(clazz))
			res.put(target.key, target.key);
		for(var target : supplierTypeTree.get(clazz))
			res.put(getTypeName(clazz), (Supplier<?>)()->buildChooseMap(target));
		return res;
	}
	
	private static boolean putType(Class<?> currentClass, Class<?> targetClass, HashSet<Class<?>> targetSet, HashSet<Class<?>> visitedClasses){
		if(!currentClass.isAssignableFrom(targetClass)) return false;
		if(visitedClasses.contains(currentClass)) return true;
		visitedClasses.add(currentClass);
		var currSet = supplierTypeTree.get(currentClass);
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
	
	private static <T, U extends IScriptSupplier<T>> boolean register(String key, Class<T> clazz, ScriptRegistration<T, U> registration){
		if(suppliers.containsKey(key)) return false;
		suppliers.put(key, registration);
		suppliersInverseMap.put(registration.supplierClass, registration);
		suppliersFromClass.computeIfAbsent(clazz, v->new HashSet<>()).add(registration);
		if(!supplierTypeTree.containsKey(clazz)){
			HashSet<Class<?>> targetSet = new HashSet<>();
			supplierTypeTree.put(clazz, targetSet);
			putType(Object.class, clazz, targetSet, new HashSet<>());
		}
		return true;
	}
}
