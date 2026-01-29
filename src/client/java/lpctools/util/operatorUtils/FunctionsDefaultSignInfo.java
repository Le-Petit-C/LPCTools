package lpctools.util.operatorUtils;

import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.function.Consumer;

public class FunctionsDefaultSignInfo<T extends Operators.SignBase> implements Operators.ISignInfo<T> {
	private final T[] signs;
	private final ImmutableMap<String, Integer> indexMap;
	
	FunctionsDefaultSignInfo(Class<T> targetType) {
		ArrayList<T> list = new ArrayList<>();
		for (var v : Operators.class.getFields()) {
			if (Modifier.isStatic(v.getModifiers()) && targetType.isAssignableFrom(v.getType())) {
				try {
					//noinspection unchecked
					list.add((T) v.get(null));
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}
		}
		//noinspection unchecked
		this.signs = list.toArray((T[]) Array.newInstance(targetType, list.size()));
		ImmutableMap.Builder<String, Integer> indexMapBuilder = new ImmutableMap.Builder<>();
		for (int i = 0; i < signs.length; ++i) indexMapBuilder.put(signs[i].idString(), i);
		indexMap = indexMapBuilder.build();
	}
	
	public T cycleSign(T curr, boolean forward) {
		var i = indexMap.get(curr.idString());
		if (i == null) return signs[0];
		if (forward) {
			int j = i + 1;
			if (j >= signs.length) return signs[0];
			else return signs[j];
		} else {
			int j = i - 1;
			if (j < 0) return signs[signs.length - 1];
			else return signs[j];
		}
	}
	
	@Override public void mouseButtonClicked(T curr, boolean isLeftButton, Consumer<T> callback) {
		callback.accept(cycleSign(curr, isLeftButton));
	}
	
	@Override public @Nullable T get(String idString) {
		var i = indexMap.get(idString);
		if (i == null) return null;
		else return signs[i];
	}
	
	@Override public T getDefault() {
		return signs[0];
	}
}
