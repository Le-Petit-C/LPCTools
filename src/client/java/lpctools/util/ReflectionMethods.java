package lpctools.util;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;
import java.io.File;
import java.net.URI;
import java.net.URL;

@SuppressWarnings("unused")
public class ReflectionMethods {
	/**
	 * 获取指定泛型类的具体类型参数
	 */
	public static @Nullable Type getTypeParameter(@Nullable Type type, Class<?> targetGeneric, int argIndex){
		return getTypeParameter(type, targetGeneric, argIndex, new HashSet<>());
	}
	private static @Nullable Type getTypeParameter(@Nullable Type type, Class<?> targetGeneric, int argIndex, HashSet<Type> searchedTypes){
		if(type == null) return null;
		if(searchedTypes.contains(type)) return null;
		searchedTypes.add(type);
		if(type instanceof Class<?> clazz){
			for(Type type1 : clazz.getGenericInterfaces())
				if(getTypeParameter(type1, targetGeneric, argIndex, searchedTypes) instanceof Type res) return res;
			for(Class<?> type1 : clazz.getInterfaces())
				if(getTypeParameter(type1, targetGeneric, argIndex, searchedTypes) instanceof Type res) return res;
			if(getTypeParameter(clazz.getGenericSuperclass(), targetGeneric, argIndex, searchedTypes) instanceof Type res) return res;
			if(getTypeParameter(clazz.getSuperclass(), targetGeneric, argIndex, searchedTypes) instanceof Type res) return res;
		}
		if(type instanceof ParameterizedType type1){
			//用户自己控制index避免IndexOutOfBoundsException
			if(type1.getRawType().equals(targetGeneric)) return type1.getActualTypeArguments()[argIndex];
			return getTypeParameter(type1.getRawType(), targetGeneric, argIndex, searchedTypes);
		}
		return null;
	}
	
	/**
	 * 获取指定包下的所有类
	 * @param packageName 包名（例如：com.example.app）
	 * @return 所有类的Class对象集合
	 */
	public static Set<Class<?>> getClasses(String packageName) {
		String path = packageName.replace('.', '/');
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		
		try {
			Enumeration<URL> resources = classLoader.getResources(path);
			List<File> dirs = new ArrayList<>();
			
			while (resources.hasMoreElements()) {
				URL resource = resources.nextElement();
				URI uri = resource.toURI();
				dirs.add(new File(uri));
			}
			
			return dirs.stream()
				.flatMap(dir -> findClasses(dir, packageName).stream())
				.collect(Collectors.toSet());
			
		} catch (Exception e) {
			throw new RuntimeException("无法扫描包: " + packageName, e);
		}
	}
	
	/**
	 * 递归查找目录中的所有类
	 */
	private static List<Class<?>> findClasses(File directory, String packageName) {
		if (!directory.exists()) {
			return Collections.emptyList();
		}
		
		List<Class<?>> classes = new ArrayList<>();
		File[] files = directory.listFiles();
		
		if (files == null) return classes;
		
		for (File file : files) {
			if (file.isDirectory()) {
				// 递归扫描子目录
				classes.addAll(findClasses(
					file,
					packageName + "." + file.getName()
				));
			} else if (file.getName().endsWith(".class")) {
				// 加载类文件
				String className = packageName + '.' +
					file.getName().substring(0, file.getName().length() - 6);
				
				try {
					Class<?> clazz = Class.forName(className);
					classes.add(clazz);
				} catch (ClassNotFoundException e) {
					// 跳过无法加载的类
				}
			}
		}
		return classes;
	}
	
	/**
	 * 获取包下所有接口
	 */
	public static Set<Class<?>> getInterfaces(String packageName) {
		return getClasses(packageName).stream()
			.filter(Class::isInterface)
			.collect(Collectors.toSet());
	}
	
	/**
	 * 获取包下所有具体类（非抽象类）
	 */
	public static Set<Class<?>> getConcreteClasses(String packageName) {
		return getClasses(packageName).stream()
			.filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))
			.filter(clazz -> !clazz.isInterface())
			.collect(Collectors.toSet());
	}
	
	/**
	 * 获取带特定注解的类
	 */
	public static Set<Class<?>> getClassesWithAnnotation(
		String packageName,
		Class<? extends java.lang.annotation.Annotation> annotationClass
	) {
		return getClasses(packageName).stream()
			.filter(clazz -> clazz.isAnnotationPresent(annotationClass))
			.collect(Collectors.toSet());
	}
}