package lpctools.script.suppliers;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lpctools.util.operatorUtils.DefaultedSignInfo;
import lpctools.util.operatorUtils.Operators.SignBase;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;
import org.jetbrains.annotations.Nullable;

import java.util.*;

// 用于获取BlockState的某个Property值的函数式接口集合
public class BlockPropertyOperators {
	public interface GenericPropertyOperator<T extends Property<?>> extends SignBase{
		class GenericPropertyOperators extends PropertyOperators<GenericPropertyOperator<?>, Property<?>> {
			public GenericPropertyOperators() {
				super((idString, property) -> new GenericPropertyOperator<>(){
					@Override public String idString() {return idString;}
					@Override public Property<?> getProperty() {return property;}
				});
			}
		}
		GenericPropertyOperators propertyGetters = new GenericPropertyOperators();
		T getProperty();
	}
	public interface BooleanPropertyGetter extends GenericPropertyOperator<BooleanProperty>{
		PropertyOperators<BooleanPropertyGetter, BooleanProperty> propertyGetters = new PropertyOperators<>(
			(idString, property) -> new BooleanPropertyGetter() {
				@Override public String idString() {return idString;}
				@Override public BooleanProperty getProperty() {return property;}
			});
	}
	public interface IntegerPropertyOperator extends GenericPropertyOperator<IntProperty> {
		PropertyOperators<IntegerPropertyOperator, IntProperty> propertyGetters = new PropertyOperators<>(
			(idString, property) -> new IntegerPropertyOperator() {
				@Override public String idString() {return idString;}
				@Override public IntProperty getProperty() {return property;}
			});
	}
	public interface EnumPropertyOperator extends GenericPropertyOperator<EnumProperty<?>> {
		class EnumPropertyGetters extends PropertyOperators<EnumPropertyOperator, EnumProperty<?>> {
			public EnumPropertyGetters() {
				super((idString, property) -> new EnumPropertyOperator(){
					@Override public String idString() {return idString;}
					@Override public EnumProperty<?> getProperty() {return property;}
				});
			}
		}
		PropertyOperators<EnumPropertyOperator, EnumProperty<?>> propertyGetters = new EnumPropertyGetters();
	}
	
	public interface ISignGenerator<SignType extends GenericPropertyOperator<?>, PropertyType extends Property<?>> {
		SignType generate(String idString, PropertyType property);
	}
	
	public static class PropertyOperators<OperatorType extends GenericPropertyOperator<?>, PropertyType extends Property<?>> extends DefaultedSignInfo<OperatorType> {
		public final ISignGenerator<OperatorType, PropertyType> signGenerator;
		private @Nullable ArrayList<PropertyType> delayedRegistrations;
		// 构造函数，传入一个根据displayString和Property生成对应Sign的函数
		public PropertyOperators(ISignGenerator<OperatorType, PropertyType> signGenerator) {
			this.signGenerator = signGenerator;
			comparator(Comparator.comparing(SignBase::idString));
			displayGen(list->{
				HashMap<String, List<OperatorType>> tempMap = new HashMap<>();
				list.forEach(prop->tempMap.computeIfAbsent(prop.getProperty().getName(), k->new ArrayList<>()).add(prop));
				HashMap<OperatorType, String> displayStrings = new HashMap<>();
				tempMap.forEach((cleanName, propList)->{
					if(propList.size() > 1){
						Object2IntOpenHashMap<Class<?>> occurrenceMap = new Object2IntOpenHashMap<>();
						// 计算每种类型的出现次数
						propList.forEach(prop->occurrenceMap.put(prop.getProperty().getType(), occurrenceMap.getOrDefault(prop.getProperty().getType(), 0) + 1));
						// 如果某种类型只出现了一次，就用简短的描述，否则用详细描述
						propList.forEach(prop->{
							String displayString;
							if(occurrenceMap.getInt(prop.getProperty().getType()) > 1) displayString = prop.idString();
							else displayString = buildDetailedString(prop.getProperty(), false);
							displayStrings.put(prop, displayString);
						});
					}
					else displayStrings.put(propList.getFirst(), cleanName);
				});
				return displayStrings::get;
			});
		}
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
		
		public void registerProperty(PropertyType property){
			addSign(()->signGenerator.generate(buildDetailedString(property, true), property));
		}
	}
}
