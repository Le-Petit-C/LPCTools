package lpctools.tools.elytraBounce;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.BooleanConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.BooleanHotkeyThirdListConfig;
import lpctools.mixin.client.accessors.EntityAccessor;
import lpctools.tools.ToolUtils;
import lpctools.util.SuppressedEventsHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Input;
import org.jetbrains.annotations.Contract;

import static lpctools.lpcfymasaapi.LPCConfigStatics.addBooleanConfig;
import static lpctools.lpcfymasaapi.LPCConfigStatics.listStack;

public class ElytraBounce {
	public static final BooleanHotkeyThirdListConfig EBConfig = ToolUtils.configBuilder("EB").build();
	static {listStack.push(EBConfig);}
	public static final BooleanConfig holdSpace = addBooleanConfig("holdSpace", true);
	public static final BooleanConfig delayStand = addBooleanConfig("delayStand", true);
	public static final BooleanConfig autoWSprint = addBooleanConfig("autoWSprint", true);
	public static final BooleanConfig instantGlide = addBooleanConfig("instantGlide", false);
	static {listStack.pop();}

	@Contract("null->false")
	private static boolean functionOn(LocalPlayer player) {
		if(player == null || !EBConfig.getBooleanValue()) return false;
		if(holdSpace.getBooleanValue() && !Minecraft.getInstance().options.keyJump.isDown()) return false;
		Abilities playerAbilities = player.getAbilities();
		if(playerAbilities.mayfly || playerAbilities.flying) return false;
		if(player.isPassenger() || player.hasEffect(MobEffects.LEVITATION) || player.isInWater()) return false;
		for (EquipmentSlot slot : EquipmentSlot.VALUES)
			if (LivingEntity.canGlideUsing(player.getItemBySlot(slot), slot)) return true;
		return false;
	}

	@Contract("null -> true")
	private static boolean shouldHandleStandPacket(LocalPlayer player) {
		return !functionOn(player) || !delayStand.getBooleanValue() || !fallFlyPreparing;
	}

	private static boolean isValidPacket(ClientboundSetEntityDataPacket packet, LocalPlayer player) {
		if(packet.id() != player.getId()) return false;
		int id = EntityAccessor.getSharedFlagsId().id();
		for(var item : packet.packedItems())
			if(item.id() == id) return true;
		return false;
	}

	public static boolean handlePacketOnHead(ClientPacketListener listener, ClientboundSetEntityDataPacket packet) {
		LocalPlayer player = Minecraft.getInstance().player;
		if(shouldHandleStandPacket(player) || !isValidPacket(packet, player)) return false;
		SuppressedEventsHolder.SuppressedEvent event = ()->{
			LocalPlayer _player = Minecraft.getInstance().player;
			if(shouldHandleStandPacket(_player)) {
				if(listener == _player.connection) listener.handleSetEntityData(packet);
				return true;
			}
			return false;
		};
		SuppressedEventsHolder.addSuppressedEvent(event);
		return true;
	}

	public static void tryRestoreGlide(ClientboundSetEntityDataPacket packet) {
		LocalPlayer player = Minecraft.getInstance().player;
		if(functionOn(player) && isValidPacket(packet, player) && instantGlide.getBooleanValue() && player.tryToStartFallFlying())
			player.connection.send(new ServerboundPlayerCommandPacket(player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
	}

	private static boolean fallFlyPreparing; // 应当仅在落地到跳起并释放jump时间段内为true

	public static Input modifyInput(boolean forward, boolean backward, boolean left, boolean right, boolean jump, boolean shift, boolean sprint, Operation<Input> original) {
		LocalPlayer player = Minecraft.getInstance().player;
		boolean functionOn = functionOn(player);
		if(!functionOn) {
			fallFlyPreparing = false;
			return original.call(forward, backward, left, right, jump, shift, sprint);
		}
		boolean extraForward = autoWSprint.getAsBoolean();
		boolean extraSprint = autoWSprint.getAsBoolean();
		boolean currentOnGround = player.onGround();
		boolean previousJump = player.input.keyPresses.jump();
		if(currentOnGround) fallFlyPreparing = true;
		else if(!previousJump) fallFlyPreparing = false;
		else if(!player.isFallFlying()) fallFlyPreparing = true;
		boolean thenJump = currentOnGround || (!fallFlyPreparing && (previousJump || !player.isFallFlying()));
		return original.call(forward || extraForward, backward, left, right, thenJump, shift, extraSprint || sprint);
	}
}
