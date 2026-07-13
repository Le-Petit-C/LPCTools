package lpctools.util.operatorUtils;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.function.Consumer;

public class ArrayListSignInfo<T extends Operators.SignBase> implements Operators.ISignInfo<T> {
	private final ArrayList<T> signs;
	
	public ArrayListSignInfo() {
		this.signs = new ArrayList<>();
	}
	
	public ArrayListSignInfo(ArrayList<T> signs) {
		this.signs = signs;
	}
	
	public void addSign(T sign) {
		signs.add(sign);
	}
	
	public T cycleSign(T curr, boolean forward) {
		var i = signs.indexOf(curr);
		if (i == -1) return signs.getFirst();
		if (forward) {
			int j = i + 1;
			if (j >= signs.size()) return signs.getFirst();
			else return signs.get(j);
		} else {
			int j = i - 1;
			if (j < 0) return signs.getLast();
			else return signs.get(j);
		}
	}
	
	@Override public void mouseButtonClicked(T curr, boolean isLeftButton, Consumer<T> callback) {
		callback.accept(cycleSign(curr, isLeftButton));
	}
	
	@Override public @Nullable T get(String idString) {
		for (var sign : signs) {
			if (sign.idString().equals(idString)) return sign;
		}
		return null;
	}
	
	@Override public T getDefault() {
		return signs.getFirst();
	}
}
