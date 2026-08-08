package lpctools.tools.tradeReroller;

import it.unimi.dsi.fastutil.ints.IntHeapPriorityQueue;
import lpctools.lpcfymasaapi.Registries;
import lpctools.mixin.client.accessors.MerchantMenuAccessor;
import lpctools.tools.ToolUtils;
import lpctools.util.AlgorithmUtils;
import lpctools.util.RuntimeComponentException;
import lpctools.util.DataUtils;
import lpctools.util.HandRestock;
import lpctools.util.MathUtils;
import lpctools.util.inGame.*;
import lpctools.util.javaex.QuietAutoCloseable;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.BlockItem;
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
import java.util.function.UnaryOperator;

import static lpctools.tools.tradeReroller.TradeReroller.*;
import static net.minecraft.world.entity.npc.villager.VillagerProfession.*;

class TradeRerollRunner implements ToolUtils.ToolRunner, ClientTickEvents.EndTick, Registries.ContainerContentInitializedCallback, Registries.MerchantOffersUpdated, QuietAutoCloseable {
	private final BlockPos lecternPos, nextButton, anvilPos;
	private final Vec3 playerPos;
	private final HashMap<EnchantmentTradeOption.EnchantmentWithLevel, IntHeapPriorityQueue> neededEnchantments;
	private final Registry<Enchantment> enchantmentRegistry;
	private final MultiCompletableInGameFutures futureCollection = new MultiCompletableInGameFutures();
	private @Nullable InGameFuture<?> globalFutureCache;
	private int timeOutCounter;
	private ProcessStage stage = new WaitingLibrarian();

	private void then(UnaryOperator<InGameFuture<?>> op) {
		if(globalFutureCache == null) globalFutureCache = futureCollection.createCompleted();
		globalFutureCache = op.apply(globalFutureCache);
	}

	private boolean isTaskDone() { return globalFutureCache == null || globalFutureCache.isDone(); }

	private void clearTasks() {
		futureCollection.cancelAll();
		globalFutureCache = null;
	}

	private boolean foundValidTrade(EnchantmentTradeOption option) {
		IntHeapPriorityQueue queue = neededEnchantments.get(option.enchantment());
		if (queue == null || option.cost() > queue.firstInt()) return false;
		queue.dequeueInt();
		if(reserveCheaperTraders.getBooleanValue() &&
			(pursueBelowMinPrice.getBooleanValue() || option.cost() > option.enchantment().minCost(enchantmentRegistry)))
			queue.enqueue(option.cost() - 1);
		else if(queue.isEmpty()) neededEnchantments.remove(option.enchantment());
		return true;
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
		final MutableComponent reason;
		DisableSignal(MutableComponent reason) { this.reason = reason; }
	}

	private interface ProcessStage {
		void onContainerContentInitialized(AbstractContainerMenu menu, InGameManager data) throws DisableSignal;
		void onMerchantOffersUpdated(MerchantMenu menu, InGameManager data) throws DisableSignal;
		void onEndTick(Minecraft client, InGameManager data) throws DisableSignal;
	}

	private abstract static class AbstractProcessStage implements ProcessStage {
		@Override public void onContainerContentInitialized(AbstractContainerMenu menu, InGameManager data) throws DisableSignal {}
		@Override public void onMerchantOffersUpdated(MerchantMenu menu, InGameManager data) throws DisableSignal {}
		@Override public void onEndTick(Minecraft client, InGameManager data) {}
	}

	// 等待村民转换回无业
	private class WaitingNone extends AbstractProcessStage {
		@Override public void onMerchantOffersUpdated(MerchantMenu menu, InGameManager data) { /* ignore restock events */ }
		@Override public void onEndTick(Minecraft client, InGameManager data) {
			Villager villager = getVillagerAroundLectern(data);
			if(villager != null && villager.getVillagerData().profession().is(NONE)) {
				clearTasks();
				stage = new WaitingLibrarian();
			}
			else if(data.getBlockState(lecternPos).is(Blocks.LECTERN) && isTaskDone())
				then(c->c.thenBreakBlockOrThrow(lecternPos));
		}
	}

	// 等待无业转化为图书管理员
	private class WaitingLibrarian extends AbstractProcessStage {
		@Override public void onEndTick(Minecraft client, InGameManager data) {
			if(!data.getBlockState(lecternPos).is(Blocks.LECTERN) && isTaskDone())
				then(c->c.thenPlaceBlockOrThrow(lecternPos, Blocks.LECTERN, InteractionHand.OFF_HAND));
			Villager villager = getVillagerAroundLectern(data);
			if(villager != null && villager.getVillagerData().profession().is(LIBRARIAN)) {
				clearTasks();
				// TODO lpctools.util.inGame.EntityInteraction
				then(c->c.thenAcceptInGameManagerNextTick(manager->manager.interact(villager, InteractionHand.MAIN_HAND)));
				stage = new WaitingMerchantScreen(villager);
			}
		}
	}

	// 等待交易界面加载
	private class WaitingMerchantScreen extends AbstractProcessStage {
		final Villager villager;
		int menuUpdateMask = 0;
		WaitingMerchantScreen(Villager villager) { this.villager = villager; }
		@Override public void onContainerContentInitialized(AbstractContainerMenu menu, InGameManager data) throws DisableSignal {
			if (Objects.requireNonNull(menu) instanceof MerchantMenu merchantMenu) {
				menuUpdateMask |= 1;
				tryHandleMerchant(merchantMenu, data);
			}
		}
		@Override public void onMerchantOffersUpdated(MerchantMenu menu, InGameManager data) throws DisableSignal {
			menuUpdateMask |= 2;
			tryHandleMerchant(menu, data);
		}
		void tryHandleMerchant(MerchantMenu menu, InGameManager data) throws DisableSignal {
			if((menuUpdateMask & 3) != 3) return;
			Merchant merchant = ((MerchantMenuAccessor)menu).getTrader();
			EnchantmentTradeOption bookResult = null;
			int lockTrade = -1;
			MerchantOffers offers = merchant.getOffers();
			for(int i = 0; i < offers.size(); ++i) {
				MerchantOffer offer = offers.get(i);
				ItemStack costA = offer.getBaseCostA();
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
						if(enchantmentId == null)
							DataUtils.clientMessage(Component.translatable("lpctools.configs.tools.TR.unknownEnchantment", firstEntry.getKey().value().description().getString()), false);
						else if(bookResult == null) {
							var trade = new EnchantmentTradeOption(enchantmentId, firstEntry.getIntValue(), costA.count());
							// foundValidTrade 中有“尝试根据已有附魔提高接下来的要求”的操作，前置保证执行
							boolean valid = foundValidTrade(trade) || merchant.getVillagerXp() > 0;
							if(valid) bookResult = trade;
							if(displayRolls.getBooleanValue() && (valid || ! onlyDisplaySucceededRolls.getBooleanValue())) {
								String msg = String.format("%s%s $%d", firstEntry.getKey().value().description().getString(), MathUtils.romanNumerals(firstEntry.getIntValue()), costA.count());
								DataUtils.clientMessage(Component.literal(msg).withColor(valid ? TextColor.GREEN : TextColor.YELLOW), false);
							}
							// 仅检测第一个附魔交易
							if(!valid) break;
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
					throw new DisableSignal(Component.translatable("lpctools.configs.tools.TR.missingLockItems"));
				int finalLockTrade = lockTrade;
				if(merchant.getVillagerXp() == 0) {
					then(c->c
						.thenAcceptInGameManagerNextTick(manager -> manager.selectMerchant(menu, finalLockTrade))
						.thenAcceptInGameManagerNextTick(manager -> manager.handleContainerInput(menu.containerId, 2, 0, ContainerInput.PICKUP))
					);
				}
				EnchantmentTradeOption finalBookResult = bookResult;
				stage = null;
				then(c->c
					.thenAcceptInGameManagerNextTick(InGameManager::closeContainer)
					.thenPlaceBlockOrThrow(anvilPos, stack->stack.getItem() instanceof BlockItem item && item.getBlock() instanceof AnvilBlock,
						state->state.getBlock() instanceof AnvilBlock, InteractionHand.OFF_HAND)
					.thenInteractBlockOrThrow(anvilPos, null, InteractionHand.MAIN_HAND)
					.thenRunMCThread(()->stage = new WaitingAnvil(finalBookResult, villager))
				);
			}
			else if(bookResult != null)
				disableToolExceptional(Component.translatable("lpctools.configs.tools.TR.notVanillaTrade"));
			else {
				stage = null;
				then(c->c
					.thenAcceptInGameManagerNextTick(InGameManager::closeContainer)
					.thenRunMCThread(()->stage = new WaitingNone())
				);
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
			if(data.player.experienceLevel <= 0) throw new DisableSignal(Component.translatable("lpctools.configs.tools.TR.noExperience"));
			NonNullList<ItemStack> menuItems = menu.getItems();
			for(int i = 0; i < menuItems.size(); ++i) {
				ItemStack stack = menuItems.get(i);
				if(stack.is(Items.NAME_TAG)) {
					boolean shouldPutBack = stack.getCount() > 2;
					int finalI = i;
					then(c->c
						.thenAcceptInGameManagerNextTick(manager-> manager.handleContainerInput(menu.containerId, finalI, 1, ContainerInput.PICKUP))
						.thenAcceptInGameManagerNextTick(manager-> manager.handleContainerInput(menu.containerId, 0, 1, ContainerInput.PICKUP))
					);
					if(shouldPutBack) then(c->c
						.thenAcceptInGameManagerNextTick(manager->manager.handleContainerInput(menu.containerId, finalI, 0, ContainerInput.PICKUP))
					);
					String newName = tradeOption.toJsonString();
					stage = null;
					then(c->c
						.thenAcceptInGameManagerNextTick(manager -> manager.setAnvilNameContent(menu, newName))
						.thenAcceptInGameManagerNextTick(manager -> manager.handleContainerInput(menu.containerId, 2, 0, ContainerInput.QUICK_MOVE))
						.thenRunMCThread(()->runCaught(() -> {
							if (HandRestock.restock(s -> s.is(Items.NAME_TAG)
								&& s.getOrDefault(DataComponents.CUSTOM_NAME, Component.empty()).getString().equals(newName), -1) <= 0)
								throw new DisableSignal(Component.translatable("lpctools.configs.tools.TR.noNameTag")); // 大概率是背包没有空间了
						}))
						.thenAcceptInGameManagerNextTick(InGameManager::closeContainer)
						.thenAcceptInGameManagerNextTick(InGameManager::swapHandsAutoStyle)
						// TODO lpctools.util.inGame.EntityInteraction
						.thenAcceptInGameManagerNextTick(manager-> manager.interact(villager, InteractionHand.MAIN_HAND))
						.thenAcceptInGameManagerNextTick(InGameManager::swapHandsAutoStyle)
						.thenInteractBlockOrThrow(nextButton, null, InteractionHand.MAIN_HAND)
						.thenRunMCThread(()->stage = new WaitingNone())
					);
					return;
				}
			}
		}
	}

	public TradeRerollRunner() throws ToolUtils.RunnerCreateFailedException {
		InGameManager data = InGameManager.get();
		var enchantmentRegistry = InGameUtils.getRegistry(net.minecraft.core.registries.Registries.ENCHANTMENT);
		if(data == null || enchantmentRegistry == null)
			throw new ToolUtils.RunnerCreateFailedException(Component.translatable("lpctools.configs.tools.TR.createFailedNoData"));
		this.enchantmentRegistry = enchantmentRegistry;
		playerPos = data.playerPos();
		BlockPos lecturePos = null, nextButton = null, anvilPos = null;
		for(BlockPos pos : AlgorithmUtils.playerTouchablePoses(data.player)) {
			BlockState state = data.getBlockState(pos);
			switch (state.getBlock()) {
				case LecternBlock _ -> { if(lecturePos == null) lecturePos = pos.immutable(); }
				case ButtonBlock _ -> { if(nextButton == null) nextButton = pos.immutable(); }
				case AnvilBlock _ -> { if(anvilPos == null) anvilPos = pos.immutable(); }
				default -> {}
			}
			if(lecturePos != null && nextButton != null && anvilPos != null) break;
		}
		if(lecturePos == null || nextButton == null || anvilPos == null)
			throw new ToolUtils.RunnerCreateFailedException(Component.translatable("lpctools.configs.tools.TR.createFailedNoBlocks"));
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

	private void disableToolExceptional(MutableComponent reason) { disableTool(reason.withColor(TextColor.RED)); }

	private void disableTool(Component reason) {
		TradeReroller.TRConfig.setBooleanValue(false);
		DataUtils.clientMessage(reason, false);
	}

	private interface StageEvent { void applyToStage(ProcessStage stage, InGameManager data) throws DisableSignal; }
	private interface StageRunnable { void runThrowable() throws DisableSignal; }

	private void runCaught(StageRunnable runnable) {
		try {
			runnable.runThrowable();
		} catch (DisableSignal e) {
			disableToolExceptional(e.reason);
		}
	}

	private void processStageEvent(StageEvent event) {
		if(stage == null) return;
		InGameManager data = InGameManager.get();
		if(data == null) return;
		runCaught(()->event.applyToStage(stage, data));
	}

	@Override public void onEndTick(@NonNull Minecraft client) {
		if(neededEnchantments.isEmpty()) {
			disableTool(Component.translatable("lpctools.configs.tools.TR.allEnchantmentsObtained").withColor(TextColor.GREEN));
			return;
		}
		if(client.isPaused()) return;
		if(globalFutureCache != null && globalFutureCache.isCompletedExceptionally() && !globalFutureCache.isCancelled()) {
			Throwable e = globalFutureCache.exceptionNow();
			disableToolExceptional(RuntimeComponentException.mutableExceptionComponent(e));
		}
		++timeOutCounter;
		ProcessStage lastStage = stage;
		processStageEvent((stage, data)->{
			if(!data.playerPos().equals(playerPos))
				disableToolExceptional(Component.translatable("lpctools.configs.tools.TR.playerMoved"));
			stage.onEndTick(client, data);
		});
		if(lastStage != stage) timeOutCounter = 0;
		if(timeOutCounter >= timeOutTicks.getIntegerValue())
			disableToolExceptional(Component.translatable("lpctools.configs.tools.TR.timeout"));
	}

	@Override public void onContainerContentInitialized(AbstractContainerMenu menu) {
		processStageEvent((stage, data)->stage.onContainerContentInitialized(menu, data));
	}

	@Override public void onMerchantOffersUpdated(MerchantMenu menu) {
		processStageEvent((stage, data)->stage.onMerchantOffersUpdated(menu, data));
	}

	@Override public void close() {
		clearTasks();
		registerAll(false);
	}
}
