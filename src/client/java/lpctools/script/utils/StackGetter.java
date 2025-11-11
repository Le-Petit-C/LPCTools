package lpctools.script.utils;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public enum StackGetter {
	MAIN_HAND_STACK(LivingEntity::getMainHandStack, Text.translatable("lpctools.script.utils.stackGetter.mainHandStack"), "mainHand"),
	OFF_HAND_STACK(LivingEntity::getOffHandStack, Text.translatable("lpctools.script.utils.stackGetter.offHandStack"), "offHand"),
	FEET_STACK(entity -> entity.getEquippedStack(EquipmentSlot.FEET), Text.translatable("lpctools.script.utils.stackGetter.feetStack"), "feet"),
	LEGS_STACK(entity -> entity.getEquippedStack(EquipmentSlot.LEGS), Text.translatable("lpctools.script.utils.stackGetter.legsStack"), "legs"),
	CHEST_STACK(entity -> entity.getEquippedStack(EquipmentSlot.CHEST), Text.translatable("lpctools.script.utils.stackGetter.chestStack"), "chest"),
	HEAD_STACK(entity -> entity.getEquippedStack(EquipmentSlot.HEAD), Text.translatable("lpctools.script.utils.stackGetter.headStack"), "head"),
	BODY_STACK(entity -> entity.getEquippedStack(EquipmentSlot.BODY), Text.translatable("lpctools.script.utils.stackGetter.bodyStack"), "body"),
	SADDLE_STACK(entity -> entity.getEquippedStack(EquipmentSlot.SADDLE), Text.translatable("lpctools.script.utils.stackGetter.saddleStack"), "saddle"),
	LEFT_HAND_STACK(entity -> entity.getStackInArm(Arm.LEFT), Text.translatable("lpctools.script.utils.stackGetter.leftHandStack"), "leftHand"),
	RIGHT_HAND_STACK(entity -> entity.getStackInArm(Arm.RIGHT), Text.translatable("lpctools.script.utils.stackGetter.rightHandStack"), "rightHand"),;
	private final Function<LivingEntity, ItemStack> slotGetter;
	public final Text name;
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
	StackGetter(Function<LivingEntity, ItemStack> slotGetter, Text name, String id){
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
