package lpctools.script.suppliers;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lpctools.lpcfymasaapi.screen.ChooseScreen;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.suppliers.Array.NewArray;
import lpctools.script.suppliers.Block.BlockInWorld;
import lpctools.script.suppliers.Block.ConstantBlock;
import lpctools.script.suppliers.BlockPos.*;
import lpctools.script.suppliers.Boolean.*;
import lpctools.script.suppliers.Direction.ConstantDirection;
import lpctools.script.suppliers.Double.*;
import lpctools.script.suppliers.Entity.VehicleEntity;
import lpctools.script.suppliers.Entity.PlayerEntity.MainPlayerEntity;
import lpctools.script.suppliers.Integer.*;
import lpctools.script.suppliers.Item.ConstantItem;
import lpctools.script.suppliers.Item.StackItem;
import lpctools.script.suppliers.ItemStack.CurrentScreenSlotStack;
import lpctools.script.suppliers.ItemStack.EmptyStack;
import lpctools.script.suppliers.ItemStack.InventoryItemStack;
import lpctools.script.suppliers.ItemStack.SlotItemStack;
import lpctools.script.suppliers.Iterable.*;
import lpctools.script.suppliers.Random.FromArray;
import lpctools.script.suppliers.Random.FromVariable;
import lpctools.script.suppliers.Random.IRandomSupplierAllocator;
import lpctools.script.suppliers.Random.Null;
import lpctools.script.suppliers.String.ConstantString;
import lpctools.script.suppliers.String.ObjectToString;
import lpctools.script.suppliers.Type.ConstantType;
import lpctools.script.suppliers.Type.ObjectType;
import lpctools.script.suppliers.Vec3d.*;
import lpctools.script.suppliers.ControlFlowIssue.*;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static lpctools.lpcfymasaapi.LPCConfigUtils.warnFailedLoadingConfig;

public class ScriptSupplierLake {
	public static final ImmutableMap<String, ScriptRegistration<?, ?>> suppliers;
	public static final ImmutableMap<Class<? extends IScriptSupplier<?>>, ScriptRegistration<?, ?>> suppliersInverseMap;
	public static final ImmutableMap<Class<?>, ImmutableSet<ScriptRegistration<?, ?>>> suppliersFromClass;
	public static final ImmutableMap<Class<?>, ImmutableSet<Class<?>>> preciseSupplierTypeTree;
	public static final ImmutableMap<Class<?>, ScriptType> typeMap;
	public static final ImmutableMap<String, ScriptType> typeIdMap;
	
	private static final LinkedHashMap<String, ScriptRegistration<?, ?>> suppliersTemp = new LinkedHashMap<>();
	private static final LinkedHashMap<Class<? extends IScriptSupplier<?>>, ScriptRegistration<?, ?>> suppliersInverseMapTemp = new LinkedHashMap<>();
	private static final LinkedHashMap<Class<?>, LinkedHashSet<ScriptRegistration<?, ?>>> suppliersFromClassTemp = new LinkedHashMap<>();
	private static final LinkedHashMap<Class<?>, LinkedHashSet<Class<?>>> preciseSupplierTypeTreeTemp = new LinkedHashMap<>();
	private static final LinkedHashMap<Class<?>, ScriptType> typeMapTemp = new LinkedHashMap<>();
	private static final LinkedHashMap<String, ScriptType> typeIdMapTemp = new LinkedHashMap<>();
	
	public static <T> void chooseSupplier(Class<T> targetClass, IScriptWithSubScript parent, Consumer<IScriptSupplier<? extends T>> callback){
		Consumer<ScriptRegistration<?, ?>> consumer = reg->{
			var allocator = reg.tryAllocate(targetClass);
			if(allocator != null) callback.accept(allocator.allocate(parent));
		};
		ChooseScreen.openChooseScreen(getTypeName(targetClass).getString(), true, suppliers, buildChooseMap(targetClass, true), consumer);
	}
	
	public static String getSupplierId(IScriptSupplier<?> supplier){
		return Objects.requireNonNull(suppliersInverseMap.get(supplier.getClass())).key;
	}
	
	public static ScriptRegistration<?, ?> getSupplierRegistration(IScriptSupplier<?> supplier){return suppliersInverseMap.get(supplier.getClass());}
	public static ScriptRegistration<?, ?> getSupplierRegistration(String key){return suppliers.get(key);}
	
	
	public static Text getTypeName(Class<?> clazz){
		if(typeMap.containsKey(clazz)) return Objects.requireNonNull(typeMap.get(clazz)).name();
		else return Text.literal(clazz.getName());
	}
	
	public static JsonObject getJsonEntryFromSupplier(@NotNull IScriptSupplier<?> supplier){
		JsonObject res = new JsonObject();
		res.addProperty("id", getSupplierId(supplier));
		res.add("data", supplier.getAsJsonElement());
		return res;
	}
	
	public static @Nullable <T> IScriptSupplier<? extends T> loadSupplierFromJsonEntry(JsonElement element, Class<T> targetClass, IScriptWithSubScript parent){
		if(!(element instanceof JsonObject object)) return null;
		if(object.get("id") instanceof JsonPrimitive id){
			var reg = ScriptSupplierLake.getSupplierRegistration(id.getAsString());
			if(reg != null){
				var allocator = reg.tryAllocate(targetClass);
				if(allocator != null){
					var res = allocator.allocate(parent);
					res.setValueFromJsonElement(object.get("data"));
					return res;
				}
			}
		}
		return null;
	}
	
	public static <T> void loadSupplierOrWarn(JsonElement element, Class<T> targetClass, IScriptWithSubScript parent, Consumer<IScriptSupplier<? extends T>> setter, String warnKey){
		if(element == null) return;
		var res = ScriptSupplierLake.loadSupplierFromJsonEntry(element, targetClass, parent);
		if(res != null) setter.accept(res);
		else warnFailedLoadingConfig(warnKey, element);
	}
	
	private static LinkedHashMap<String, Object> buildChooseMap(Class<?> clazz, boolean withRandomExtra){
		LinkedHashMap<String, Object> res;
		var targetSet = suppliersFromClass.get(clazz);
		var supTypeSet = preciseSupplierTypeTree.get(clazz);
		//当clazz没有任何子类供应器时，直接返回Object的选择列表，也就是只选择random suppliers
		//clazz为Object.class时，targetSet和supTypeSet不应该为空，不会触发无限递归。但为了保险起见，还是加上判断
		if((targetSet == null || targetSet.isEmpty()) && (supTypeSet == null || supTypeSet.isEmpty()) && clazz != Object.class)
			res = buildChooseMap(Object.class, false);
		else{
			res = new LinkedHashMap<>();
			if(withRandomExtra && clazz != ControlFlowIssue.class && clazz != Object.class)
				res.put(getTypeName(Object.class).getString(), (Supplier<?>)()->buildChooseMap(Object.class, false));
			if(targetSet != null)
				for(var target : targetSet)
					res.put(target.displayName.getString(), target.key);
			if(clazz == Object.class && !withRandomExtra){
				//由于Object是所有类型的基类，不可能由其他类继承而来，那么来到这里就只能是选择random suppliers了
				//那么就不再选择Object的子类，直接返回
				return res;
			}
			if(supTypeSet != null){
				for(var target : supTypeSet){
					if(target == ControlFlowIssue.class) continue;
					res.put(getTypeName(target).getString(), (Supplier<?>)()->buildChooseMap(target, false));
				}
			}
		}
		return res;
	}
	
	static {
		preciseSupplierTypeTreeTemp.put(Object.class, new LinkedHashSet<>());
	}
	
	//注册类型
	static{
		registerType(Object.class, 			parent->new Null<>(parent, Object.class), 			"object");
		registerType(ControlFlowIssue.class,DoNothing::new, 									"controlFlowIssue");
		registerType(Boolean.class, 		ConstantBoolean::new, 								"boolean");
		registerType(Integer.class, 		ConstantInteger::new, 								"integer");
		registerType(Double.class, 			ConstantDouble::new, 								"double");
		registerType(String.class, 			ConstantString::new, 								"string");
		registerType(Object[].class, 		parent->new Null<>(parent, Object[].class), 		"array");
		registerType(ScriptType.class, 		ConstantType::new, 									"type");
		registerType(ObjectIterable.class, 	parent->new Null<>(parent, ObjectIterable.class), 	"iterable");
		registerType(BlockPos.class, 		ConstantBlockPos::new, 								"blockPos");
		registerType(Vec3d.class, 			ConstantVec3d::new, 								"vec3d");
		registerType(Direction.class, 		ConstantDirection::new, 							"vec3d");
		registerType(Block.class, 			ConstantBlock::new, 								"block");
		registerType(Item.class, 			ConstantItem::new, 									"item");
		registerType(ItemStack.class, 		EmptyStack::new, 									"itemStack");
		registerType(Entity.class, 			MainPlayerEntity::new, 								"entity");
		registerType(PlayerEntity.class, 	MainPlayerEntity::new, 								"playerEntity");
	}
	
	//注册suppliers
	static {
		//注册random suppliers，也就是无类型限制的Object类suppliers
		registerRandom("null", Null.class, Null::new);
		registerRandom("fromVariable", FromVariable.class, FromVariable::new);
		registerRandom("fromArray", FromArray.class, FromArray::new);
		//注册control flow issue suppliers，也就是执行操作
		registerPrecise("doNothing", ControlFlowIssue.class, DoNothing.class, DoNothing::new);
		registerPrecise("runMultiple", ControlFlowIssue.class, RunMultiple.class, RunMultiple::new);
		registerPrecise("runIfElse", ControlFlowIssue.class, RunIfElse.class, RunIfElse::new);
		registerPrecise("whileLoop",ControlFlowIssue.class, WhileLoop.class, WhileLoop::new);
		registerPrecise("doWhileLoop", ControlFlowIssue.class, DoWhileLoop.class, DoWhileLoop::new);
		registerPrecise("forLoop", ControlFlowIssue.class, ForLoop.class, ForLoop::new);
		registerPrecise("break", ControlFlowIssue.class, Break.class, Break::new);
		registerPrecise("continue", ControlFlowIssue.class, Continue.class, Continue::new);
		registerPrecise("return", ControlFlowIssue.class, Return.class, Return::new);
		registerPrecise("setVariable", ControlFlowIssue.class, SetVariable.class, SetVariable::new);
		registerPrecise("setArray", ControlFlowIssue.class, SetArray.class, SetArray::new);
		registerPrecise("iterateArray", ControlFlowIssue.class, IterateArray.class, IterateArray::new);
		registerPrecise("iterateIterable", ControlFlowIssue.class, IterateIterable.class, IterateIterable::new);
		registerPrecise("clientMessage", ControlFlowIssue.class, ClientMessage.class, ClientMessage::new);
		registerPrecise("doAttack", ControlFlowIssue.class, DoAttack.class, DoAttack::new);
		registerPrecise("doItemUse", ControlFlowIssue.class, DoItemUse.class, DoItemUse::new);
		registerPrecise("attackBlock", ControlFlowIssue.class, AttackBlock.class, AttackBlock::new);
		registerPrecise("interactBlock", ControlFlowIssue.class, InteractBlock.class, InteractBlock::new);
		registerPrecise("attackEntity", ControlFlowIssue.class, AttackEntity.class, AttackEntity::new);
		registerPrecise("interactEntity", ControlFlowIssue.class, InteractEntity.class, InteractEntity::new);
		registerPrecise("clickSlot", ControlFlowIssue.class, ClickSlot.class, ClickSlot::new);
		//注册boolean suppliers
		registerPrecise("constantBoolean", Boolean.class, ConstantBoolean.class, ConstantBoolean::new);
		registerPrecise("and", Boolean.class, And.class, And::new);
		registerPrecise("or", Boolean.class, Or.class, Or::new);
		registerPrecise("not", Boolean.class, Not.class, Not::new);
		registerPrecise("compareIntegers", Boolean.class, CompareIntegers.class, CompareIntegers::new);
		registerPrecise("compareDoubles", Boolean.class, CompareDoubles.class, CompareDoubles::new);
		registerPrecise("compareObjects", Boolean.class, CompareObjects.class, CompareObjects::new);
		registerPrecise("notNull", Boolean.class, lpctools.script.suppliers.Boolean.NotNull.class, lpctools.script.suppliers.Boolean.NotNull::new);
		registerPrecise("canBreakInstantly", Boolean.class, CanBreakInstantly.class, CanBreakInstantly::new);
		//注册integer suppliers
		registerPrecise("constantInteger", Integer.class, ConstantInteger.class, ConstantInteger::new);
		registerPrecise("calculateIntegers", Integer.class, CalculateIntegers.class, CalculateIntegers::new);
		registerPrecise("integerFunction", Integer.class, IntegerFunction.class, IntegerFunction::new);
		registerPrecise("integerBiFunction", Integer.class, IntegerBiFunction.class, IntegerBiFunction::new);
		registerPrecise("integerTriFunction", Integer.class, IntegerTriFunction.class, IntegerTriFunction::new);
		registerPrecise("integerFromDouble", Integer.class, IntegerFromDouble.class, IntegerFromDouble::new);
		registerPrecise("integerFromBlockPos", Integer.class, IntegerFromBlockPos.class, IntegerFromBlockPos::new);
		registerPrecise("integerFromBlockPoses", Integer.class, IntegerFromBlockPoses.class, IntegerFromBlockPoses::new);
		registerPrecise("itemStackCount", Integer.class, ItemStackCount.class, ItemStackCount::new);
		//注册double suppliers
		registerPrecise("constantDouble", Double.class, ConstantDouble.class, ConstantDouble::new);
		registerPrecise("calculateDoubles", Double.class, CalculateDoubles.class, CalculateDoubles::new);
		registerPrecise("doubleConstant", Double.class, DoubleConstant.class, DoubleConstant::new);
		registerPrecise("doubleFunction", Double.class, DoubleFunction.class, DoubleFunction::new);
		registerPrecise("doubleBiFunction", Double.class, DoubleBiFunction.class, DoubleBiFunction::new);
		registerPrecise("doubleFromInteger", Double.class, DoubleFromInteger.class, DoubleFromInteger::new);
		registerPrecise("doubleFromVec3d", Double.class, DoubleFromVec3d.class, DoubleFromVec3d::new);
		registerPrecise("doubleFromVec3ds", Double.class, DoubleFromVec3ds.class, DoubleFromVec3ds::new);
		registerPrecise("blockInteractionRange", Double.class, BlockInteractionRange.class, BlockInteractionRange::new);
		registerPrecise("entityInteractionRange", Double.class, EntityInteractionRange.class, EntityInteractionRange::new);
		//注册string suppliers
		registerPrecise("constantString", String.class, ConstantString.class, ConstantString::new);
		registerPrecise("objectToString", String.class, ObjectToString.class, ObjectToString::new);
		//注册array suppliers
		registerPrecise("newArray", Object[].class, NewArray.class, NewArray::new);
		//注册type suppliers
		registerPrecise("objectType", ScriptType.class, ObjectType.class, ObjectType::new);
		registerPrecise("constantType", ScriptType.class, ConstantType.class, ConstantType::new);
		//注册iterable suppliers
		registerPrecise("iterableFromArray", ObjectIterable.class, IterableFromArray.class, IterableFromArray::new);
		registerPrecise("clientPlayers", ObjectIterable.class, ClientPlayers.class, ClientPlayers::new);
		registerPrecise("clientEntities", ObjectIterable.class, ClientEntities.class, ClientEntities::new);
		registerPrecise("blockPosInDistance", ObjectIterable.class, BlockPosInDistance.class, BlockPosInDistance::new);
		//注册direction suppliers
		registerPrecise("constantDirection", Direction.class, ConstantDirection.class, ConstantDirection::new);
		//注册blockPos suppliers
		registerPrecise("constantBlockPos", BlockPos.class, ConstantBlockPos.class, ConstantBlockPos::new);
		registerPrecise("calculateBlockPoses", BlockPos.class, CalculateBlockPoses.class, CalculateBlockPoses::new);
		registerPrecise("blockPosFromCoordinates", BlockPos.class, BlockPosFromCoordinates.class, BlockPosFromCoordinates::new);
		registerPrecise("blockPosFromVec3d", BlockPos.class, BlockPosFromVec3d.class, BlockPosFromVec3d::new);
		registerPrecise("directionVector", BlockPos.class, DirectionVector.class, DirectionVector::new);
		registerPrecise("entityBlockPos", BlockPos.class, EntityBlockPos.class, EntityBlockPos::new);
		//注册vec3d suppliers
		registerPrecise("constantVec3d", Vec3d.class, ConstantVec3d.class, ConstantVec3d::new);
		registerPrecise("calculateVec3ds", Vec3d.class, CalculateVec3ds.class, CalculateVec3ds::new);
		registerPrecise("vec3dFromCoordinates", Vec3d.class, Vec3dFromCoordinates.class, Vec3dFromCoordinates::new);
		registerPrecise("vec3dFromBlockPos", Vec3d.class, Vec3dFromBlockPos.class, Vec3dFromBlockPos::new);
		registerPrecise("entityPos", Vec3d.class, EntityPos.class, EntityPos::new);
		registerPrecise("entityEyePos", Vec3d.class, EntityEyePos.class, EntityEyePos::new);
		//注册block suppliers
		registerPrecise("constantBlock", Block.class, ConstantBlock.class, ConstantBlock::new);
		registerPrecise("blockInWorld", Block.class, BlockInWorld.class, BlockInWorld::new);
		//注册item suppliers
		registerPrecise("constantItem", Item.class, ConstantItem.class, ConstantItem::new);
		registerPrecise("stackItem", Item.class, StackItem.class, StackItem::new);
		//注册item stack suppliers
		registerPrecise("emptyStack", ItemStack.class, EmptyStack.class, EmptyStack::new);
		registerPrecise("slotItemStack", ItemStack.class, SlotItemStack.class, SlotItemStack::new);
		registerPrecise("inventoryItemStack", ItemStack.class, InventoryItemStack.class, InventoryItemStack::new);
		registerPrecise("currentScreenSlotStack", ItemStack.class, CurrentScreenSlotStack.class, CurrentScreenSlotStack::new);
		//注册entity suppliers
		registerPrecise("vehicleEntity", Entity.class, VehicleEntity.class, VehicleEntity::new);
		//注册player entity suppliers
		registerPrecise("mainPlayerEntity", PlayerEntity.class, MainPlayerEntity.class, MainPlayerEntity::new);
	}
	
	//固定temp数据到不可变映射
	static {
		//suppliers
		suppliers = ImmutableMap.copyOf(suppliersTemp);
		suppliersTemp.clear();
		//suppliersInverseMap
		suppliersInverseMap = ImmutableMap.copyOf(suppliersInverseMapTemp);
		suppliersInverseMapTemp.clear();
		//suppliersFromClass
		LinkedHashMap<Class<?>, ImmutableSet<ScriptRegistration<?, ?>>> _suppliersFromClassTemp = new LinkedHashMap<>();
		for(var entry : suppliersFromClassTemp.entrySet())
			_suppliersFromClassTemp.put(entry.getKey(), ImmutableSet.copyOf(entry.getValue()));
		suppliersFromClass = ImmutableMap.copyOf(_suppliersFromClassTemp);
		suppliersFromClassTemp.clear();
		//preciseSupplierTypeTree
		LinkedHashMap<Class<?>, ImmutableSet<Class<?>>> _preciseSupplierTypeTreeTemp = new LinkedHashMap<>();
		for(var entry : preciseSupplierTypeTreeTemp.entrySet())
			_preciseSupplierTypeTreeTemp.put(entry.getKey(), ImmutableSet.copyOf(entry.getValue()));
		preciseSupplierTypeTree = ImmutableMap.copyOf(_preciseSupplierTypeTreeTemp);
		preciseSupplierTypeTreeTemp.clear();
		//typeMap
		typeMap = ImmutableMap.copyOf(typeMapTemp);
		typeMapTemp.clear();
		//typeIdMap
		typeIdMap = ImmutableMap.copyOf(typeIdMapTemp);
		typeIdMapTemp.clear();
	}
	
	private static boolean putType(Class<?> currentClass, Class<?> targetClass, LinkedHashSet<Class<?>> targetSet, HashSet<Class<?>> visitedClasses){
		if(!currentClass.isAssignableFrom(targetClass)) return false;
		if(visitedClasses.contains(currentClass)) return true;
		visitedClasses.add(currentClass);
		var currSet = preciseSupplierTypeTreeTemp.get(currentClass);
		boolean isDirect = true;
		ArrayList<Class<?>> removed = new ArrayList<>();
		for(var clazz : currSet){
			if(putType(clazz, targetClass, targetSet, visitedClasses))
				isDirect = false;
			else if(targetClass.isAssignableFrom(clazz)){
				targetSet.add(clazz);
				removed.add(clazz);
			}
		}
		removed.forEach(currSet::remove);
		if(isDirect) currSet.add(targetClass);
		return true;
	}
	
	private static <T, U extends IScriptSupplier<? extends T>> boolean register(String key, Class<? super T> clazz, ScriptRegistration<T, U> registration){
		if(!typeMapTemp.containsKey(clazz)){
			Class<? super T> superClass = Object.class;
			for(var type : typeMapTemp.keySet()){
				if(type.isAssignableFrom(clazz) && superClass.isAssignableFrom(type))
					//noinspection unchecked
					superClass = (Class<? super T>)type;
			}
			clazz = superClass;
		}
		if(suppliersTemp.containsKey(key)) return false;
		suppliersTemp.put(key, registration);
		suppliersInverseMapTemp.put(registration.supplierClass, registration);
		suppliersFromClassTemp.computeIfAbsent(clazz, v->new LinkedHashSet<>()).add(registration);
		return true;
	}
	
	private static <T> void registerType(Class<T> basicClass, Function<IScriptWithSubScript, IScriptSupplier<? extends T>> defaultValue, String id){
		var type = new ScriptType.BasicType<>(basicClass, defaultValue, Text.translatable("lpctools.script.typeName." + id), id);
		typeMapTemp.put(basicClass, type);
		typeIdMapTemp.put(id, type);
		LinkedHashSet<Class<?>> set = new LinkedHashSet<>();
		putType(Object.class, basicClass, set, new HashSet<>());
		preciseSupplierTypeTreeTemp.put(basicClass, set);
	}
	
	@SuppressWarnings("UnusedReturnValue")
	public static <T, U extends IScriptSupplier<T>> boolean registerPrecise(String key, Class<T> clazz, Class<U> supplierClass, IScriptSupplierAllocator<U> allocator){
		return register(key, clazz, ScriptRegistration.ofPrecise(key,
			Text.translatable("lpctools.script.suppliers." + typeMapTemp.get(clazz).id() + '.' + key + ".name"),
			Text.translatable("lpctools.script.suppliers." + typeMapTemp.get(clazz).id() + '.' + key + ".comment"),
			clazz, supplierClass, allocator));
	}
	
	@SuppressWarnings("UnusedReturnValue")
	public static <U extends IScriptSupplier<?>> boolean registerRandom(String key, Class<U> supplierClass, IRandomSupplierAllocator allocator){
		return register(key, Object.class, ScriptRegistration.ofRandom(key,
			Text.translatable("lpctools.script.suppliers.random." + key + ".name"),
			Text.translatable("lpctools.script.suppliers.random." + key + ".comment"),
			supplierClass, allocator));
	}
}
