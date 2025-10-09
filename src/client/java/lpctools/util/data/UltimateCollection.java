package lpctools.util.data;

import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

//一个支持O(log(n))按元素查找，按元素插入删除，按索引查找，按索引插入删除的元素不可重复的集合
public class UltimateCollection<E> extends AvlTreeList<E> implements Set<E> {
	protected final HashMap<E, UltimateNode<E>> nodeMap = new HashMap<>();
	
	private static final Function<?, AvlTreeList<UltimateNode<?>>> treeAllocator = o->new AvlTreeList<>();
	
	private static <T> Function<T, AvlTreeList<UltimateNode<T>>> treeAllocator(){
		//noinspection unchecked
		return (Function<T, AvlTreeList<UltimateNode<T>>>)(Object) treeAllocator;
	}
	
	public UltimateCollection(){
		//noinspection unchecked
		super((Node<E>) UltimateNode.EMPTY_LEAF);
	}
	
	public UltimateCollection(Collection<? extends E> coll){
		this();
		Objects.requireNonNull(coll);
		addAll(coll);
	}
	
	protected UltimateCollection(@NotNull UltimateNode<E> root){
		super(root);
	}
	
	/* 需要好好实现的一般方法 */
	
	//设置失败（集合中已有val）则返回val
	@Override public E set(int index, E val) {
		if(nodeMap.containsKey(val)) return val;
		var node = (UltimateNode<E>) root.getNodeAt(index);
		var res = node.value;
		node.value = val;
		nodeMap.remove(res);
		nodeMap.put(val, node);
		return res;
	}
	
	//设置失败（集合中已有val）时不进行操作
	@Override public void add(int index, E val) {
		if(nodeMap.containsKey(val)) return;
		if (index < 0 || index > size())  // Different constraint than the other methods
			throw new IndexOutOfBoundsException();
		root = root.insertAt(index, val, v->nodeMap.put(val, (UltimateNode<E>) v));
	}
	
	@Override public E remove(int index) {
		MutableObject<E> res = new MutableObject<>();
		root = root.removeAt(index, n->{
			res.setValue(n.value);
			nodeMap.remove(n.value);
		});
		return res.getValue();
	}
	
	@Override public boolean remove(Object o) {
		//noinspection SuspiciousMethodCalls
		var node = nodeMap.get(o);
		if(node == null) return false;
		remove(node.getIndex());
		return true;
	}
	
	@Override public boolean contains(Object o) {
		//noinspection SuspiciousMethodCalls
		return nodeMap.containsKey(o);
	}
	
	@Override public int indexOf(Object o){
		//noinspection SuspiciousMethodCalls
		var node = nodeMap.get(o);
		return node != null ? node.getIndex() : -1;
	}
	
	@Override public int lastIndexOf(Object o){
		//noinspection SuspiciousMethodCalls
		var node = nodeMap.get(o);
		return node != null ? node.getIndex() : -1;
	}
	
	/**
	 *  Insert before an object
	 * @param 	o element to search
	 * @param 	element element to insert
	 * @return {@code true} if succeeded, {@code false} if failed, depends on o and element's existence.
	 */
	public boolean insertBefore(Object o, E element){
		int i = indexOf(o);
		if(i < 0 || nodeMap.containsKey(element)) return false;
		add(i, element);
		return true;
	}
	
	/**
	 *  Insert after an object
	 * @param 	o element to search
	 * @param 	element element to insert
	 * @return {@code true} if succeeded, {@code false} if failed, depends on o and element's existence.
	 */
	public boolean insertAfter(Object o, E element){
		int i = indexOf(o);
		if(i < 0 || nodeMap.containsKey(element)) return false;
		add(i + 1, element);
		return true;
	}
	
	/* 不推荐使用的方法，但是为了满足MultiSet仍然写了实现 */
	
	@Override public @NotNull Spliterator<E> spliterator() {return super.spliterator();}
	
	@Override public @NotNull String toString() {return super.toString();}
	
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
		nodeMap.forEach((v, node)->{
			if(!nodes.remove(node))
				throw new AssertionError("Ultimate structure violated: nodeMap contains invalid node:" + node);
		});
	}
}
