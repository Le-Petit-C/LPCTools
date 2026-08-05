package lpctools.generic;

import lpctools.lpcfymasaapi.configButtons.transferredConfigs.DoubleConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.BooleanThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import lpctools.util.GameTime;
import lpctools.util.HandRestock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

import static lpctools.lpcfymasaapi.LPCConfigStatics.addDoubleConfig;
import static lpctools.lpcfymasaapi.LPCConfigStatics.listStack;

public abstract class OperationSpeedLimit {
	public static final BooleanThirdListConfig limitOperationSpeed = new BooleanThirdListConfig(GenericConfigs.generic, "limitOperationSpeed", false, null);
	static { listStack.push(limitOperationSpeed); }
	public static final DoubleConfig maxOperationSpeed = addDoubleConfig("maxOperationSpeed", 1);
	public static final DoubleConfig interactBlockCost = addDoubleConfig("interactOperationCost", 1, 0, 1);
	public static final DoubleConfig breakBlockCost = addDoubleConfig("breakingOperationCost", 1, 0, 1);
	public static final DoubleConfig interactEntityCost = addDoubleConfig("interactEntityCost", 1, 0, 1);
	public static final DoubleConfig attackEntityCost = addDoubleConfig("attackEntityCost", 1, 0, 1);
	static { listStack.pop(); }
	static {
		ILPCValueChangeCallback rootOperationLimitChangedCallback = OperationSpeedLimit::updateReplenishingSpeed;
		limitOperationSpeed.setValueChangeCallback(rootOperationLimitChangedCallback);
		maxOperationSpeed.setValueChangeCallback(rootOperationLimitChangedCallback);
	}

	public static OperationSpeedLimitRoot root() { return OperationSpeedLimitRoot.INSTANCE; }

	public abstract boolean hasReservedTimes();
	public void costInteractBlock() { decrease(interactBlockCost.getDoubleValue()); }
	public void costBreakBlock() { decrease(breakBlockCost.getDoubleValue()); }
	public void costInteractEntity() { decrease(interactEntityCost.getDoubleValue()); }
	public void costAttackEntity() { decrease(attackEntityCost.getDoubleValue()); }
	public OperationSpeedLimit createSub(double reserved) { return new SubOperationSpeedLimit(this, reserved); }
	public OperationSpeedLimit createSub(int reserved) { return new IntSubOperationSpeedLimit(this, reserved); }
	public ReplenishingLimit createSubReplenishing() { return new ReplenishingSubLimit(this); }
	//自带Restocked测试，每重置一次剩余操作次数最多只能Restock一次，多次调用返回“不可restock”
	public LazyRestockOperationSpeedLimit limitWithRestock(Predicate<ItemStack> restockTest, int offhandPriority) {
		return new LazyRestockOperationSpeedLimit(this, restockTest, offhandPriority);
	}
	public LazyRestockOperationSpeedLimit limitWithRestock(Predicate<ItemStack> restockTest, InteractionHand hand) {
		return limitWithRestock(restockTest, hand == InteractionHand.MAIN_HAND ? 0 : -1);
	}

	protected abstract void decrease(double value);

	public static class ReplenishingLimit extends OperationSpeedLimit {
		protected double reserved = 0, replenishSpeed = 1;
		long lastUpdateTick = GameTime.getClientTickCount();

		private ReplenishingLimit() {}

		public void setReplenishSpeed(double regenerateSpeed) {
			updateReserved();
			this.replenishSpeed = regenerateSpeed;
		}

		void updateReserved() {
			long delta = GameTime.getClientTickCount() - lastUpdateTick;
			if(delta == 0) return;
			if(lastUpdateTick != 0) {
				if (reserved > 0) reserved = 0;
				reserved += replenishSpeed * delta;
				if(reserved > replenishSpeed) reserved = replenishSpeed;
			}
			lastUpdateTick += delta;
		}

		@Override public boolean hasReservedTimes() { updateReserved(); return reserved > 0; }
		@Override protected void decrease(double value) { reserved -= value; }
	}

	private static class ReplenishingSubLimit extends ReplenishingLimit {
		final OperationSpeedLimit parent;
		ReplenishingSubLimit(OperationSpeedLimit parent) { this.parent = parent; }
		@Override public boolean hasReservedTimes() { return super.hasReservedTimes() && parent.hasReservedTimes(); }
		@Override protected void decrease(double value) { reserved -= value; parent.decrease(value); }
	}

	public static final class OperationSpeedLimitRoot extends ReplenishingLimit {
		static final OperationSpeedLimitRoot INSTANCE = new OperationSpeedLimitRoot();
		long lastMainHandRestockTick = 0;
		long lastOffHandRestockTick = 0;
		public long lastRestockTick(InteractionHand hand) {
			return switch (hand) {
				case MAIN_HAND -> lastMainHandRestockTick;
				case OFF_HAND -> lastOffHandRestockTick;
			};
		}
		public boolean notRestockedThisTick(InteractionHand hand) {
			return lastRestockTick(hand) != GameTime.getClientTickCount();
		}
		public void setRestockedThisTick(InteractionHand hand) {
			switch (hand) {
				case OFF_HAND -> lastOffHandRestockTick = GameTime.getClientTickCount();
				case MAIN_HAND -> lastMainHandRestockTick = GameTime.getClientTickCount();
			}
		}
	}

	private static class SubOperationSpeedLimit extends OperationSpeedLimit {
		double reserved;
		final OperationSpeedLimit parent;
		SubOperationSpeedLimit(OperationSpeedLimit parent, double reserved) { this.parent = parent; this.reserved = reserved; }
		@Override public boolean hasReservedTimes() { return reserved > 0 && parent.hasReservedTimes(); }
		@Override protected void decrease(double value) { reserved -= value; parent.decrease(value); }
	}

	private static class IntSubOperationSpeedLimit extends OperationSpeedLimit {
		int reserved;
		final OperationSpeedLimit parent;
		IntSubOperationSpeedLimit(OperationSpeedLimit parent, int reserved) { this.parent = parent; this.reserved = reserved; }
		@Override public boolean hasReservedTimes() { return reserved > 0 && parent.hasReservedTimes(); }
		@Override protected void decrease(double value) { --reserved; parent.decrease(value); }
	}

	public static class LazyRestockOperationSpeedLimit extends OperationSpeedLimit {
		final Predicate<ItemStack> restockTest;
		final int offhandPriority;
		final InteractionHand hand;
		final OperationSpeedLimit parent;

		private LazyRestockOperationSpeedLimit(OperationSpeedLimit parent, Predicate<ItemStack> restockTest, int offhandPriority) {
			this.restockTest = restockTest;
			this.offhandPriority = offhandPriority;
			this.parent = parent;
			this.hand = offhandPriority < 0 ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
		}

		@Override public boolean hasReservedTimes() {
			LocalPlayer player = Minecraft.getInstance().player;
			if(player == null) return false;
			return parent.hasReservedTimes() && ((root().notRestockedThisTick(hand) ? HandRestock.search(restockTest, offhandPriority) != -1 : restockTest.test(player.getItemInHand(hand))));
		}
		public boolean hasReservedTimesRegardlessRestock() {
			LocalPlayer player = Minecraft.getInstance().player;
			if(player == null) return false;
			return parent.hasReservedTimes() && (root().notRestockedThisTick(hand) || restockTest.test(player.getItemInHand(hand)));
		}
		@Override protected void decrease(double value) {
			applyRestock();
			parent.decrease(value);
		}
		public void applyRestock() {
			if(root().notRestockedThisTick(hand)) {
				int res = HandRestock.restock(restockTest, offhandPriority);
				if(res > 0) root().setRestockedThisTick(hand);
			}
		}
	}

	private static void updateReplenishingSpeed() {
		OperationSpeedLimitRoot.INSTANCE.setReplenishSpeed(limitOperationSpeed.getBooleanValue() ? maxOperationSpeed.getDoubleValue() : Double.POSITIVE_INFINITY);
	}

	static { updateReplenishingSpeed(); }
}
