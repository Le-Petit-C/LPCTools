package lpctools.util.data;

/*
 * AVL tree list (Java)
 * 
 * Copyright (c) 2018 Project Nayuki. (MIT License)
 * https://www.nayuki.io/page/avl-tree-list
 *
 * Modifications copyright (c) 2025 Le_Petit_C (MIT License)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * - The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 * - The Software is provided "as is", without warranty of any kind, express or
 *   implied, including but not limited to the warranties of merchantability,
 *   fitness for a particular purpose and noninfringement. In no event shall the
 *   authors or copyright holders be liable for any claim, damages or other
 *   liability, whether in an action of contract, tort or otherwise, arising from,
 *   out of or in connection with the Software or the use or other dealings in the
 *   Software.
 */

import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class AvlTreeList<E> extends AbstractList<E> {
	
	/*---- Fields ----*/
	
	protected @NotNull Node<E> root;
	
	
	
	/*---- Constructors ----*/
	
	public AvlTreeList() {
		//noinspection unchecked
		root = (Node<E>) Node.EMPTY_LEAF;
	}
	
	
	public AvlTreeList(Collection<? extends E> coll) {
		this();
		Objects.requireNonNull(coll);
		addAll(coll);
	}
	
	protected AvlTreeList(@NotNull Node<E> root){
		this.root = root;
	}
	
	
	
	/*---- Methods ----*/
	
	// Must not exceed Integer.MAX_VALUE.
	public int size() {
		return root.size;
	}
	
	
	public E get(int index) {
		if (index < 0 || index >= size())
			throw new IndexOutOfBoundsException();
		return root.getNodeAt(index).value;
	}
	
	
	public E set(int index, E val) {
		if (index < 0 || index >= size())
			throw new IndexOutOfBoundsException();
		Node<E> node = root.getNodeAt(index);
		E result = node.value;
		node.value = val;
		return result;
	}
	
	
	public void add(int index, E val) {
		if (index < 0 || index > size())  // Different constraint than the other methods
			throw new IndexOutOfBoundsException();
		if (size() == Integer.MAX_VALUE)
			throw new IllegalStateException("Maximum size reached");
		root = root.insertAt(index, val, null);
	}
	
	
	public E remove(int index) {
		if (index < 0 || index >= size())
			throw new IndexOutOfBoundsException();
		MutableObject<E> result = new MutableObject<>();
		root = root.removeAt(index, v->result.setValue(v.value));
		return result.getValue();
	}
	
	
	public void clear() {
		root = root.getEmptyLeaf();
	}
	
	
	public @NotNull Iterator<E> iterator() {
		return new Iter();
	}
	
	
	// For unit tests.
	public void checkStructure() {
		root.checkStructure(new HashSet<>());
	}
	
	
	
	/*---- Helper class: AVL tree node ----*/
	
	protected static class Node<E> {
		
		// A bit of a hack, but more elegant than using null values as leaf nodes.
		public static final Node<?> EMPTY_LEAF = new Node<>();
		
		
		/*-- Fields --*/
		
		// The object stored at this node. Can be null.
		public E value;
		
		// The height of the tree rooted at this node. Empty nodes have height 0.
		// This node has height equal to max(left.height, right.height) + 1.
		protected int height;
		
		// The number of non-empty nodes in the tree rooted at this node, including this node.
		// Empty nodes have size 0. This node has size equal to left.size + right.size + 1.
		public int size;
		
		// The root node of the left subtree.
		public @NotNull Node<E> left;
		
		// The root node of the right subtree.
		public @NotNull Node<E> right;
		
		
		/*-- Constructors --*/
		
		// For the singleton empty leaf node.
		protected Node() {
			value  = null;
			height = 0;
			size   = 0;
			left   = this;
			right  = this;
		}
		
		
		// Normal non-leaf nodes.
		protected Node(E val) {
			value  = val;
			height = 1;
			size   = 1;
			left   = getEmptyLeaf();
			right  = getEmptyLeaf();
		}
		
		
		/*-- Methods --*/
		
		public Node<E> getNodeAt(int index) {
			assert 0 <= index && index < size;  // Automatically implies this != EMPTY_LEAF, because EMPTY_LEAF.size == 0
			int leftSize = left.size;
			if (index < leftSize) return left.getNodeAt(index);
			else if (index > leftSize) return right.getNodeAt(index - leftSize - 1);
			else return this;
		}
		
		
		public Node<E> insertAt(int index, E obj, @Nullable Consumer<Node<E>> result) {
			assert 0 <= index && index <= size;
			if (this == getEmptyLeaf()){// Automatically implies index == 0, because EMPTY_LEAF.size == 0
				var res = allocate(obj);
				if (result != null) result.accept(res);
				return res;
			}
			int leftSize = left.size;
			if (index <= leftSize) left = left.insertAt(index, obj, result);
			else right = right.insertAt(index - leftSize - 1, obj, result);
			recalculate();
			return balance();
		}
		
		
		public Node<E> removeAt(int index, @Nullable Consumer<Node<E>> removed) {
			assert 0 <= index && index < size;  // Automatically implies this != EMPTY_LEAF, because EMPTY_LEAF.size == 0
			int leftSize = left.size;
			if (index < leftSize) left = left.removeAt(index, removed);
			else if (index > leftSize) right = right.removeAt(index - leftSize - 1, removed);
			else {
				if (removed != null) removed.accept(this);
				if (left == getEmptyLeaf() && right == getEmptyLeaf())
					return getEmptyLeaf();
				else if (left != getEmptyLeaf() && right == getEmptyLeaf())
					return left;
				else if (left == getEmptyLeaf() /*&& right != getEmptyLeaf()(需要满足但是冗余的判断)*/)
					return right;
				else {
					MutableObject<Node<E>> res = new MutableObject<>();
					right = right.removeAt(0, res::setValue);
					var node = res.getValue();
					replaceByNode(node);
					node.recalculate();
					return node.balance();
				}
			}
			recalculate();
			return balance();
		}
		
		//让node替换自己的位置（仅替换位置但是值不变，以及parent的左右节点由parent自己设置）
		@Contract("_->param1")
		public Node<E> replaceByNode(Node<E> node){
			node.left = left;
			node.right = right;
			node.size = size;
			node.height = height;
			return node;
		}
		
		
		@Override public String toString() {
			return String.format("AvlTreeNode(size=%d, height=%d, val=%s)", size, height, value);
		}
		
		
		// Balances the subtree rooted at this node and returns the new root.
		protected Node<E> balance() {
			int bal = getBalance();
			assert Math.abs(bal) <= 2;
			Node<E> result = this;
			if (bal == -2) {
				assert Math.abs(left.getBalance()) <= 1;
				if (left.getBalance() == +1)
					left = left.rotateLeft();
				result = rotateRight();
			} else if (bal == +2) {
				assert Math.abs(right.getBalance()) <= 1;
				if (right.getBalance() == -1)
					right = right.rotateRight();
				result = rotateLeft();
			}
			assert Math.abs(result.getBalance()) <= 1;
			return result;
		}
		
		
		/* 
		 *   A            B
		 *  / \          / \
		 * 0   B   ->   A   2
		 *    / \      / \
		 *   1   2    0   1
		 */
		protected Node<E> rotateLeft() {
			assert right != getEmptyLeaf();
			Node<E> root = this.right;
			this.right = root.left;
			root.left = this;
			this.recalculate();
			root.recalculate();
			return root;
		}
		
		
		/* 
		 *     B          A
		 *    / \        / \
		 *   A   2  ->  0   B
		 *  / \            / \
		 * 0   1          1   2
		 */
		protected Node<E> rotateRight() {
			assert left != getEmptyLeaf();
			Node<E> root = this.left;
			this.left = root.right;
			root.right = this;
			this.recalculate();
			root.recalculate();
			return root;
		}
		
		
		// Needs to be called every time the left or right subtree is changed.
		// Assumes the left and right subtrees have the correct values computed already.
		protected void recalculate() {
			assert this != getEmptyLeaf();
			assert left.height >= 0 && right.height >= 0;
			assert left.size >= 0 && right.size >= 0;
			height = Math.max(left.height, right.height) + 1;
			size = left.size + right.size + 1;
			assert height >= 0 && size >= 0;
		}
		
		
		private int getBalance() {
			return right.height - left.height;
		}
		
		
		// For unit tests, invokable by the outer class.
		void checkStructure(Set<Node<E>> visitedNodes) {
			if (this == getEmptyLeaf()) return;
			if (!visitedNodes.add(this))
				throw new AssertionError("AVL tree structure violated: Not a tree");
			left .checkStructure(visitedNodes);
			right.checkStructure(visitedNodes);
			if (height != Math.max(left.height, right.height) + 1)
				throw new AssertionError("AVL tree structure violated: Incorrect cached height");
			if (size != left.size + right.size + 1)
				throw new AssertionError("AVL tree structure violated: Incorrect cached size");
			if (Math.abs(getBalance()) > 1)
				throw new AssertionError("AVL tree structure violated: Height imbalance");
		}
		
		//为子类保持子类性质添加的一些方法
		
		protected Node<E> getEmptyLeaf(){
			//noinspection unchecked
			return (Node<E>) EMPTY_LEAF;
		}
		
		protected Node<E> allocate(E obj){
			return new Node<>(obj);
		}
		
		public void testBuild(StringBuilder builder, int depth){
			builder.append("    ".repeat(depth));
			builder.append("{\n");
			if(left != left.getEmptyLeaf()) {
				builder.append("left");
				builder.append(":\n");
				left.testBuild(builder, depth + 1);
			}
			if(right != right.getEmptyLeaf()) {
				builder.append("right");
				builder.append(":\n");
				right.testBuild(builder, depth + 1);
			}
			builder.append("    ".repeat(depth));
			builder.append("}\n");
		}
		
	}
	
	
	
	/*---- Helper class: Binary search tree iterator ----*/
	
	// Note: Not fail-fast on concurrent modification.
	private final class Iter implements Iterator<E> {
		
		/*-- Fields --*/
		
		private int index;
		private final Stack<Node<E>> stack;
		
		
		/*-- Constructors --*/
		
		public Iter() {
			index = 0;
			stack = new Stack<>();
			initPath();
		}
		
		
		/*-- Methods --*/
		
		private void initPath() {
			stack.clear();
			int idx = index;
			for (Node<E> node = root; node != node.getEmptyLeaf(); ) {
				assert 0 <= idx && idx <= node.size;
				if (idx <= node.left.size) {
					stack.push(node);
					if (idx < node.left.size)
						node = node.left;
					else
						break;
				} else {
					idx -= node.left.size + 1;
					node = node.right;
				}
			}
		}
		
		
		public boolean hasNext() {
			assert stack.isEmpty() == (index == root.size);
			return !stack.isEmpty();
		}
		
		
		public E next() {
			if (!hasNext())
				throw new NoSuchElementException();
			Node<E> node = stack.pop();
			E result = node.value;
			node = node.right;
			while (node != node.getEmptyLeaf()) {
				stack.push(node);
				node = node.left;
			}
			index++;
			return result;
		}
		
		public void remove() {
			index--;
			AvlTreeList.this.remove(index);
			initPath();
		}
	}
}
