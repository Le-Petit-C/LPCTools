package lpctools.util.data;

import org.jetbrains.annotations.NotNull;

import java.util.*;

//根据comparator建立小根堆
public class Heap<T> extends ArrayList<T> {
	protected @NotNull Comparator<? super T> comparator;
	@SuppressWarnings("UnusedReturnValue")
	protected T setUnsafe(int i, T v){return super.set(i, v);}
	public Heap(){
		//assert T instanceof Comparable<T>;
		//noinspection unchecked
		comparator = (o1, o2) -> ((Comparable<? super T>) o1).compareTo(o2);
	}
	public Heap(@NotNull Heap<T> heap){
		super(heap);
		comparator = heap.comparator;
	}
	public Heap(@NotNull Comparator<? super T> comparator){this.comparator = comparator;}
	public Heap(@NotNull Comparator<? super T> comparator, @NotNull Collection<? extends T> collection){
		super(collection);
		this.comparator = comparator;
		rebuildHeap();
	}
	public void setComparator(@NotNull Comparator<? super T> comparator){
		this.comparator = comparator;
		rebuildHeap();
	}
	public @NotNull Comparator<? super T> getComparator(){return comparator;}
	protected boolean siftDown(int index) {
		T curr = get(index);
		int i = index;
		int heapSize = size();
		int half = heapSize >>> 1; // 第一个叶子节点的索引
		
		while (i < half) {
			int leftIndex = (i << 1) + 1;
			int rightIndex = leftIndex + 1;
			int smallest = leftIndex;
			
			// 检查右子节点是否存在并比较
			if (rightIndex < heapSize &&
				comparator.compare(get(rightIndex), get(leftIndex)) < 0) {
				smallest = rightIndex;
			}
			
			if (comparator.compare(get(smallest), curr) >= 0) break;
			
			setUnsafe(i, get(smallest));
			i = smallest;
		}
		
		if (i == index) return false;
		setUnsafe(i, curr);
		return true;
	}
	protected boolean siftUp(int index){
		T curr = get(index);
		int i = index;
		while(i > 0){
			int nextIndex = (i - 1) >>> 1;
			T next = get(nextIndex);
			if(comparator.compare(curr, next) >= 0) break;
			setUnsafe(i, next);
			i = nextIndex;
		}
		if(i == index) return false;
		setUnsafe(i, curr);
		return true;
	}
	public void rebuildHeap() {
		int i = size() >> 1;
		while(i > 0) siftDown(--i);
	}
	@Override public boolean add(T t) {
		super.add(t);
		siftUp(size() - 1);
		return true;
	}
	@Override public boolean addAll(Collection<? extends T> c) {
		final int m = c.size();
		if(m == 0) return false;
		final int oldSize = size();
		super.addAll(c);
		final int n = size();
		
		// 若原堆为空或新增元素很多，直接全堆化更稳妥高效
		// 效率差不多的情况下使用oldSize作为阈值省去oldSize <= 0的判断
		if (m >= oldSize) {
			rebuildHeap();
			return true;
		}
		
		int start = oldSize;
		int end = n - 1;
		
		// 向上移动到该层的父层，然后处理父层 [start, end]
		// start <= end的条件似乎不需要了
		while (start > 0) {
			// 上移到父层
			start = (start - 1) >> 1;
			end = (end - 1) >> 1;
			
			boolean anySwap = false;
			for (int i = end; i >= start; --i)
				anySwap |= siftDown(i);
			
			// 若本层没有任何交换，说明上层也无须变动（早退）
			if (!anySwap) break;
		}
		
		return true;
	}
	
	@Override public boolean addAll(int index, Collection<? extends T> c) {
		throw new UnsupportedOperationException("Heap does not support add by index");
	}
	@Override public T remove(int index) {
		T res = get(index);
		if(size() != index + 1){
			setUnsafe(index, super.removeLast());
			if(!siftUp(index)) siftDown(index);
		}
		else super.removeLast();
		return res;
	}
	@Override public T removeFirst() {
		return remove(0);
	}
	private static final int hashLimit = 8;
	@Override public boolean removeAll(Collection<?> c) {
		if(c.size() > hashLimit && !(c instanceof Set<?>))
			c = new HashSet<>(c);
		int i = size();
		boolean res = false;
		while (i > 0){
			if(c.contains(get(--i))){
				remove(i);
				res = true;
			}
		}
		return res;
	}
	@Override public boolean retainAll(Collection<?> c) {
		if(c.size() > hashLimit && !(c instanceof Set<?>))
			c = new HashSet<>(c);
		int i = size();
		boolean res = false;
		while (i > 0){
			if(!c.contains(get(--i))){
				remove(i);
				res = true;
			}
		}
		return res;
	}
	
	public T peek(){return getFirst();}
	public T remove(){return removeFirst();}
	public void insert(T v){add(v);}
	public <U extends List<T>> U popAll(U list) {
		list.clear();
		while (!isEmpty())
			list.add(removeFirst());
		return list;
	}
	public <U extends List<T>> U sortedList(U list) {
		return new Heap<>(this).popAll(list);
	}
	public ArrayList<T> sortedList(){
		return sortedList(new ArrayList<>());
	}
	
	@Override public void add(int index, T element) {
		throw new UnsupportedOperationException("Heap does not support add by index");
	}
	@Override public T set(int index, T element) {
		T res = super.set(index, element);
		if(!siftUp(index)) siftDown(index);
		return res;
	}
}
