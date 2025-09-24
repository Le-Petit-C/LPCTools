package lpctools.lpcfymasaapi.configButtons.uniqueConfigs;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lpctools.lpcfymasaapi.configButtons.UpdateTodo;
import lpctools.lpcfymasaapi.interfaces.ILPCConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import lpctools.lpcfymasaapi.screen.ChooseScreen;
import net.minecraft.text.Text;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

public class ChooseConfig<T extends ILPCConfig> extends LPCUniqueConfigBase implements ILPCConfigReadable, AutoCloseable {
	private @NotNull T value;
	private @NotNull String valueKey;
	public <U> ChooseConfig(ILPCConfigReadable parent, String nameKey, ImmutableMap<String, ? extends TriFunction<? super ChooseConfig<T>, String, U, ? extends T>> allocatorMap,
							Map<?, ?> optionTree, ILPCValueChangeCallback callback, U userData) {
		super(parent, nameKey, callback);
		allocators = new WrappedAllocators<>(allocatorMap, userData);
		this.optionTree = optionTree;
		valueKey = allocatorMap.keySet().iterator().next();
		value = allocators.allocate(valueKey, this);
	}
	public ChooseConfig(ILPCConfigReadable parent, String nameKey, ImmutableMap<String, ? extends BiFunction<? super ChooseConfig<T>, String, ? extends T>> allocatorMap,
						Map<?, ?> optionTree, ILPCValueChangeCallback callback) {
		this(parent, nameKey, convert(allocatorMap), optionTree, callback, null);
	}
	@SuppressWarnings("unused")
	public <U> ChooseConfig(ILPCConfigReadable parent, String nameKey, ImmutableMap<String, ? extends TriFunction<? super ChooseConfig<T>, String, U, ? extends T>> allocatorMap,
							ILPCValueChangeCallback callback, U userData) {
		this(parent, nameKey, allocatorMap, defaultOptionTree(allocatorMap.keySet()), callback, userData);
	}
	@SuppressWarnings("unused")
	public ChooseConfig(ILPCConfigReadable parent, String nameKey, ImmutableMap<String, ? extends BiFunction<? super ChooseConfig<T>, String, ? extends T>> allocatorMap,
						ILPCValueChangeCallback callback) {
		this(parent, nameKey, convert(allocatorMap), defaultOptionTree(allocatorMap.keySet()), callback, null);
	}
	public @NotNull T get(){return value;}
	public void openChoose(){allocators.chooseConfig(optionTree, this);}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		res.add(1, (button, mouseButton)->openChoose(),
			()->Text.translatable(chooseButtonKey).getString(), buttonGenericAllocator);
	}
	@Override public @Nullable JsonObject getAsJsonElement() {
		JsonObject object = new JsonObject();
		object.addProperty("key", valueKey);
		object.add("value", value.getAsJsonElement());
		return object;
	}
	@Override public UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement element) {
		if(element instanceof JsonObject object){
			UpdateTodo todo = new UpdateTodo();
			if(object.get("key") instanceof JsonPrimitive key){
				if(!valueKey.equals(key.getAsString())){
					String valueKey = key.getAsString();
					if(allocators.allocatorMap.containsKey(valueKey)){
						this.valueKey = valueKey;
						if(value instanceof AutoCloseable closeable) {
							try {
								closeable.close();
							} catch (Exception e) {
								throw new RuntimeException(e);
							}
						}
						value = allocators.allocate(valueKey, this);
						todo.valueChanged();
					}
					else return setValueFailed(element);
				}
				if(object.get("value") instanceof JsonElement element1)
					todo.combine(value.setValueFromJsonElementEx(element1));
			}
			return todo;
		}
		return setValueFailed(element);
	}
	
	private static <T extends ILPCConfig> ImmutableMap<String, TriFunction<? super ChooseConfig<T>, String, Object, T>>
	convert(ImmutableMap<String, ? extends BiFunction<? super ChooseConfig<T>, String, ? extends T>> allocatorMap){
		ImmutableMap.Builder<String, TriFunction<? super ChooseConfig<T>, String, Object, T>> res = ImmutableMap.builder();
		allocatorMap.forEach((k, f)->res.put(k, (config, key, obj)->f.apply(config, k)));
		return res.build();
	}
	private static ImmutableMap<String, String> defaultOptionTree(Iterable<String> keys){
		ImmutableMap.Builder<String, String> res = ImmutableMap.builder();
		keys.forEach(s->res.put(s, s));
		return res.build();
	}
	
	@Override public @NotNull Iterable<ILPCConfig> getConfigs() {return List.of(value);}
	int indent;
	@Override public void setAlignedIndent(int indent) {this.indent = indent;}
	@Override public int getAlignedIndent() {return indent;}
	
	@Override public void close() throws Exception {
		if(value instanceof AutoCloseable closeable)
			closeable.close();
	}
	
	private record WrappedAllocators<T extends ILPCConfig, U>(ImmutableMap<String, ? extends TriFunction<? super ChooseConfig<T>, String, U, ? extends T>> allocatorMap, U userData){
		public T allocate(String key, ChooseConfig<T> config){
			return Objects.requireNonNull(allocatorMap.get(key)).apply(config, key, userData);
		}
		public void chooseConfig(Map<?, ?> optionTree, ChooseConfig<T> config){
			ImmutableMap.Builder<String, ChooseScreen.OptionCallback<U>> options = ImmutableMap.builder();
			allocatorMap.forEach((key, func)->options.put(key, (button, mouseButton, userData)->{
				if(config.value instanceof AutoCloseable closeable) {
					try {
						closeable.close();
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
				config.value = func.apply(config, key, userData);
				config.valueKey = key;
				config.getPage().markNeedUpdate();
				config.onValueChanged();
			}));
			ChooseScreen.openChooseScreen(Text.translatable(chooseTitle).getString(), true, options.build(), optionTree, userData);
		}
	}
	private final WrappedAllocators<T, ?> allocators;
	private final Map<?, ?> optionTree;
	public static final String chooseButtonKey = "lpctools.configs.utils.chooseConfig.chooseConfigButton";
	public static final String chooseTitle = "lpctools.configs.utils.chooseConfig.chooseTitle";
}
