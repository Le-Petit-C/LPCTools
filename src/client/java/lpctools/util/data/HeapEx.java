package lpctools.util.data;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

public class HeapEx<T> extends Heap<T>{
	protected final ArrayList<@NotNull NodeReference> references = new ArrayList<>();
	protected void setReference(int i, NodeReference reference){
		reference.index = i;
		references.set(i, reference);
	}
	protected NodeReference removeLastReference(){return references.removeLast();}
	public class NodeReference{
		NodeReference(int index){this.index = index;}
		int index;
		private void assertValid(){if(!isValid()) throw new IllegalStateException("reference invalid");}
		public T getValue(){
			assertValid();
			return get(index);
		}
		public void setValue(T v){
			assertValid();
			set(index, v);
		}
		public int getIndex(){
			assertValid();
			return index;
		}
		public T remove(){
			assertValid();
			int i = index;
			index = -1;
			return HeapEx.this.remove(i);
		}
		public HeapEx<T> getParent(){return HeapEx.this;}
		public boolean isValid(){return index >= 0;}
	}
	private NodeReference pushReference(){
		var res = new NodeReference(references.size());
		references.add(res);
		return res;
	}
	private void initReferences(){for(T v : this) pushReference();}
	public HeapEx(){super();}
	public HeapEx(@NotNull Heap<T> heap){
		super(heap);
		initReferences();
	}
	public HeapEx(@NotNull Heap<T> heap, Collection<NodeReference> references){
		this(heap);
		references.clear();
		references.addAll(this.references);
	}
	public HeapEx(@NotNull Comparator<? super T> comparator){super(comparator);}
	public HeapEx(@NotNull Comparator<? super T> comparator, @NotNull Collection<? extends T> collection){
		super(comparator, collection);
		initReferences();
	}
	public HeapEx(@NotNull Comparator<? super T> comparator, @NotNull Collection<? extends T> collection, Collection<NodeReference> references){
		super(comparator);
		addAllWithReference(collection, references);
	}
	public NodeReference getReference(int index){return references.get(index);}
	public NodeReference getFirstReference(){return references.getFirst();}
	@Override public boolean add(T t) {
		pushReference();
		return super.add(t);
	}
	public boolean computeAdd(@NotNull Function<NodeReference, T> comp){
		return super.add(comp.apply(pushReference()));
	}
	public NodeReference addWithReference(T t) {
		var res = pushReference();
		super.add(t);
		return res;
	}
	@Override public boolean addAll(Collection<? extends T> c) {
		for(int i = 0; i < c.size(); ++i) pushReference();
		return super.addAll(c);
	}
	public void addAllWithReference(Collection<? extends T> c, Collection<NodeReference> res) {
		res.clear();
		for(int i = 0; i < c.size(); ++i) res.add(pushReference());
		super.addAll(c);
	}
	@Override public T remove(int index) {
		T res = get(index);
		var ref = getReference(index);
		if(size() != index + 1){
			setUnsafe(index, super.removeLast());
			getReference(index).index = -1;
			setReference(index, removeLastReference());
			if(!siftUp(index))
				siftDown(index);
		}
		else {
			super.removeLast();
			removeLastReference().index = -1;
		}
		return res;
	}
	
	@Override protected boolean siftDown(int index) {
		T curr = get(index);
		var ref = references.get(index);
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
			setReference(i, getReference(smallest));
			i = smallest;
		}
		
		if (i == index) return false;
		setUnsafe(i, curr);
		setReference(i, ref);
		return true;
	}
	protected boolean siftUp(int index){
		T curr = get(index);
		var ref = references.get(index);
		int i = index;
		while(i > 0){
			int nextIndex = (i - 1) >>> 1;
			T next = get(nextIndex);
			if(comparator.compare(curr, next) >= 0) break;
			setUnsafe(i, next);
			setReference(i, getReference(nextIndex));
			i = nextIndex;
		}
		if(i == index) return false;
		setUnsafe(i, curr);
		setReference(i, ref);
		return true;
	}
}
