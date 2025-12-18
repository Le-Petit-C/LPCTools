package lpctools.script.suppliers;

import com.google.common.collect.ImmutableMap;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import lpctools.lpcfymasaapi.screen.ChooseScreen;
import lpctools.util.Functions.ISignInfo;
import lpctools.util.Functions.SignBase;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

import static lpctools.util.DataUtils.*;

// 用于获取BlockState的某个Property值的函数式接口集合
public class BlockStatePropertyGettersAsFunction {
	public interface DoHasProperty extends SignBase{
		PropertyGetters<DoHasProperty, Property<?>> propertyGetters = new PropertyGetters<>(
			(idString, displayString, property) -> new DoHasProperty(){
				@Override public String displayString() {return displayString;}
				@Override public String idString() {return idString;}
				@Override public boolean hasProperty(BlockState state) {return state.contains(property);}
		});
		boolean hasProperty(BlockState state);
	}
	public interface BooleanPropertyGetter extends SignBase{
		PropertyGetters<BooleanPropertyGetter, Property<Boolean>> propertyGetters = new PropertyGetters<>(
			(idString, displayString, property) -> new BooleanPropertyGetter(){
				@Override public String displayString() {return displayString;}
				@Override public String idString() {return idString;}
				@Override public boolean getBoolean(BlockState state) {return state.get(property);}
		});
		boolean getBoolean(BlockState state);
	}
	public interface IntegerPropertyGetter extends SignBase{
		PropertyGetters<IntegerPropertyGetter, Property<Integer>> propertyGetters = new PropertyGetters<>(
			(idString, displayString, property) -> new IntegerPropertyGetter(){
				@Override public String displayString() {return displayString;}
				@Override public String idString() {return idString;}
				@Override public int getInteger(BlockState state) {return state.get(property);}
		});
		int getInteger(BlockState state);
	}
	
	public interface ISignGenerator<SignType extends SignBase, PropertyType extends Property<?>> {
		SignType generate(String idString, String displayString, PropertyType property);
	}
	
	public static class PropertyGetters<SignType extends SignBase, PropertyType extends Property<?>> implements ISignInfo<SignType> {
		public final ISignGenerator<SignType, PropertyType> signGenerator;
		private @Nullable ArrayList<PropertyType> delayedRegistrations;
		private final HashSet<PropertyType> properties = new HashSet<>();
		private @Nullable ImmutableMap<String, PropertyData<SignType, PropertyType>> propertyIdMap;
		private @Nullable ImmutableMap<String, String> chooseTreeCache;
		// 构造函数，传入一个根据displayString和Property生成对应Sign的函数
		public PropertyGetters(ISignGenerator<SignType, PropertyType> signGenerator) {
			this.signGenerator = signGenerator;
		}
		private record PropertyData<T extends SignBase, U extends Property<?>>(String detailedIdString, String displayString, U property, T getter)
			implements ChooseScreen.OptionCallback<Consumer<T>>, Comparable<PropertyData<T, U>> {
			// 生成详细描述字符串
			// 不带哈希值了，因为哈希值或许会变化，程序无法正确保存和加载同一个属性
			// 如果真的有完全一样的属性，，，活累了，毁灭吧，丢一个Exception给用户算了
			private static String buildDetailedString(Property<?> property){
				StringBuilder builder = new StringBuilder();
				builder.append(property.getName());
				builder.append(" (Type=");
				builder.append(property.getType().getSimpleName());
				if(property instanceof IntProperty intProp) {
					builder.append(", ");
					builder.append("min=").append(intProp.getValues().getFirst());
					builder.append(", ");
					builder.append("max=").append(intProp.getValues().getLast());
				}
				else {
					builder.append(", ");
					builder.append("values=").append(property.getValues());
				}
				builder.append(")");
				return builder.toString();
			}
			@Override public int compareTo(@NotNull PropertyData<T, U> o) {
				if(this == o) return 0;
				int comp = this.detailedIdString.compareTo(o.detailedIdString);
				if(comp != 0) return comp;
				else throw new IllegalArgumentException("Duplicate property detected: " + this.detailedIdString);
			}
			@Override public void action(ButtonBase button, int mouseButton, Consumer<T> callback) {callback.accept(getter);}
		}
		private HashSet<PropertyType> getProperties(){
			if(delayedRegistrations != null){
				properties.addAll(delayedRegistrations);
				delayedRegistrations = null;
			}
			return properties;
		}
		private @NotNull ImmutableMap<String, PropertyData<SignType, PropertyType>> getPropertyIdMap(){
			if(propertyIdMap != null) return propertyIdMap;
			TreeMap<String, List<PropertyType>> tempMap = new TreeMap<>();
			getProperties().forEach(prop->tempMap.computeIfAbsent(prop.getName(), k->new ArrayList<>()).add(prop));
			ImmutableMap.Builder<String, PropertyData<SignType, PropertyType>> optionCallbacksBuilder = ImmutableMap.builder();
			tempMap.forEach((cleanName, propList)->{
				if(propList.size() > 1){
					ArrayList<PropertyData<SignType, PropertyType>> dataList = new ArrayList<>();
					propList.forEach(prop->{
						String detailedIdString = PropertyData.buildDetailedString(prop);
						SignType sign = signGenerator.generate(detailedIdString, detailedIdString, prop);
						dataList.add(new PropertyData<>(detailedIdString, detailedIdString, prop, sign));
					});
					dataList.sort(Comparator.naturalOrder());
					dataList.forEach(data->optionCallbacksBuilder.put(data.detailedIdString, data));
				}
				else {
					PropertyType prop = propList.getFirst();
					String detailedIdString = PropertyData.buildDetailedString(prop);
					SignType sign = signGenerator.generate(detailedIdString, prop.getName(), prop);
					PropertyData<SignType, PropertyType> data = new PropertyData<>(detailedIdString, prop.getName(), prop, sign);
					optionCallbacksBuilder.put(data.detailedIdString, data);
				}
			});
			return propertyIdMap = optionCallbacksBuilder.build();
		}
		
		private @NotNull ImmutableMap<String, String> getChooseTreeCache(){
			if(chooseTreeCache != null) return chooseTreeCache;
			ImmutableMap.Builder<String, String> chooseTreeBuilder = ImmutableMap.builder();
			getPropertyIdMap().forEach((key, data)->chooseTreeBuilder.put(data.displayString, key));
			return chooseTreeCache = chooseTreeBuilder.build();
		}
		
		@Override public void mouseButtonClicked(SignType curr, boolean isLeftButton, Consumer<SignType> callback) {
			ChooseScreen.openChooseScreen("", true, true, getPropertyIdMap(), getChooseTreeCache(), callback);
		}
		
		// 根据idString获取对应的SignType
		// 方块状态属性也许容易跟着Minecraft版本变化而变化？所以这里做了模糊匹配
		@Override public @Nullable SignType get(String idString) {
			return findMostSimilar(getPropertyIdMap(), idString).getter;
		}
		
		public void registerProperty(PropertyType property){
			if(delayedRegistrations == null)
				delayedRegistrations = new ArrayList<>();
			delayedRegistrations.add(property);
			// invalidate cache
			propertyIdMap = null;
			chooseTreeCache = null;
		}
		
		public SignType getFirstProperty(){
			return getPropertyIdMap().values().iterator().next().getter;
		}
	}
}
