package lpctools.util.data;

import lpctools.util.AlgorithmUtils;
import org.jspecify.annotations.NonNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

public class WeakReferencedCollection<T> implements Iterable<T> {
	private final ArrayList<WeakReference<T>> list = new ArrayList<>();
	private int lastChecked = 0;
	@Override public @NonNull Iterator<T> iterator() {
		return AlgorithmUtils.iteratorWeakReferences(list);
	}
	public void add(T value) {
		checkOnce();
		checkOnce();
		list.add(new WeakReference<>(value));
	}
	public Iterable<WeakReference<T>> raw() { return list; }
	public void clear() { list.clear(); }
	private void checkOnce() {
		if(list.isEmpty()) return;
		if(++lastChecked >= list.size()) lastChecked = 0;
		if(list.get(lastChecked).get() == null) {
			list.set(lastChecked, list.getLast());
			list.removeLast();
		}
	}
}
