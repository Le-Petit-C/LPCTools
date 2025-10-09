package lpctools.util.data;

import com.google.common.collect.Multiset;
import it.unimi.dsi.fastutil.ints.AbstractIntList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

//一个支持O(log(n))按元素查找，按元素插入删除，按索引查找，按索引插入删除的元素可重复的集合
public class UltimateMultiCollection<E> extends AvlTreeList<E> implements Multiset<E> {
	protected final HashMap<E, AvlTreeList<UltimateNode<E>>> nodeMap = new HashMap<>();
	
	private static final Function<?, AvlTreeList<UltimateNode<?>>> treeAllocator = o->new AvlTreeList<>();
	
	private static <T> Function<T, AvlTreeList<UltimateNode<T>>> treeAllocator(){
		//noinspection unchecked
		return (Function<T, AvlTreeList<UltimateNode<T>>>)(Object) treeAllocator;
	}
	
	public UltimateMultiCollection(){
		//noinspection unchecked
		super((Node<E>) UltimateNode.EMPTY_LEAF);
	}
	
	public UltimateMultiCollection(Collection<? extends E> coll){
		this();
		Objects.requireNonNull(coll);
		addAll(coll);
	}
	
	protected UltimateMultiCollection(@NotNull UltimateNode<E> root){
		super(root);
	}
	
	/* 需要好好实现的一般方法 */
	
	//获取sortedList中的目标index使得sortedList.get(index - 1).getIndex() < targetIndex <= sortedList.get(index).getIndex()
	private static <T> int getMultiListIndex(AvlTreeList<UltimateNode<T>> sortedList, int targetIndex){
		var node = sortedList.root;
		int idx = 0;
		while(node != node.getEmptyLeaf()){
			int i = node.value.getIndex();
			if(targetIndex > i){
				idx += node.left.size + 1;
				node = node.right;
			}
			else if(targetIndex < i) node = node.left;
			else {
				idx += node.left.size;
				break;
			}
		}
		return idx;
	}
	
	@Override public E set(int index, E val) {
		var node = (UltimateNode<E>) root.getNodeAt(index);
		var res = node.value;
		var list = nodeMap.get(res);
		node.value = val;
		list.remove(getMultiListIndex(list, index));
		if(list.isEmpty()) nodeMap.remove(res);
		list = nodeMap.computeIfAbsent(val, treeAllocator());
		list.add(getMultiListIndex(list, index), node);
		return res;
	}
	
	@Override public void add(int index, E val) {
		if (index < 0 || index > size())  // Different constraint than the other methods
			throw new IndexOutOfBoundsException();
		if (size() == Integer.MAX_VALUE)
			throw new IllegalStateException("Maximum size reached");
		root = root.insertAt(index, val, v->{
			var list = nodeMap.computeIfAbsent(val, treeAllocator());
			list.add(getMultiListIndex(list, index), (UltimateNode<E>) v);
		});
	}
	
	@Override public E remove(int index) {
		MutableObject<E> res = new MutableObject<>();
		root = root.removeAt(index, n->{
			UltimateNode<E> node = (UltimateNode<E>)n;
			res.setValue(node.value);
			var list = nodeMap.get(node.value);
			list.remove(getMultiListIndex(list, index));
			if(list.isEmpty()) nodeMap.remove(node.value);
		});
		return res.getValue();
	}
	
	@Override public boolean remove(Object o) {
		//noinspection SuspiciousMethodCalls
		var list = nodeMap.get(o);
		if(list == null || list.isEmpty()) return false;
		remove(list.getFirst().getIndex());
		return true;
	}
	
	@Override public boolean contains(Object o) {
		//noinspection SuspiciousMethodCalls
		return nodeMap.containsKey(o);
	}
	
	@Override public int count(Object element) {
		//noinspection SuspiciousMethodCalls
		var list = nodeMap.get(element);
		if(list != null) return list.size();
		else return 0;
	}
	
	//默认移除前occurrences个
	@Override public int remove(Object element, int occurrences) {
		if(occurrences < 0) throw new IllegalArgumentException("occurrences can't be negative");
		//noinspection SuspiciousMethodCalls
		var list = nodeMap.get(element);
		int res = list.size();
		int n = Math.min(res, occurrences);
		if(n == 0) return res;
		int[] buf = new int[n];
		for(int i = 0; i < n; ++i)
			buf[i] = list.get(n - i - 1).getIndex();
		for(int i : buf) remove(i);
		return res;
	}
	
	//移除全部o元素
	public boolean removeAll(Object o) {
		//noinspection SuspiciousMethodCalls
		var list = nodeMap.get(o);
		int n = list.size();
		if(n == 0) return false;
		int[] buf = new int[n];
		for(int i = 0; i < n; ++i)
			buf[i] = list.get(n - i - 1).getIndex();
		for(int i : buf) remove(i);
		return true;
	}
	
	@Override public @NotNull Set<E> elementSet() {return elementSet;}
	
	@Override public int indexOf(Object o){
		//noinspection SuspiciousMethodCalls
		var list = nodeMap.get(o);
		return list != null ? list.getFirst().getIndex() : -1;
	}
	
	@Override public int lastIndexOf(Object o){
		//noinspection SuspiciousMethodCalls
		var list = nodeMap.get(o);
		return list != null ? list.getLast().getIndex() : -1;
	}
	
	/**
	 *  Get the index list of an element
	 * @param	o element to search
	 * @return	Immutable index list reference(changes with collection), empty if o does not exist
	 */
	public @NotNull IntList getIndexList(Object o){
		return new AbstractIntList() {
			@Override public int getInt(int i) {
				//noinspection SuspiciousMethodCalls
				var list = nodeMap.get(o);
				if(list == null) throw new IndexOutOfBoundsException();
				return list.get(i).getIndex();
			}
			@Override public int size() {
				//noinspection SuspiciousMethodCalls
				var list = nodeMap.get(o);
				return list != null ? list.size() : 0;
			}
		};
	}
	
	/**
	 *  Get the index list of an element, copy and restore in the given storage list
	 * @param 	o element to search
	 * @param 	res Result storage list
	 * @return 	{@code res}
	 */
	@Contract("_, _->param2")
	public @NotNull IntList getIndexList(Object o, IntList res){
		//noinspection SuspiciousMethodCalls
		var list = nodeMap.get(o);
		int size = list != null ? list.size() : 0;
		res.size(size);
		int i = 0;
		if (list != null) {
			for(var v : list)
				res.set(i++, v.getIndex());
		}
		return res;
	}
	
	/**
	 *  Get the index of an element
	 * @param 	o element to search
	 * @param 	index Occurrences index of the element
	 * @return 	Index of the "index + 1" occurrences to the element
	 * @throws IndexOutOfBoundsException if o does not exist or index out of index list bound
	 */
	public int indexOf(Object o, int index){
		//noinspection SuspiciousMethodCalls
		var list = nodeMap.get(o);
		if(list == null) throw new IndexOutOfBoundsException();
		else return list.get(index).getIndex();
	}
	
	/**
	 *  Insert before the first occurrence of an object
	 * @param 	o element to search
	 * @param 	element element to insert
	 * @return 	{@code true} if inserts successfully, {@code false} if insertion failed (Most probably because {@code o} not found)
	 */
	public boolean insertBeforeFirst(Object o, E element){
		int i = indexOf(o);
		if(i < 0) return false;
		add(i, element);
		return true;
	}
	
	/**
	 *  Insert after the first occurrence of an object
	 * @param 	o element to search
	 * @param 	element element to insert
	 * @return 	{@code true} if inserts successfully, {@code false} if insertion failed (Most probably because {@code o} not found)
	 */
	public boolean insertAfterFirst(Object o, E element){
		int i = indexOf(o);
		if(i < 0) return false;
		add(i + 1, element);
		return true;
	}
	
	/**
	 *  Insert before the last occurrence of an object
	 * @param 	o element to search
	 * @param 	element element to insert
	 * @return 	{@code true} if inserts successfully, {@code false} if insertion failed (Most probably because {@code o} not found)
	 */
	public boolean insertBeforeLast(Object o, E element){
		int i = lastIndexOf(o);
		if(i < 0) return false;
		add(i, element);
		return true;
	}
	
	/**
	 *  Insert after the last occurrence of an object
	 * @param 	o element to search
	 * @param 	element element to insert
	 * @return 	{@code true} if inserts successfully, {@code false} if insertion failed (Most probably because {@code o} not found)
	 */
	public boolean insertAfterLast(Object o, E element){
		int i = lastIndexOf(o);
		if(i < 0) return false;
		add(i + 1, element);
		return true;
	}
	
	/**
	 *  Insert before the specific occurrence of an object
	 * @param 	o element to search
	 * @param 	element element to insert
	 * @throws IndexOutOfBoundsException if o does not exist or index out of index list bound
	 */
	public void insertBefore(Object o, int index, E element){
		int i = indexOf(o, index);
		add(i, element);
	}
	
	/**
	 *  Insert after the specific occurrence of an object
	 * @param 	o element to search
	 * @param 	element element to insert
	 * @throws IndexOutOfBoundsException if o does not exist or index out of index list bound
	 */
	public void insertAfter(Object o, int index, E element){
		int i = indexOf(o, index);
		add(i + 1, element);
	}
	
	/* 不推荐使用的方法，但是为了满足MultiSet仍然写了实现 */
	
	//往列表最后面加occurrences个element
	@Override public int add(E element, int occurrences) {
		if(occurrences < 0) throw new IllegalArgumentException("occurrences can't be negative");
		int res = count(element);
		for(int i = 0; i < occurrences; ++i) add(element);
		return res;
	}
	
	//减少时从最后一个往前移除，新添时在列表最末往后添加
	@Override public int setCount(E element, int count) {
		if(count < 0) throw new IllegalArgumentException("count can't be negative");
		int c = count(element) - count;
		if(c < 0) return add(element, -c);
		else if(c > 0) return remove(element, c);
		else return c;
	}
	
	//减少时从最后一个往前移除，新添时在列表最末往后添加
	@Override public boolean setCount(E element, int oldCount, int newCount) {
		if(oldCount < 0 || newCount < 0) throw new IllegalArgumentException("Both oldCount and newCount can't be negative");
		if(oldCount == newCount) return false;
		int old = count(element);
		if(old != oldCount) return false;
		if(oldCount < newCount) add(element, newCount - oldCount);
		else remove(element, oldCount - newCount);
		return true;
	}
	
	@Override public @NotNull Set<Entry<E>> entrySet() {return entrySet;}
	
	@Override public @NotNull Spliterator<E> spliterator() {return super.spliterator();}
	
	@Override public @NotNull String toString() {return super.toString();}
	
	private final @NotNull Set<E> elementSet = new Set<>() {
		@Override public int size() {return nodeMap.size();}
		@Override public boolean isEmpty() {return UltimateMultiCollection.this.isEmpty();}
		@SuppressWarnings("SuspiciousMethodCalls")
		@Override public boolean contains(Object o) {return nodeMap.containsKey(o);}
		@Override public @NotNull Iterator<E> iterator() {return nodeMap.keySet().iterator();}
		@Override public @NotNull Object @NotNull [] toArray() {return nodeMap.keySet().toArray();}
		@Override public @NotNull <T1> T1 @NotNull [] toArray(@NotNull T1 @NotNull [] a) {return nodeMap.keySet().toArray(a);}
		@Override public boolean add(E e) {return UltimateMultiCollection.this.add(e);}
		@Override public boolean remove(Object o) {return UltimateMultiCollection.this.remove(o);}
		@Override public boolean containsAll(@NotNull Collection<?> c) {return nodeMap.keySet().containsAll(c);}
		@Override public boolean addAll(@NotNull Collection<? extends E> c) {return UltimateMultiCollection.this.addAll(c);}
		@Override public boolean retainAll(@NotNull Collection<?> c) {return UltimateMultiCollection.this.retainAll(c);}
		@Override public boolean removeAll(@NotNull Collection<?> c) {return UltimateMultiCollection.this.removeAll(c);}
		@Override public void clear() {UltimateMultiCollection.this.clear();}
	};
	
	private final @NotNull Set<Entry<E>> entrySet = new AbstractSet<>() {
		@Override public int size() {return elementSet.size();}
		@Override public boolean isEmpty() {return UltimateMultiCollection.this.isEmpty();}
		@Override public boolean contains(Object o) {
			if(!(o instanceof Multiset.Entry<?> entry)) return false;
			return UltimateMultiCollection.this.count(entry.getElement()) == entry.getCount();
		}
		@Override public @NotNull Iterator<Entry<E>> iterator() {
			return new Iterator<>() {
				final Iterator<Map.Entry<E, AvlTreeList<UltimateNode<E>>>> iter = nodeMap.entrySet().iterator();
				@Override public boolean hasNext() {
					return iter.hasNext();
				}
				@Override public Entry<E> next() {
					var entry = iter.next();
					return new Entry<>() {
						@Override public E getElement() {return entry.getKey();}
						@Override public int getCount() {return entry.getValue().size();}
					};
				}
			};
		}
		
		@Override public boolean add(Entry<E> eEntry) {
			int count = eEntry.getCount();
			if(count == 0) return false;
			UltimateMultiCollection.this.add(eEntry.getElement(), count);
			return true;
		}
		
		@Override public boolean remove(Object o) {
			if(!(o instanceof Multiset.Entry<?> entry)) return false;
			int count = entry.getCount();
			if(count == 0) return false;
			UltimateMultiCollection.this.remove(entry.getElement(), count);
			return true;
		}
		
		@Override public void clear() {UltimateMultiCollection.this.clear();}
	};
	
	protected static class UltimateNode<E> extends Node<E> {
		public static final UltimateNode<?> EMPTY_LEAF = new UltimateNode<>();
		public @NotNull UltimateNode<E> parent;
		
		protected UltimateNode(){
			super();
			parent = this;
		}
		
		protected UltimateNode(E obj){
			super(obj);
			//noinspection unchecked
			parent = (UltimateNode<E>) EMPTY_LEAF;
		}
		
		UltimateNode<E> assertAndSetParent(Node<E> v){
			assert v instanceof UltimateNode<E>;
			var res = (UltimateNode<E>)v;
			if(res != getEmptyLeaf()) res.parent = this;
			return res;
		}
		
		@Override protected void recalculate() {
			if(left != getEmptyLeaf()) ((UltimateNode<E>)left).parent = this;
			if(right != getEmptyLeaf()) ((UltimateNode<E>)right).parent = this;
			super.recalculate();
		}
		
		@Override protected Node<E> rotateRight() {
			var left = (UltimateNode<E>)this.left;
			left.parent = parent;
			parent = left;
			super.rotateRight();
			left = (UltimateNode<E>)this.left;
			if(left != left.getEmptyLeaf())
				left.parent = this;
			return parent;
		}
		
		@Override protected Node<E> rotateLeft() {
			var right = (UltimateNode<E>)this.right;
			right.parent = parent;
			parent = right;
			super.rotateLeft();
			right = (UltimateNode<E>)this.right;
			if(right != right.getEmptyLeaf())
				right.parent = this;
			return parent;
		}
		
		@Override public UltimateNode<E> removeAt(int index, @Nullable Consumer<Node<E>> removed) {
			assert 0 <= index && index < size;
			int leftSize = left.size;
			if (index < leftSize) left = left.removeAt(index, removed);
			else if (index > leftSize) right = right.removeAt(index - leftSize - 1, removed);
			else {
				if (removed != null) removed.accept(this);
				if (left == getEmptyLeaf() && right == getEmptyLeaf())
					return getEmptyLeaf();
				else if (left != getEmptyLeaf() && right == getEmptyLeaf()){
					((UltimateNode<E>)left).parent = parent;
					return (UltimateNode<E>) left;
				}
				else if (left == getEmptyLeaf() /*&& right != getEmptyLeaf()(需要满足但是冗余的判断)*/){
					((UltimateNode<E>)right).parent = parent;
					return (UltimateNode<E>) right;
				}
				else {
					MutableObject<Node<E>> res = new MutableObject<>();
					right = right.removeAt(0, res::setValue);
					var node = res.getValue();
					replaceByNode(node);
					node.recalculate();
					return (UltimateNode<E>) node.balance();
				}
			}
			recalculate();
			return (UltimateNode<E>) balance();
		}
		
		
		@Override public Node<E> replaceByNode(Node<E> n) {
			var node = (UltimateNode<E>)n;
			node.parent = parent;
			if(left != getEmptyLeaf()) ((UltimateNode<E>)left).parent = node;
			if(right != getEmptyLeaf()) ((UltimateNode<E>)right).parent = node;
			var res = super.replaceByNode(n);
			//这个节点不应该再会被用到，如果用到了就抛出NullPointerException
			//noinspection DataFlowIssue
			left = right = parent = null;
			return res;
		}
		
		@Override public String toString() {
			StringBuilder builder = new StringBuilder();
			testBuild(builder, 0);
			return builder.toString();
		}
		
		@Override protected UltimateNode<E> getEmptyLeaf(){
			//noinspection unchecked
			return (UltimateNode<E>) EMPTY_LEAF;
		}
		
		@Override protected UltimateNode<E> allocate(E obj) {
			return new UltimateNode<>(obj);
		}
		
		public int getIndex(){
			int res = left.size;
			UltimateNode<E> node = this;
			while(node != node.getEmptyLeaf()){
				if(node.parent.right == node) res += node.parent.left.size + 1;
				node = node.parent;
			}
			return res;
		}
		
		@Override public void testBuild(StringBuilder builder, int depth){
			builder.append("    ".repeat(depth));
			builder.append(getClass().getName()).append("@").append(Integer.toHexString(hashCode())).append(":{\n");
			if(left != left.getEmptyLeaf()) {
				builder.append("    ".repeat(depth + 1));
				builder.append("left(parent check ");
				builder.append(((UltimateNode<E>)left).parent == this ? "ok" : "failed");
				builder.append("):\n");
				left.testBuild(builder, depth + 1);
			}
			builder.append("    ".repeat(depth + 1));
			builder.append(String.format("UltimateNode(size=%d, height=%d, val=%s)\n", size, height, value));
			builder.append("    ".repeat(depth + 1));
			builder.append(String.format("Extra:(index=%d, parentEmpty=%b)\n", getIndex(), parent == getEmptyLeaf()));
			if(right != right.getEmptyLeaf()) {
				builder.append("    ".repeat(depth + 1));
				builder.append("right(parent check ");
				builder.append(((UltimateNode<E>)right).parent == this ? "ok" : "failed");
				builder.append("):\n");
				right.testBuild(builder, depth + 1);
			}
			builder.append("    ".repeat(depth));
			builder.append("}\n");
		}
		
		@Override void checkStructure(Set<Node<E>> visitedNodes) {
			super.checkStructure(visitedNodes);
			if(parent != getEmptyLeaf() && parent.left != this && parent.right != this)
				throw new AssertionError("AVL tree structure violated: parent check failed:\n" + this + "parent:\n" + parent);
		}
	}
	
	@SuppressWarnings("UnusedReturnValue")
	public StringBuilder testBuild(StringBuilder builder){
		if(root != root.getEmptyLeaf())
			root.testBuild(builder, 0);
		return builder;
	}
	
	public void checkStructure(){
		HashSet<Node<E>> nodes = new HashSet<>();
		var root = (UltimateNode<E>)this.root;
		if(root.parent != root.getEmptyLeaf()){
			var realRoot = root;
			while(realRoot.parent != root.getEmptyLeaf()) realRoot = realRoot.parent;
			throw new AssertionError("Ultimate structure violated: Root not root\nroot:" + root +"\nrealRoot:" + realRoot);
		}
		if(root != root.getEmptyLeaf())
			root.checkStructure(nodes);
		nodeMap.forEach((v, list)->{
			for(var node : list){
				if(!nodes.remove(node))
					throw new AssertionError("Ultimate structure violated: nodeMap contains invalid node:" + node);
			}
		});
	}
}
