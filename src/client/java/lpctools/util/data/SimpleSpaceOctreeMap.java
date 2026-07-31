package lpctools.util.data;

import lpctools.util.MathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.joml.Vector3i;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class SimpleSpaceOctreeMap<T> extends AbstractMap<Vector3i, T> {
	// root可以为空
	private final RootOctreeNode<T> root = new RootOctreeNode<>();
	private EntrySet entrySet;

	public SimpleSpaceOctreeMap() {}

	public @NotNull EntrySet octreeEntrySet() {
		if(entrySet != null) return entrySet;
		else return entrySet = new EntrySet();
	}

	@Deprecated @Override public @NonNull Set<Entry<Vector3i, T>> entrySet() {
		EntrySet set = octreeEntrySet();
		return new AbstractSet<>() {
			@Override public @NonNull Iterator<Entry<Vector3i, T>> iterator() {
				SimpleIterator wrapped = set.iterator();
				return new Iterator<>() {
					@Override public boolean hasNext() { return wrapped.hasNext(); }
					@Override public OctreeEntry<T> next() { return wrapped.next(); }
					@Override public void remove() { wrapped.remove(); }
				};
			}
			@Override public int size() { return set.size(); }
			@Override public void clear() { set.clear(); }
		};
	}

	@Override public int size() { return root.size; }

	public T put(int x, int y, int z, T value) { return getOrCreateEntry(x, y, z).replace(value); }
	@Override public T put(Vector3i key, T value) { return put(key.x, key.y, key.z, value); }

	public T remove(int x, int y, int z) { return root.remove(x, y, z); }
	public T remove(@NotNull Vector3i key) { return remove(key.x, key.y, key.z); }
	public T remove(@NotNull Vec3i key) { return remove(key.getX(), key.getY(), key.getZ()); }
	@Override public T remove(Object key) { return key instanceof Vector3i vec ? remove(vec) : null; }

	public @Nullable OctreeEntry<T> getEntry(int x, int y, int z) { return root.getEntry(x, y, z); }
	public @Nullable OctreeEntry<T> getEntry(@NotNull Vector3i key) { return getEntry(key.x, key.y, key.z); }
	public @Nullable OctreeEntry<T> getEntry(@NotNull Vec3i key) { return getEntry(key.getX(), key.getY(), key.getZ()); }

	public @NotNull OctreeEntry<T> getOrCreateEntry(int x, int y, int z) { return root.getOrCreateEntry(x, y, z, (_, _, _)->null); }
	public @NotNull OctreeEntry<T> getOrCreateEntry(@NotNull Vector3i key) { return getOrCreateEntry(key.x, key.y, key.z); }
	public @NotNull OctreeEntry<T> getOrCreateEntry(@NotNull Vec3i key) { return getOrCreateEntry(key.getX(), key.getY(), key.getZ()); }

	public T get(int x, int y, int z) { return getEntry(x, y, z) instanceof OctreeEntry<T> entry ? entry.getValue() : null; }
	public T get(@NotNull Vector3i key) { return get(key.x, key.y, key.z); }
	public T get(@NotNull Vec3i key) { return get(key.getX(), key.getY(), key.getZ()); }
	@Override public T get(Object key) { return key instanceof Vector3i vec ? get(vec) : null; }

	public boolean containsKey(int x, int y, int z) { return root.containsKey(x, y, z); }
	public boolean containsKey(@NotNull Vector3i key) { return containsKey(key.x, key.y, key.z); }
	public boolean containsKey(@NotNull Vec3i key) { return containsKey(key.getX(), key.getY(), key.getZ()); }
	@Override public boolean containsKey(Object key) { return key instanceof Vector3i vec && containsKey(vec); }

	public T compute(int x, int y, int z, @NonNull SpaceEntryTranslator<? super T, ? extends T> remappingFunction) {
		var entry = getOrCreateEntry(x, y, z);
		return entry.value = remappingFunction.translateSpaceEntry(entry.x, entry.y, entry.z, entry.value);
	}
	public T compute(Vector3i key, @NonNull SpaceEntryTranslator<? super T, ? extends T> remappingFunction) {
		return compute(key.x, key.y, key.z, remappingFunction);
	}
	public T compute(BlockPos key, @NonNull SpaceEntryTranslator<? super T, ? extends T> remappingFunction) {
		return compute(key.getX(), key.getY(), key.getZ(), remappingFunction);
	}
	@Deprecated @Override public T compute(Vector3i key, @NonNull BiFunction<? super Vector3i, ? super T, ? extends T> remappingFunction) {
		return compute(key, (x, y, z, value)->remappingFunction.apply(new Vector3i(x, y, z), value));
	}

	public T computeIfPresent(int x, int y, int z, @NonNull SpaceEntryTranslator<? super T, ? extends T> remappingFunction) {
		var entry = getEntry(x, y, z);
		return entry == null ? null : (entry.value = remappingFunction.translateSpaceEntry(entry.x, entry.y, entry.z, entry.value));
	}
	public T computeIfPresent(Vector3i key, @NonNull SpaceEntryTranslator<? super T, ? extends T> remappingFunction) {
		return computeIfPresent(key.x, key.y, key.z, remappingFunction);
	}
	public T computeIfPresent(BlockPos key, @NonNull SpaceEntryTranslator<? super T, ? extends T> remappingFunction) {
		return computeIfPresent(key.getX(), key.getY(), key.getZ(), remappingFunction);
	}
	@Deprecated @Override public T computeIfPresent(Vector3i key, @NonNull BiFunction<? super Vector3i, ? super T, ? extends T> remappingFunction) {
		return computeIfPresent(key, (x, y, z, value)->remappingFunction.apply(new Vector3i(x, y, z), value));
	}

	public T computeIfAbsent(int x, int y, int z, @NonNull SpaceValueGenerator<? extends T> mappingFunction) {
		return root.getOrCreateEntry(x, y, z, mappingFunction).value;
	}
	public T computeIfAbsent(Vector3i key, @NonNull SpaceValueGenerator<? extends T> mappingFunction) {
		return computeIfAbsent(key.x, key.y, key.z, mappingFunction);
	}
	public T computeIfAbsent(BlockPos key, @NonNull SpaceValueGenerator<? extends T> mappingFunction) {
		return computeIfAbsent(key.getX(), key.getY(), key.getZ(), mappingFunction);
	}
	@Deprecated @Override public T computeIfAbsent(Vector3i key, @NonNull Function<? super Vector3i, ? extends T> mappingFunction) {
		return computeIfAbsent(key, (x, y, z)->mappingFunction.apply(new Vector3i(x, y, z)));
	}

	public interface SpaceEntryConsumer<T> { void acceptSpaceEntry(int x, int y, int z, T value); }
	public interface SpaceEntryTranslator<T, U> { U translateSpaceEntry(int x, int y, int z, T value); }
	public interface SpaceValueGenerator<T> { T generateSpaceValue(int x, int y, int z); }
	public void forEach(SpaceEntryConsumer<T> action) { root.forEach(action); }
	@Deprecated @Override public void forEach(BiConsumer<? super Vector3i, ? super T> action)
	{ forEach((x, y, z, value)->action.accept(new Vector3i(x, y, z), value)); }

	public Iterable<OctreeEntry<T>> fromClosest(double x, double y, double z) { return ()->new SimpleIterator(
		OrderedContainer.doubleCachedPriorityQueue(node -> node.distanceForFromClosest(x, y, z))); }
	public Iterable<OctreeEntry<T>> fromClosest(Vector3d pos) { return fromClosest(pos.x, pos.y, pos.z); }
	public Iterable<OctreeEntry<T>> fromClosest(Vec3 pos) { return fromClosest(pos.x, pos.y, pos.z); }
	public Iterable<OctreeEntry<T>> fromClosestCentered(double x, double y, double z) { return ()->new SimpleIterator(
		OrderedContainer.doubleCachedPriorityQueue(node -> node.distanceForFromClosestCentered(x, y, z))); }
	public Iterable<OctreeEntry<T>> fromClosestCentered(Vector3d pos) { return fromClosestCentered(pos.x, pos.y, pos.z); }
	public Iterable<OctreeEntry<T>> fromClosestCentered(Vec3 pos) { return fromClosestCentered(pos.x, pos.y, pos.z); }
	public Iterable<OctreeEntry<T>> fromClosestBounds(double x, double y, double z) { return ()->new SimpleIterator(
		OrderedContainer.doubleCachedPriorityQueue(node -> cycledSquaredClosestDistance(x, y, z, node))); }
	public Iterable<OctreeEntry<T>> fromClosestBounds(Vector3d pos) { return fromClosestBounds(pos.x, pos.y, pos.z); }
	public Iterable<OctreeEntry<T>> fromClosestBounds(Vec3 pos) { return fromClosestBounds(pos.x, pos.y, pos.z); }

	@Override public void clear() { root.clear(); }

	private static abstract class Node<T> {
		abstract int getX();
		abstract int getY();
		abstract int getZ();
		abstract int side();
		abstract int size();
		abstract boolean containsPos(int x, int y, int z);
		abstract boolean containsKey(int x, int y, int z);
		abstract @Nullable OctreeEntry<T> getEntry(int x, int y, int z);
		// getOrCreateEntry 时约定 containsPos(x, y, z) 为 true
		abstract @NotNull OctreeEntry<T> getOrCreateEntry(int x, int y, int z, @NonNull SpaceValueGenerator<? extends T> remappingFunction);
		abstract T runRemove(int x, int y, int z, AbstractOctreeNode<T> node, int index);
		abstract OctreeEntry<T> getOrSub(OrderedContainer<Node<T>> queue);
		abstract double distanceForFromClosest(double x, double y, double z);
		abstract double distanceForFromClosestCentered(double x, double y, double z);
		abstract void forEach(SpaceEntryConsumer<T> action);
		boolean containsKey(Vector3i pos) { return containsKey(pos.x, pos.y, pos.z); }
		OctreeNode<T> expand() {
			int newRadius = side();
			int mask = -(newRadius << 1);
			return new OctreeNode<>(getX() & mask, getY() & mask, getZ() & mask, newRadius);
		}
	}
	public static class OctreeEntry<T> extends Node<T> implements Entry<Vector3i, T> {
		public final int x, y, z;
		T value;
		private OctreeEntry(int x, int y, int z, T value) { this.x = x; this.y = y; this.z = z; this.value = value; }
		@Deprecated @Override public Vector3i getKey() { return getKey(new Vector3i()); }
		public Vector3i getKey(Vector3i cache) { return cache.set(x, y, z); }
		public BlockPos.MutableBlockPos getKey(BlockPos.MutableBlockPos cache) { return cache.set(x, y, z); }
		public int getX() { return x; }
		public int getY() { return y; }
		public int getZ() { return z; }
		int side() { return 1; }
		@Override public T getValue() { return value; }
		@Override public T setValue(T value) { T old = this.value; this.value = value; return old; }
		@Override int size() { return 1; }
		@Override boolean containsPos(int x, int y, int z) { return x == this.x && y == this.y && z == this.z; }
		@Override boolean containsKey(int x, int y, int z) { return containsPos(x, y, z); }
		@Override @Nullable OctreeEntry<T> getEntry(int x, int y, int z) { return containsPos(x, y, z) ? this : null; }
		@Override @NotNull OctreeEntry<T> getOrCreateEntry(int x, int y, int z, @NonNull SpaceValueGenerator<? extends T> remappingFunction) { return this; }
		@Override T runRemove(int x, int y, int z, AbstractOctreeNode<T> node, int index) { return node.removeOnEntry(this, index); }
		@Override OctreeEntry<T> getOrSub(OrderedContainer<Node<T>> queue) { return this; }
		@Override double distanceForFromClosest(double x, double y, double z) {
			return MathUtils.cycledClosestDistanceSquared(x, y, z, getX(), getY(), getZ());
		}
		@Override double distanceForFromClosestCentered(double x, double y, double z) {
			return MathUtils.cycledClosestDistanceSquared(x - 0.5, y - 0.5, z - 0.5, getX(), getY(), getZ());
		}
		@Override void forEach(SpaceEntryConsumer<T> action) { action.acceptSpaceEntry(x, y, z, value); }
		public T replace(T newValue) {
			T oldValue = value;
			value = newValue;
			return oldValue;
		}
	}

	private abstract static class AbstractOctreeNode<T> extends Node<T> {
		// nodes可null但不可以有空Node或只有一个subNode的OctreeNode
		Object[] nodes = new Object[8];
		int size = 0;
		int nonNullSubNodeFlag = 0;

		abstract public int getX();
		abstract public int getY();
		abstract public int getZ();
		int side() { return getRadius() << 1; }
		int getCenterX() { return getX() + getRadius(); }
		int getCenterY() { return getY() + getRadius(); }
		int getCenterZ() { return getZ() + getRadius(); }
		abstract int getRadius();

		static int indexFromXYZ(boolean x, boolean y, boolean z) { return (x ? 1 : 0) | (y ? 2 : 0) | (z ? 4 : 0); }
		int indexFromXYZ(int x, int y, int z) { return indexFromXYZ(x - getCenterX() >= 0, y - getCenterY() >= 0, z - getCenterZ() >= 0); }
		@SuppressWarnings("unchecked") Node<T> getNode(int i) { return (Node<T>) nodes[i]; }
		Node<T> getNode(boolean x, boolean y, boolean z) { return getNode(indexFromXYZ(x, y, z)); }
		Node<T> getNode(int x, int y, int z) { return containsPos(x, y, z) ? getNode(indexFromXYZ(x, y, z)) : null; }

		@Override int size() { return size; }
		@Override boolean containsPos(int x, int y, int z) {
			return Integer.compareUnsigned(x - getX(), side()) < 0
				&& Integer.compareUnsigned(y - getY(), side()) < 0
				&& Integer.compareUnsigned(z - getZ(), side()) < 0;
		}

		@Override boolean containsKey(int x, int y, int z) {
			Node<T> subNode = getNode(x, y, z);
			return subNode != null && subNode.containsKey(x, y, z);
		}

		@Override @Nullable OctreeEntry<T> getEntry(int x, int y, int z) {
			Node<T> subNode = getNode(x, y, z);
			return subNode == null ? null : subNode.getEntry(x, y, z);
		}

		@Override @NotNull OctreeEntry<T> getOrCreateEntry(int x, int y, int z, @NonNull SpaceValueGenerator<? extends T> mappingFunction) {
			int index = indexFromXYZ(x, y, z);
			Node<T> node = getNode(index);
			if(node == null) {
				OctreeEntry<T> res = new OctreeEntry<>(x, y, z, mappingFunction.generateSpaceValue(x, y, z));
				nodes[index] = res;
				nonNullSubNodeFlag += index | 8;
				++size;
				return res;
			}
			else {
				if (node.containsPos(x, y, z)) {
					int oldSize = node.size();
					OctreeEntry<T> res = node.getOrCreateEntry(x, y, z, mappingFunction);
					size += node.size() - oldSize;
					return res;
				}
				else {
					OctreeNode<T> newNode = node.expand();
					while (!newNode.containsPos(x, y, z)) newNode = newNode.expand();
					nodes[index] = newNode;
					int i = newNode.indexFromXYZ(node.getX(), node.getY(), node.getZ());
					newNode.nodes[i] = node;
					newNode.nonNullSubNodeFlag = i + 8;
					newNode.size = node.size();
					++size;
					return newNode.getOrCreateEntry(x, y, z, mappingFunction);
				}
			}
		}

		@Override T runRemove(int x, int y, int z, AbstractOctreeNode<T> node, int index) { return node.removeOnNode(x, y, z, this, index); }
		@Override OctreeEntry<T> getOrSub(OrderedContainer<Node<T>> queue) {
			for(Object _node : nodes) {
				// noinspection unchecked
				Node<T> node = (Node<T>)_node;
				if(node != null) queue.add(node);
			}
			return null;
		}

		@Override void forEach(SpaceEntryConsumer<T> action) {
			for(Object _node : nodes) {
				@SuppressWarnings("unchecked") Node<T> node = (Node<T>)_node;
				if(node != null) node.forEach(action);
			}
		}

		// remove 时约定 containsPos(x, y, z) 为 true
		@Nullable T remove(int x, int y, int z) {
			int index = indexFromXYZ(x, y, z);
			Node<T> node = getNode(index);
			if(node != null && node.containsPos(x, y, z))
				return node.runRemove(x, y, z, this, index);
			else return null;
		}

		T removeOnNode(int x, int y, int z, AbstractOctreeNode<T> node, int index) {
			int oldSize = node.size;
			T res = node.remove(x, y, z);
			size += node.size - oldSize;
			if(node.nonNullSubNodeFlag < 16)
				nodes[index] = node.nodes[node.nonNullSubNodeFlag & 7];
			return res;
		}

		T removeOnEntry(OctreeEntry<T> node, int index) {
			nodes[index] = null;
			nonNullSubNodeFlag -= index | 8;
			--size;
			return node.value;
		}
	}

	private static class OctreeNode<T> extends AbstractOctreeNode<T> {
		final int x, y, z, radius;

		OctreeNode(int x, int y, int z, int radius) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.radius = radius;
		}

		@Override public int getX() { return x; }
		@Override public int getY() { return y; }
		@Override public int getZ() { return z; }

		@Override int getRadius() { return radius; }

		@Override double distanceForFromClosest(double x, double y, double z) { return SimpleSpaceOctreeMap.cycledSquaredClosestDistance(x, y, z, this); }
		@Override double distanceForFromClosestCentered(double x, double y, double z) { return SimpleSpaceOctreeMap.cycledSquaredClosestDistance(x, y, z, this); }
	}

	private static class RootOctreeNode<T> extends AbstractOctreeNode<T> {
		@Override public int getX() { return Integer.MIN_VALUE; }
		@Override public int getY() { return Integer.MIN_VALUE; }
		@Override public int getZ() { return Integer.MIN_VALUE; }
		@Override int getRadius() { return Integer.MIN_VALUE; }
		@Override boolean containsPos(int x, int y, int z) { return true; }
		@Override double distanceForFromClosest(double x, double y, double z) { return 0; }
		@Override double distanceForFromClosestCentered(double x, double y, double z) { return 0; }
		void clear() {
			Arrays.fill(nodes, null);
			nonNullSubNodeFlag = 0;
			size = 0;
		}
	}

	public class EntrySet extends AbstractSet<OctreeEntry<T>> {
		@Override public @NonNull SimpleIterator iterator() { return new SimpleIterator(0, 0, 0); }
		@Override public int size() { return SimpleSpaceOctreeMap.this.size(); }
		@Override public void clear() { root.clear(); }
	}

	public class SimpleIterator implements Iterator<OctreeEntry<T>> {
		// orderedContainer中不可有空node
		final OrderedContainer<Node<T>> orderedContainer;
		OctreeEntry<T> lastReturned;

		SimpleIterator(OrderedContainer<Node<T>> orderedContainer) {
			this.orderedContainer = orderedContainer;
			if(root.size != 0) orderedContainer.add(root);
		}

		private SimpleIterator(double startX, double startY, double startZ) {
			this(OrderedContainer.warpQueue(new PriorityQueue<>(Comparator.comparingDouble(node -> cycledSquaredClosestDistance(startX, startY, startZ, node)))));
		}

		@Override public boolean hasNext() { return !orderedContainer.isEmpty(); }

		@Override public OctreeEntry<T> next() {
			while (orderedContainer.poll() instanceof Node<T> node) {
				OctreeEntry<T> entry = node.getOrSub(orderedContainer);
				if(entry != null) {
					lastReturned = entry;
					return entry;
				}
			}
			throw new NoSuchElementException();
		}

		@Override public void remove() {
			if(lastReturned == null) throw new IllegalStateException();
			SimpleSpaceOctreeMap.this.remove(lastReturned.x, lastReturned.y, lastReturned.z);
			lastReturned = null;
		}
	}

	private static double cycledSquaredClosestDistance(double startX, double startY, double startZ, SimpleSpaceOctreeMap.Node<?> node) {
		return MathUtils.cycledClosestDistanceSquared(startX, startY, startZ, node.getX(), node.getY(), node.getZ(), node.side(), node.side(), node.side());
	}

	public void validate() {
		int[] entryCount = new int[1];
		validateNode(root, entryCount, null, -1);
		if(entryCount[0] != root.size)
			throw new AssertionError("root.size=" + root.size + " but counted " + entryCount[0]);
		System.out.println("validate OK: " + entryCount[0] + " entries");
	}

	private void validateNode(Node<T> node, int[] count, AbstractOctreeNode<T> parent, int indexInParent) {
		if(parent != null) {
			int expectedIndex = parent.indexFromXYZ(node.getX(), node.getY(), node.getZ());
			if(expectedIndex != indexInParent)
				throw new AssertionError("Wrong index: node (" + node.getX() + "," + node.getY() + "," + node.getZ()
					+ ") in parent slot " + indexInParent + ", expected " + expectedIndex
					+ ". parent=(" + parent.getX() + "," + parent.getY() + "," + parent.getZ() + ",side=" + parent.side() + ")");
			if(!parent.containsPos(node.getX(), node.getY(), node.getZ()))
				throw new AssertionError("Child origin outside parent: child=(" + node.getX() + "," + node.getY() + "," + node.getZ()
					+ ") parent=(" + parent.getX() + "," + parent.getY() + "," + parent.getZ() + ",side=" + parent.side() + ")");
		}
		if(node instanceof OctreeEntry) {
			count[0]++;
		} else if(node instanceof AbstractOctreeNode<T> oct) {
			int actualNonNull = 0, sumChildSize = 0, expectedFlag = 0;
			for(int i = 0; i < 8; ++i) {
				@SuppressWarnings("unchecked")
				Node<T> child = (Node<T>)oct.nodes[i];
				if(child != null) {
					actualNonNull++;
					sumChildSize += child.size();
					expectedFlag += i | 8;
					validateNode(child, count, oct, i);
				}
			}
			if(oct.nonNullSubNodeFlag != expectedFlag)
				throw new AssertionError("nonNullSubNodeFlag mismatch: flag=" + oct.nonNullSubNodeFlag
					+ " expected=" + expectedFlag + " at (" + oct.getX() + "," + oct.getY() + "," + oct.getZ() + ")");
			if(oct.size != sumChildSize)
				throw new AssertionError("size mismatch: node.size=" + oct.size + " sumChildren=" + sumChildSize
					+ " at (" + oct.getX() + "," + oct.getY() + "," + oct.getZ() + ")");
			if(oct instanceof OctreeNode && actualNonNull == 1)
				throw new AssertionError("OctreeNode has only 1 child at (" + oct.getX() + "," + oct.getY() + "," + oct.getZ() + ")");
		}
	}
}
