package lpctools.util.data;

import it.unimi.dsi.fastutil.objects.ObjectDoubleImmutablePair;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.ToDoubleFunction;

public interface OrderedContainer<T> {
	void add(T item);
	@Nullable T poll();
	@Nullable T peek();
	int size();
	default boolean isEmpty() { return size() == 0; }

	static <T> OrderedContainer<T> warpQueue(Queue<T> queue) {
		return new OrderedContainer<>() {
			@Override public void add(T item) { queue.add(item); }
			@Override public @Nullable T poll() { return queue.poll(); }
			@Override public @Nullable T peek() { return queue.peek(); }
			@Override public int size() { return queue.size(); }
			@Override public boolean isEmpty() { return queue.isEmpty(); }
		};
	}

	static <T> OrderedContainer<T> warpList(List<T> list) {
		return new OrderedContainer<>() {
			@Override public void add(T item) { list.addLast(item); }
			@Override public @Nullable T poll() { return list.removeLast(); }
			@Override public @Nullable T peek() { return list.getLast(); }
			@Override public int size() { return list.size(); }
			@Override public boolean isEmpty() { return list.isEmpty(); }
		};
	}

	static <T> OrderedContainer<T> stack() { return warpList(new ArrayList<>()); }
	static <T> OrderedContainer<T> queue() { return warpQueue(new ArrayDeque<>()); }

	interface DoubleCachedOrderedContainer<T> extends OrderedContainer<T> {
		@Nullable ObjectDoubleImmutablePair<T> pollPair();
		@Nullable ObjectDoubleImmutablePair<T> peekPair();
		@Override default @Nullable T poll() { return pollPair() instanceof ObjectDoubleImmutablePair<T> pair ? pair.left() : null; }
		@Override default @Nullable T peek() { return peekPair() instanceof ObjectDoubleImmutablePair<T> pair ? pair.left() : null; }
	}

	static <T> DoubleCachedOrderedContainer<T> doubleCachedPriorityQueue(ToDoubleFunction<T> func) {
		PriorityQueue<ObjectDoubleImmutablePair<T>> priorityQueue = new PriorityQueue<>
			(Comparator.comparingDouble(ObjectDoubleImmutablePair::rightDouble));
		return new DoubleCachedOrderedContainer<>() {
			@Override public void add(T item) { priorityQueue.add(ObjectDoubleImmutablePair.of(item, func.applyAsDouble(item))); }
			@Override public @Nullable ObjectDoubleImmutablePair<T> pollPair() { return priorityQueue.poll(); }
			@Override public @Nullable ObjectDoubleImmutablePair<T> peekPair() { return priorityQueue.peek(); }
			@Override public int size() { return priorityQueue.size(); }
			@Override public boolean isEmpty() { return priorityQueue.isEmpty(); }
		};
	}
}
