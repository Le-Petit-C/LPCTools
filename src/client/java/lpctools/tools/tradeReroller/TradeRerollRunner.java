package lpctools.tools.tradeReroller;

import it.unimi.dsi.fastutil.ints.IntHeapPriorityQueue;
import lpctools.lpcfymasaapi.Registries;
import lpctools.mixin.client.accessors.MerchantMenuAccessor;
import lpctools.tools.ToolUtils;
import lpctools.util.AlgorithmUtils;
import lpctools.util.DataUtils;
import lpctools.util.HandRestock;
import lpctools.util.MathUtils;
import lpctools.util.inGame.BlockBreaking;
import lpctools.util.inGame.ClientTickExecutor;
import lpctools.util.inGame.InGameManager;
import lpctools.util.inGame.InGameUtils;
import lpctools.util.javaex.QuietAutoCloseable;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Objects;

import static lpctools.tools.tradeReroller.TradeReroller.*;
import static net.minecraft.world.entity.npc.villager.VillagerProfession.*;

class TradeRerollRunner implements ToolUtils.ToolRunner, ClientTickEvents.EndTick, Registries.ContainerContentInitializedCallback, Registries.MerchantOffersUpdated, QuietAutoCloseable {
	private final BlockPos lecternPos, nextButton, anvilPos;
	private final Vec3 playerPos;
	private @Nullable BlockBreaking breakingTask;
	private int timeOutCounter;
	private final HashMap<EnchantmentTradeOption.EnchantmentWithLevel, IntHeapPriorityQueue> neededEnchantments;
	private ProcessStage stage = new WaitingLibrarian();
	private final Registry<Enchantment> enchantmentRegistry;
	private final ClientTickExecutor operator = new ClientTickExecutor();

	private boolean foundValidTrade(EnchantmentTradeOption option) {
		IntHeapPriorityQueue queue = neededEnchantments.get(option.enchantment());
		if (queue == null || option.cost() > queue.firstInt()) return false;
		queue.dequeueInt();
		if(reserveCheaperTraders.getBooleanValue() && option.cost() > option.enchantment().minCost(enchantmentRegistry))
			queue.enqueue(option.cost() - 1);
		else if(queue.isEmpty()) neededEnchantments.remove(option.enchantment());
		return true;
	}

	private static void disableUnexpectedState() throws DisableSignal { throw new DisableSignal(); }
	private static void tryPlaceBlockByOffhand(BlockPos pos, Block block, InGameManager data) throws DisableSignal {
		BlockState state = data.getBlockState(pos);
		if(state.getBlock() == block) return;
		if(!state.canBeReplaced()) throw new DisableSignal();
		if(HandRestock.restock(item -> item.getItem() == block.asItem(), -1) <= 0)
			throw new DisableSignal();
		data.useItemOn(InteractionHand.OFF_HAND, pos);
	}
	private @Nullable Villager getVillagerAroundLectern(InGameManager data) {
		AABB aabb = new AABB(lecternPos).inflate(1);
		Villager villager = null;
		for(Entity entity : data.getEntities(aabb)) {
			Vec3 entityPosition = entity.position();
			if(aabb.contains(entityPosition) && entity instanceof Villager v) {
				if(villager != null) return null;
				villager = v;
			}
		}
		return villager;
	}

	private static class DisableSignal extends Throwable {
		final Component reason;
		DisableSignal(Component reason) { this.reason = reason; }
		DisableSignal() { this(Component.empty()); }
	}

	private interface ProcessStage {
		void onContainerContentInitialized(AbstractContainerMenu menu, InGameManager data) throws DisableSignal;
		void onMerchantOffersUpdated(MerchantMenu menu, InGameManager data) throws DisableSignal;
		void onEndTick(Minecraft client, InGameManager data) throws DisableSignal;
	}

	private abstract static class AbstractProcessStage implements ProcessStage {
		@Override public void onContainerContentInitialized(AbstractContainerMenu menu, InGameManager data) throws DisableSignal { disableUnexpectedState(); }
		@Override public void onMerchantOffersUpdated(MerchantMenu menu, InGameManager data) throws DisableSignal { disableUnexpectedState(); }
		@Override public void onEndTick(Minecraft client, InGameManager data) throws DisableSignal {}
	}

	// 等待村民转换回无业
	private class WaitingNone extends AbstractProcessStage {
		@Override public void onEndTick(Minecraft client, InGameManager data) {
			Villager villager = getVillagerAroundLectern(data);
			if(villager != null && villager.getVillagerData().profession().is(NONE)) {
				stage = new WaitingLibrarian();
				if(breakingTask != null) breakingTask.cancel();
			}
			else if(data.getBlockState(lecternPos).is(Blocks.LECTERN) && breakingTask == null) {
				breakingTask = BlockBreaking.scheduleBreak(lecternPos).callback((breaking, state)->{
					if(state.isResultState && breakingTask == breaking) breakingTask = null;
				});
			}
		}
	}

	// 等待无业转化为图书管理员
	private class WaitingLibrarian extends AbstractProcessStage {
		@Override public void onEndTick(Minecraft client, InGameManager data) throws DisableSignal {
			tryPlaceBlockByOffhand(lecternPos, Blocks.LECTERN, data);
			Villager villager = getVillagerAroundLectern(data);
			if(villager != null && villager.getVillagerData().profession().is(LIBRARIAN)) {
				operator.schedule(manager->manager.interact(villager, InteractionHand.MAIN_HAND));
				stage = new WaitingMerchantScreen(villager);
			}
		}
	}

	// 等待交易界面加载
	private class WaitingMerchantScreen extends AbstractProcessStage {
		final Villager villager;
		int menuUpdateCounter = 2;
		WaitingMerchantScreen(Villager villager) { this.villager = villager; }
		@Override public void onContainerContentInitialized(AbstractContainerMenu menu, InGameManager data) throws DisableSignal {
			if (Objects.requireNonNull(menu) instanceof MerchantMenu merchantMenu)
				tryHandleMerchant(merchantMenu, data);
		}
		@Override public void onMerchantOffersUpdated(MerchantMenu menu, InGameManager data) throws DisableSignal {
			tryHandleMerchant(menu, data);
		}
		void tryHandleMerchant(MerchantMenu menu, InGameManager data) throws DisableSignal {
			if(--menuUpdateCounter > 0) return;
			Merchant merchant = ((MerchantMenuAccessor)menu).getTrader();
			EnchantmentTradeOption bookResult = null;
			int lockTrade = -1;
			MerchantOffers offers = merchant.getOffers();
			for(int i = 0; i < offers.size(); ++i) {
				MerchantOffer offer = offers.get(i);
				ItemStack costA = offer.getCostA();
				ItemStack costB = offer.getCostB();
				ItemStack result = offer.getResult();
				if(((costA.getItem() == Items.EMERALD && costA.getCount() <= 9)
					|| (costA.getItem() == Items.PAPER && costA.count() <= 24))
					&& costB.isEmpty()
				) {
					if(lockTrade < 0) lockTrade = i;
				}
				else if(costA.getItem() == Items.EMERALD && costB.getItem() == Items.BOOK && result.getItem() == Items.ENCHANTED_BOOK) {
					ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(result);
					if(!enchantments.isEmpty()) {
						var firstEntry = enchantments.entrySet().iterator().next();
						Identifier enchantmentId = enchantmentRegistry.getKey(firstEntry.getKey().value());
						if(enchantmentId == null) {
							// TODO msg("Unknown Enchantment: %s")
							DataUtils.clientMessage("", false);
						}
						else if(bookResult == null) {
							var trade = new EnchantmentTradeOption(enchantmentId, firstEntry.getIntValue(), costA.count());
							boolean valid = foundValidTrade(trade);
							String msg = String.format("%s%s $%d", firstEntry.getKey().value().description().getString(), MathUtils.romanNumerals(firstEntry.getIntValue()), costA.count());
							if(valid) bookResult = trade;
							DataUtils.clientMessage(Component.literal(msg).withColor(valid ? TextColor.GREEN : TextColor.YELLOW), false);
						}
					}
				}
			}
			if(bookResult != null && lockTrade >= 0) {
				MerchantOffer offer = offers.get(lockTrade);
				ItemStack costA = offer.getCostA().copy();
				ItemStack costB = offer.getCostB().copy();
				for(ItemStack stack : data.getInventory().getNonEquipmentItems()) {
					int i = stack.count();
					if(ItemStack.isSameItemSameComponents(stack, costA)) {
						int n = Math.min(costA.count(), i);
						costA.setCount(costA.count() - n);
						i -= n;
					}
					if(ItemStack.isSameItemSameComponents(stack, costB)) {
						int n = Math.min(costB.count(), i);
						costB.setCount(costB.count() - n);
					}
				}
				if(!costA.isEmpty() || !costB.isEmpty())
					throw new DisableSignal();
				int finalLockTrade = lockTrade;
				operator.schedule(manager->manager.selectMerchant(menu, finalLockTrade));
				operator.schedule(manager->manager.handleContainerInput(menu.containerId, 2, 0, ContainerInput.PICKUP));
				operator.schedule(InGameManager::closeContainer);
				operator.schedule(manager->runCaught(()->tryPlaceBlockByOffhand(anvilPos, Blocks.ANVIL, manager)));
				EnchantmentTradeOption finalBookResult = bookResult;
				operator.schedule(manager->{
					manager.useItemOn(InteractionHand.MAIN_HAND, anvilPos);
					stage = new WaitingAnvil(finalBookResult, villager);
				});
				stage = null;
			}
			else {
				if(bookResult != null) {
					// TODO warning not vanilla?
				}
				operator.schedule(InGameManager::closeContainer);
				stage = new WaitingNone();
			}
		}
	}

	// 等待铁砧界面打开
	private class WaitingAnvil extends AbstractProcessStage {
		final EnchantmentTradeOption tradeOption;
		final Villager villager;
		WaitingAnvil(EnchantmentTradeOption tradeOption, Villager villager) { this.tradeOption = tradeOption; this.villager = villager; }
		@Override public void onContainerContentInitialized(AbstractContainerMenu menu, InGameManager data) throws DisableSignal {
			if(menu instanceof AnvilMenu anvilMenu) tryHandleAnvil(anvilMenu, data);
		}
		void tryHandleAnvil(AnvilMenu menu, InGameManager data) throws DisableSignal {
			Villager villager = getVillagerAroundLectern(data);
			if(villager == null) throw new IllegalStateException();
			if(data.player().experienceLevel <= 0) throw new DisableSignal();
			NonNullList<ItemStack> menuItems = menu.getItems();
			for(int i = 0; i < menuItems.size(); ++i) {
				ItemStack stack = menuItems.get(i);
				if(stack.is(Items.NAME_TAG)) {
					boolean shouldPutBack = stack.getCount() > 2;
					int finalI = i;
					operator.schedule(manager->manager.handleContainerInput(menu.containerId, finalI, 1, ContainerInput.PICKUP));
					operator.schedule(manager->manager.handleContainerInput(menu.containerId, 0, 1, ContainerInput.PICKUP));
					if(shouldPutBack) operator.schedule(manager->manager.handleContainerInput(menu.containerId, finalI, 0, ContainerInput.PICKUP));
					String newName = tradeOption.toJsonString();
					operator.schedule(manager->manager.setAnvilNameContent(menu, newName));
					operator.schedule(manager->manager.handleContainerInput(menu.containerId, 2, 0, ContainerInput.QUICK_MOVE));
					operator.schedule(_->runCaught(()->{
						if(HandRestock.restock(s->s.is(Items.NAME_TAG)
							&& s.getOrDefault(DataComponents.CUSTOM_NAME, Component.empty()).getString().equals(newName), -1) <= 0)
							throw new DisableSignal(); // 大概率是背包没有空间了
						}));
					operator.schedule(InGameManager::closeContainer);
					operator.schedule(InGameManager::swapHandsAutoStyle);
					operator.schedule(manager->manager.interact(villager, InteractionHand.MAIN_HAND));
					operator.schedule(InGameManager::swapHandsAutoStyle);
					operator.schedule(manager->manager.useItemOn(InteractionHand.MAIN_HAND, nextButton));
					operator.schedule(_ ->stage = new WaitingNone());
					stage = null;
					return;
				}
			}
		}
	}

	public TradeRerollRunner() throws ToolUtils.RunnerCreateFailedException {
		InGameManager data = InGameManager.get();
		var enchantmentRegistry = InGameUtils.getRegistry(net.minecraft.core.registries.Registries.ENCHANTMENT);
		if(data == null || enchantmentRegistry == null) throw new ToolUtils.RunnerCreateFailedException();
		this.enchantmentRegistry = enchantmentRegistry;
		playerPos = data.playerPos();
		BlockPos lecturePos = null, nextButton = null, anvilPos = null;
		for(BlockPos pos : AlgorithmUtils.playerTouchablePoses(data.player())) {
			BlockState state = data.getBlockState(pos);
			switch (state.getBlock()) {
				case LecternBlock _ -> { if(lecturePos == null) lecturePos = pos.immutable(); }
				case ButtonBlock _ -> { if(nextButton == null) nextButton = pos.immutable(); }
				case AnvilBlock _ -> { if(anvilPos == null) anvilPos = pos.immutable(); }
				default -> {}
			}
			if(lecturePos != null && nextButton != null && anvilPos != null) break;
		}
		if(lecturePos == null || nextButton == null || anvilPos == null) throw new ToolUtils.RunnerCreateFailedException();
		this.lecternPos = lecturePos;
		this.nextButton = nextButton;
		this.anvilPos = anvilPos;
		neededEnchantments = new HashMap<>();
		updateNeededEnchantments(data);
	}

	public void updateNeededEnchantments(InGameManager data) {
		neededEnchantments.clear();
		for(String str : targetEnchantments) {
			EnchantmentTradeOption option = EnchantmentTradeOption.fromJsonString(str);
			if(option != null) neededEnchantments.computeIfAbsent(option.enchantment(),
				_->new IntHeapPriorityQueue((i1, i2)->Integer.compare(i2, i1)))
				.enqueue(option.cost());
		}
		for(Entity entity : data.getAllEntities()) {
			if(entity instanceof Villager villager && villager.getVillagerData().profession().is(LIBRARIAN)) {
				Component customName = villager.getCustomName();
				if(customName != null) {
					EnchantmentTradeOption option = EnchantmentTradeOption.fromJsonString(customName.getString());
					if (option != null) foundValidTrade(option);
				}
			}
		}
	}

	@Override public void registerAll(boolean b) {
		Registries.END_CLIENT_TICK.register(this, b);
		Registries.CLIENT_CONTAINER_CONTENT_INITIALIZED.register(this, b);
		Registries.CLIENT_MERCHANT_OFFERS_UPDATED.register(this, b);
	}

	private void disableTool(Component reason) {
		TradeReroller.TRConfig.setBooleanValue(false);
		DataUtils.clientMessage(reason, true);
	}

	private interface StageEvent { void applyToStage(ProcessStage stage, InGameManager data) throws DisableSignal; }
	private interface StageRunnable { void runThrowable() throws DisableSignal; }

	private void runCaught(StageRunnable runnable) {
		try {
			runnable.runThrowable();
		} catch (DisableSignal e) {
			disableTool(e.reason);
		}
	}

	private void processStageEvent(StageEvent event) {
		if(stage == null) return;
		InGameManager data = InGameManager.get();
		if(data == null) return;
		runCaught(()->event.applyToStage(stage, data));
	}

	@Override public void onEndTick(@NonNull Minecraft client) {
		if(client.isPaused()) return;
		++timeOutCounter;
		processStageEvent((stage, data)->{
			if(!data.playerPos().equals(playerPos))
				// TODO message("别乱动！")
				disableTool(Component.empty());
			stage.onEndTick(client, data);
		});
		if(timeOutCounter >= 20 * 60) disableTool(Component.empty());
	}

	@Override public void onContainerContentInitialized(AbstractContainerMenu menu) {
		processStageEvent((stage, data)->stage.onContainerContentInitialized(menu, data));
	}

	@Override public void onMerchantOffersUpdated(MerchantMenu menu) {
		processStageEvent((stage, data)->stage.onMerchantOffersUpdated(menu, data));
	}

	@Override public void close() {
		if(breakingTask != null) {
			breakingTask.cancel();
			breakingTask = null;
		}
		registerAll(false);
		operator.close();
	}
}
