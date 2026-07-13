package lpctools.script.suppliers.Iterable;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;

public interface ObjectIterable extends Iterable<Object>{
	static ObjectIterable of(Iterable<?> iterable){
		return new ObjectIterable() {
			@Override public @NotNull Iterator<Object> iterator() {
				//noinspection unchecked
				return (Iterator<Object>) iterable.iterator();
			}
		};
	}
	static <T> ObjectIterable of(T[] array){
		var list = Arrays.asList(array);
		return new ObjectIterable() {
			@Override public @NotNull Iterator<Object> iterator() {
				//noinspection unchecked
				return (Iterator<Object>) list.iterator();
			}
		};
	}
	ObjectIterable empty = new ObjectIterable() {
		@Override public @NotNull Iterator<Object> iterator() {
			return new Iterator<>() {
				@Override public boolean hasNext() {return false;}
				@Override public Object next() {return null;}
			};
		}
	};
}
