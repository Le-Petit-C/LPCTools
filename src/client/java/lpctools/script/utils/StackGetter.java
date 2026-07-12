package lpctools.script.utils;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public enum StackGetter {
	MAIN_HAND_STACK(LivingEntity::getMainHandItem, Component.translatable("lpctools.script.utils.stackGetter.mainHandStack"), "mainHand"),
	OFF_HAND_STACK(LivingEntity::getOffhandItem, Component.translatable("lpctools.script.utils.stackGetter.offHandStack"), "offHand"),
	FEET_STACK(entity -> entity.getItemBySlot(EquipmentSlot.FEET), Component.translatable("lpctools.script.utils.stackGetter.feetStack"), "feet"),
	LEGS_STACK(entity -> entity.getItemBySlot(EquipmentSlot.LEGS), Component.translatable("lpctools.script.utils.stackGetter.legsStack"), "legs"),
	CHEST_STACK(entity -> entity.getItemBySlot(EquipmentSlot.CHEST), Component.translatable("lpctools.script.utils.stackGetter.chestStack"), "chest"),
	HEAD_STACK(entity -> entity.getItemBySlot(EquipmentSlot.HEAD), Component.translatable("lpctools.script.utils.stackGetter.headStack"), "head"),
	BODY_STACK(entity -> entity.getItemBySlot(EquipmentSlot.BODY), Component.translatable("lpctools.script.utils.stackGetter.bodyStack"), "body"),
	SADDLE_STACK(entity -> entity.getItemBySlot(EquipmentSlot.SADDLE), Component.translatable("lpctools.script.utils.stackGetter.saddleStack"), "saddle"),
	LEFT_HAND_STACK(entity -> entity.getItemHeldByArm(HumanoidArm.LEFT), Component.translatable("lpctools.script.utils.stackGetter.leftHandStack"), "leftHand"),
	RIGHT_HAND_STACK(entity -> entity.getItemHeldByArm(HumanoidArm.RIGHT), Component.translatable("lpctools.script.utils.stackGetter.rightHandStack"), "rightHand"),;
	private final Function<LivingEntity, ItemStack> slotGetter;
	public final Component name;
	public final String id;
	public static final Object2IntMap<String> indexMap;
	private int index;
	static {
		var tempMap = new Object2IntOpenHashMap<String>();
		var values = values();
		for(int i = 0; i < values.length; ++i) {
			tempMap.put(values[i].id, i);
			values[i].index = i;
		}
		indexMap = Object2IntMaps.unmodifiable(tempMap);
	}
	StackGetter(Function<LivingEntity, ItemStack> slotGetter, Component name, String id){
		this.slotGetter = slotGetter;
		this.name = name;
		this.id = id;
	}
	public ItemStack getEntityStack(LivingEntity entity){return slotGetter.apply(entity);}
	public StackGetter cycle(boolean forward){
		var values = values();
		if(forward){
			if(index + 1 == values.length) return values[0];
			else return values[index + 1];
		}
		else {
			if(index == 0) return values[values.length - 1];
			else return values[index - 1];
		}
	}
	public static @Nullable StackGetter fromId(String id){
		int index = indexMap.getOrDefault(id, -1);
		if(index >= 0) return values()[index];
		else return null;
	}
}
