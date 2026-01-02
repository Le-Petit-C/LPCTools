package lpctools.script.suppliers;

import com.google.common.collect.ImmutableMap;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lpctools.lpcfymasaapi.screen.ChooseScreen;
import lpctools.util.Operators.ISignInfo;
import lpctools.util.Operators.SignBase;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

import static lpctools.util.DataUtils.*;

// 用于获取BlockState的某个Property值的函数式接口集合
public class BlockPropertyOperators {
	public interface GenericPropertyOperator<T extends Property<?>> extends SignBase{
		class GenericPropertyOperators extends PropertyOperators<GenericPropertyOperator<?>, Property<?>> {
			public GenericPropertyOperators() {
				super((idString, displayString, property) -> new GenericPropertyOperator<>(){
					@Override public String displayString() {return displayString;}
					@Override public String idString() {return idString;}
					@Override public Property<?> getProperty() {return property;}
				}, null);
			}
		}
		GenericPropertyOperators propertyGetters = new GenericPropertyOperators();
		T getProperty();
	}
	public interface BooleanPropertyGetter extends GenericPropertyOperator<BooleanProperty>{
		PropertyOperators<BooleanPropertyGetter, BooleanProperty> propertyGetters = new PropertyOperators<>(
			(idString, displayString, property) -> new BooleanPropertyGetter() {
				@Override public String displayString() {return displayString;}
				
				@Override public String idString() {return idString;}
				
				@Override public BooleanProperty getProperty() {return property;}
			}, GenericPropertyOperator.propertyGetters);
	}
	public interface IntegerPropertyOperator extends GenericPropertyOperator<IntProperty> {
		PropertyOperators<IntegerPropertyOperator, IntProperty> propertyGetters = new PropertyOperators<>(
			(idString, displayString, property) -> new IntegerPropertyOperator() {
				@Override public String displayString() {return displayString;}
				
				@Override public String idString() {return idString;}
				
				@Override public IntProperty getProperty() {return property;}
			}, GenericPropertyOperator.propertyGetters);
	}
	public interface EnumPropertyOperator extends GenericPropertyOperator<EnumProperty<?>> {
		class EnumPropertyGetters extends PropertyOperators<EnumPropertyOperator, EnumProperty<?>> {
			public EnumPropertyGetters() {
				super((idString, displayString, property) -> new EnumPropertyOperator(){
					@Override public String displayString() {return displayString;}
					@Override public String idString() {return idString;}
					@Override public EnumProperty<?> getProperty() {return property;}
				}, GenericPropertyOperator.propertyGetters);
			}
		}
		PropertyOperators<EnumPropertyOperator, EnumProperty<?>> propertyGetters = new EnumPropertyGetters();
	}
	
	public interface ISignGenerator<SignType extends GenericPropertyOperator<?>, PropertyType extends Property<?>> {
		SignType generate(String idString, String displayString, PropertyType property);
	}
	
	
	public static class PropertyOperators<OperatorType extends GenericPropertyOperator<?>, PropertyType extends Property<?>> implements ISignInfo<OperatorType> {
		public final ISignGenerator<OperatorType, PropertyType> signGenerator;
		public final @Nullable PropertyOperators<? super OperatorType, ? super PropertyType> parent;
		private @Nullable ArrayList<PropertyType> delayedRegistrations;
		private final ArrayList<PropertyOperators<? extends OperatorType, ? extends PropertyType>> childOperators = new ArrayList<>();
		private boolean needUpdateFromChildren = false;
		private final HashSet<PropertyType> properties = new HashSet<>();
		private @Nullable ImmutableMap<String, PropertyData<OperatorType, PropertyType>> propertyIdMap;
		private @Nullable ImmutableMap<String, String> chooseTreeCache;
		// 构造函数，传入一个根据displayString和Property生成对应Sign的函数
		public PropertyOperators(ISignGenerator<OperatorType, PropertyType> signGenerator, @Nullable BlockPropertyOperators.PropertyOperators<? super OperatorType, ? super PropertyType> parent) {
			this.signGenerator = signGenerator;
			this.parent = parent;
			if (parent != null) parent.childOperators.add(this);
		}
		private record PropertyData<T extends SignBase, U extends Property<?>>(String detailedIdString, String displayString, U property, T operator)
			implements ChooseScreen.OptionCallback<Consumer<T>>, Comparable<PropertyData<T, U>> {
			// 生成详细描述字符串
			// 不带哈希值了，因为哈希值或许会变化，程序无法正确保存和加载同一个属性
			// 如果真的有完全一样的属性，，，活累了，毁灭吧，丢一个Exception给用户算了
			private static String buildDetailedString(Property<?> property, boolean withValueDetails){
				StringBuilder builder = new StringBuilder();
				builder.append(property.getName());
				builder.append(" (Type=");
				builder.append(property.getType().getSimpleName());
				if(withValueDetails){
					builder.append(", ");
					if(property instanceof IntProperty intProp) {
						builder.append("min=").append(intProp.getValues().getFirst());
						builder.append(", ");
						builder.append("max=").append(intProp.getValues().getLast());
					}
					else builder.append("values=").append(property.getValues());
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
			@Override public void action(ButtonBase button, int mouseButton, Consumer<T> callback) {callback.accept(operator);}
		}
		private HashSet<PropertyType> getProperties(){
			if(delayedRegistrations != null){
				properties.addAll(delayedRegistrations);
				delayedRegistrations = null;
			}
			if(needUpdateFromChildren){
				childOperators.forEach(child->properties.addAll(child.getProperties()));
				needUpdateFromChildren = false;
			}
			return properties;
		}
		
		// 可能出现的问题：两个不同的类型具有相同的simpleName，从而导致displayString冲突
		// 暂时不考虑这种情况，不过或许可以设为TODO
		private @NotNull ImmutableMap<String, PropertyData<OperatorType, PropertyType>> getPropertyIdMap(){
			if(propertyIdMap != null) return propertyIdMap;
			TreeMap<String, List<PropertyType>> tempMap = new TreeMap<>();
			getProperties().forEach(prop->tempMap.computeIfAbsent(prop.getName(), k->new ArrayList<>()).add(prop));
			ImmutableMap.Builder<String, PropertyData<OperatorType, PropertyType>> optionCallbacksBuilder = ImmutableMap.builder();
			tempMap.forEach((cleanName, propList)->{
				if(propList.size() > 1){
					ArrayList<PropertyData<OperatorType, PropertyType>> dataList = new ArrayList<>();
					Object2IntOpenHashMap<Class<?>> occurrenceMap = new Object2IntOpenHashMap<>();
					// 计算每种类型的出现次数
					propList.forEach(prop->occurrenceMap.put(prop.getType(), occurrenceMap.getOrDefault(prop.getType(), 0) + 1));
					// 如果某种类型只出现了一次，就用简短的描述，否则用详细描述
					propList.forEach(prop->{
						String detailedIdString = PropertyData.buildDetailedString(prop, true);
						String displayString;
						if(occurrenceMap.getInt(prop.getType()) > 1) displayString = detailedIdString;
						else displayString = PropertyData.buildDetailedString(prop, false);
						OperatorType sign = signGenerator.generate(detailedIdString, displayString, prop);
						dataList.add(new PropertyData<>(detailedIdString, displayString, prop, sign));
					});
					dataList.sort(Comparator.naturalOrder());
					dataList.forEach(data->optionCallbacksBuilder.put(data.detailedIdString, data));
				}
				else {
					PropertyType prop = propList.getFirst();
					String detailedIdString = PropertyData.buildDetailedString(prop, true);
					OperatorType sign = signGenerator.generate(detailedIdString, prop.getName(), prop);
					PropertyData<OperatorType, PropertyType> data = new PropertyData<>(detailedIdString, prop.getName(), prop, sign);
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
		
		@Override public void mouseButtonClicked(OperatorType curr, boolean isLeftButton, Consumer<OperatorType> callback) {
			ChooseScreen.openChooseScreen("", true, true, getPropertyIdMap(), getChooseTreeCache(), callback);
		}
		
		// 根据idString获取对应的SignType
		// 方块状态属性也许容易跟着Minecraft版本变化而变化？所以这里做了模糊匹配
		@Override public @Nullable OperatorType get(String idString) {
			return findMostSimilar(getPropertyIdMap(), idString).operator;
		}
		
		private void invalidateCache(){
			PropertyOperators<?, ?> operators = this;
			while(operators != null && !operators.needUpdateFromChildren){
				operators.needUpdateFromChildren = true;
				operators.propertyIdMap = null;
				operators.chooseTreeCache = null;
				operators = operators.parent;
			}
		}
		
		public void registerProperty(PropertyType property){
			if(delayedRegistrations == null)
				delayedRegistrations = new ArrayList<>();
			delayedRegistrations.add(property);
			invalidateCache();
		}
		
		public OperatorType getFirstProperty(){
			return getPropertyIdMap().values().iterator().next().operator;
		}
	}
}
