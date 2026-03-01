package lpctools.util.operatorUtils;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lpctools.lpcfymasaapi.screen.SelectionScreen;
import lpctools.util.CachedSupplier;
import net.minecraft.text.Text;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class DefaultedSignInfo<T extends Operators.SignBase> implements Operators.ISignInfo<T> {
	private static final Function<Object, Function<Operators.SignBase, String>>
		defaultDisplayStringGenerator = list->Operators.SignBase::idString;
	private static <T extends Operators.SignBase>
	void defaultChooseTreeGeneratorHelperFunc(ImmutableMap.Builder<String, String> builder, DefaultedSignInfo<T> info){
		for(var v : info.sortedList.get()) builder.put(info.getDisplayString(v), info.getIdString(v));
	}
	private static final Function<DefaultedSignInfo<? extends Operators.SignBase>, ImmutableMap<?, ?>>
		defaultChooseTreeGenerator = info->{
		var builder = ImmutableMap.<String, String>builder();
		defaultChooseTreeGeneratorHelperFunc(builder, info);
		return builder.build();
	};
	//配置参数
	private @NotNull Function<? super List<? extends T>, ? extends Function<? super T, String>> displayStringGenerator = defaultDisplayStringGenerator;
	private boolean fuzzy = true;
	private @Nullable Comparator<? super T> sortComparator = null;
	private @Nullable Function<? super DefaultedSignInfo<? extends T>, ImmutableMap<?, ?>> chooseTreeGenerator = defaultChooseTreeGenerator;
	//数据和缓存
	private final ArrayList<T> signs = new ArrayList<>();
	private final Queue<Supplier<? extends T>> delayedRegistrations = new ArrayDeque<>();
	private final CachedSupplier<ArrayList<String>> ids = new CachedSupplier<>(this::generateIds);
	private final CachedSupplier<Object2IntOpenHashMap<T>> sign2indexMap = new CachedSupplier<>(this::generateSign2indexMap);
	private final CachedSupplier<Object2IntOpenHashMap<String>> id2indexMap = new CachedSupplier<>(this::generateId2IndexMap);
	private final CachedSupplier<Function<? super T, String>> displayString = new CachedSupplier<>(()->displayStringGenerator.apply(signs));
	private final CachedSupplier<ArrayList<T>> sortedList = new CachedSupplier<>(this::sortSigns);
	
	public DefaultedSignInfo(){}
	
	/** 设置展示字符串生成器，默认是生成与id相同的展示字符串 */
	@SuppressWarnings("UnusedReturnValue") @Contract("_->this") public DefaultedSignInfo<T> displayGen(@Nullable Function<? super List<? extends T>, ? extends Function<? super T, String>> generator)
	{ displayStringGenerator = generator == null ? defaultDisplayStringGenerator : generator; return this; }
	/** 控制get()时的一个行为，未找到完全匹配的key时默认会找与key最接近的key对应的sign，fuzzy关闭后则是直接输出null */
	@SuppressWarnings("unused") @Contract("_->this") public DefaultedSignInfo<T> fuzzyMode(boolean b) { fuzzy = b; return this; }
	/** 设置显示时的顺序比较实例，用于设置选择界面的显示顺序或者cycle mode的cycle顺序。比较时允许通过getDisplayString获取显示字符串 */
	@Contract("_->this") public DefaultedSignInfo<T> comparator(Comparator<? super T> comparator) { sortComparator = comparator; return this; }
	/** 设置显示时的顺序比较实例，的另一个版本，传入参数为当前实例 */
	@SuppressWarnings("unused") @Contract("_->this") public DefaultedSignInfo<T> comparator(Function<DefaultedSignInfo<T>, Comparator<? super T>> comparator) { return comparator(comparator.apply(this)); }
	/** 批量设置signs，同时会清空原有的signs。可以iterate到null，返回null的遍历项会被忽略 */
	@SuppressWarnings("unused") @Contract("_->this") public DefaultedSignInfo<T> setSigns(Iterable<? extends T> signs) { _setSigns(signs); return this; }
	/** setSigns的延迟版 */
	@SuppressWarnings("unused") @Contract("_->this") public DefaultedSignInfo<T> delayedSetSigns(Iterable<? extends Supplier<? extends T>> signs) { _delayedSetSigns(signs); return this; }
	/** 设置chooseTree的生成函数，null表示使用cycle，即不开启选择界面进行选择而是根据按下的是左右键轮换到下一个或上一个。 */
	@SuppressWarnings("unused") @Contract("_->this") public DefaultedSignInfo<T> chooseTree(@Nullable Function<? super DefaultedSignInfo<? extends T>, ImmutableMap<?, ?>> generator)
	{ chooseTreeGenerator = generator; return this; }
	/** 将chooseTree的生成函数设回默认，即按顺序从displayString到idString的一一映射 */
	@SuppressWarnings("unused") @Contract("->this") public DefaultedSignInfo<T> chooseTree() { chooseTreeGenerator = defaultChooseTreeGenerator; return this; }
	
	@SuppressWarnings("unused") public Iterable<T> getSigns() { return sortedList.get(); }
	public int size() { return sortedList.get().size(); }
	public String getIdString(T sign) {
		var sign2indexMap = this.sign2indexMap.get();
		if(!sign2indexMap.containsKey(sign)) return null;
		else return ids.get().get(sign2indexMap.getInt(sign));
	}
	@Override public @Nullable T get(String idString) {
		int index;
		var id2indexMap = this.id2indexMap.get();
		if(id2indexMap.containsKey(idString))
			index = id2indexMap.getInt(idString);
		else if(fuzzy && !sortedList.get().isEmpty()){
			var ids = this.ids.get();
			int i = 0, min = StringUtils.getLevenshteinDistance(ids.getFirst(), idString);
			for(int j = 1; j < sortedList.get().size(); ++j){
				int d = StringUtils.getLevenshteinDistance(ids.get(j), idString);
				if(d < min){
					min = d;
					i = j;
				}
			}
			index = i;
		}
		else return null;
		return sortedList.get().get(index);
	}
	
	@Override public void mouseButtonClicked(T curr, boolean isLeftButton, Consumer<T> callback){
		if(chooseTreeGenerator == null){
			// 我觉得你不应该在cycle模式下设置过多的可选项，所以这里就不进行优化了
			var sortedList = this.sortedList.get();
			int i;
			for(i = 0; i < sortedList.size(); ++i)
				if(sortedList.get(i).equals(curr))
					break;
			if(isLeftButton){
				// 若curr不在signs中返回第一个，似乎也挺合适
				if(i + 1 >= sortedList.size()) callback.accept(sortedList.getFirst());
				else callback.accept(sortedList.get(i + 1));
			}
			else{
				if(i - 1 < 0) callback.accept(sortedList.getLast());
				// 若curr不在signs中会返回最后一个
				else callback.accept(sortedList.get(i - 1));
			}
		}
		else {
			SelectionScreen.openSelectionScreen(SelectionScreen.OptionNode.ofOptions(
				sortedList.get(), sign->Text.of(getDisplayString(sign)), Text.of("")
			), callback);
			//ChooseScreen.openSelectionScreen("Choose an option", true, true, chooseTree.get())
		}
	}
	
	@Override public String getDisplayString(T sign) { return displayString.get().apply(sign); }
	
	@Override public T getDefault() { return sortedList.get().getFirst(); }
	
	@SuppressWarnings("unused") public void addSign(T sign){
		signs.add(sign);
		invalidateCache();
	}
	
	public void addSign(Supplier<T> delayedRegistration){
		delayedRegistrations.add(delayedRegistration);
		invalidateCache();
	}
	
	private void invalidateCache(){
		sign2indexMap.invalidate();
		id2indexMap.invalidate();
		displayString.invalidate();
		sortedList.invalidate();
	}
	
	private ArrayList<String> generateIds(){
		var res = new ArrayList<String>();
		sortedList.get().forEach(sign->res.add(sign.idString()));
		return res;
	}
	
	private Object2IntOpenHashMap<T> generateSign2indexMap(){
		var res = new Object2IntOpenHashMap<T>();
		sortedList.get().forEach(v->{
			if(res.containsKey(v)) throw new IllegalArgumentException("Duplicate sign:" + v.idString());
			res.put(v, res.size());
		});
		return res;
	}
	
	private Object2IntOpenHashMap<String> generateId2IndexMap(){
		var res = new Object2IntOpenHashMap<String>();
		sortedList.get().forEach(v->{
			var id = v.idString();
			if(res.containsKey(id)) throw new IllegalArgumentException("Duplicate sign id:" + id);
			res.put(id, res.size());
		});
		return res;
	}
	
	private ArrayList<T> sortSigns(){
		while(!delayedRegistrations.isEmpty()) {
			var next = delayedRegistrations.poll().get();
			if(next != null) signs.add(next);
		}
		if(sortComparator != null) signs.sort(sortComparator);
		return signs;
	}
	
	private void _setSigns(Iterable<? extends T> signs){
		this.signs.clear();
		delayedRegistrations.clear();
		invalidateCache();
		signs.forEach(s->{ if(s != null) this.signs.add(s); });
	}
	
	private void _delayedSetSigns(Iterable<? extends Supplier<? extends T>> delayedRegistrations){
		signs.clear();
		this.delayedRegistrations.clear();
		invalidateCache();
		delayedRegistrations.forEach(s->{ if(s != null) this.delayedRegistrations.add(s); });
	}
}
