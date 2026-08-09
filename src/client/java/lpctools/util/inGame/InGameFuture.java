package lpctools.util.inGame;

import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import it.unimi.dsi.fastutil.objects.ObjectBooleanImmutablePair;
import lpctools.lpcfymasaapi.Registries;
import lpctools.lpcfymasaapi.interfaces.IUnregistrableRegistryBase;
import lpctools.util.RuntimeComponentException;
import lpctools.util.DirectionVectorPredicator;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayDeque;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.*;

public class InGameFuture<T> extends CompletableFuture<T> implements Comparable<InGameFuture<?>> {
	@Nullable MultiCompletableInGameFutures relatedCollection;
	private static final AtomicLong idRecorder = new AtomicLong();
	private final long id = idRecorder.getAndAdd(1);
	public InGameFuture() { super(); }
	InGameFuture(@Nullable MultiCompletableInGameFutures relatedCollection) {
		super();
		if(relatedCollection != null) {
			relatedCollection.futures.add(this);
			whenComplete((_, _)->relatedCollection.futures.remove(this));
		}
		this.relatedCollection = relatedCollection;
	}
	@Override public <U> InGameFuture<U> newIncompleteFuture() { return new InGameFuture<>(relatedCollection); }

	@SuppressWarnings("unchecked") @Override public @NonNull <U> InGameFuture<U> thenApply(@NonNull Function<? super T, ? extends U> fn) { return (InGameFuture<U>) super.thenApply(fn); }
	@SuppressWarnings("unchecked") @Override public @NonNull <U> InGameFuture<U> thenApplyAsync(@NonNull Function<? super T, ? extends U> fn) { return (InGameFuture<U>) super.thenApplyAsync(fn); }
	@SuppressWarnings("unchecked") @Override public @NonNull <U> InGameFuture<U> thenApplyAsync(@NonNull Function<? super T, ? extends U> fn, @NonNull Executor executor) { return (InGameFuture<U>) super.thenApplyAsync(fn, executor); }
	public @NonNull <U> InGameFuture<U> thenApplyMCThread(@NonNull Function<? super T, ? extends U> fn) { return thenApplyAsync(fn, mcExecutor()); }

	@Override public @NonNull InGameFuture<Void> thenAccept(@NonNull Consumer<? super T> action) { return (InGameFuture<Void>) super.thenAccept(action); }
	@Override public @NonNull InGameFuture<Void> thenAcceptAsync(@NonNull Consumer<? super T> action) { return (InGameFuture<Void>) super.thenAcceptAsync(action); }
	@Override public @NonNull InGameFuture<Void> thenAcceptAsync(@NonNull Consumer<? super T> action, @NonNull Executor executor) { return (InGameFuture<Void>) super.thenAcceptAsync(action, executor); }
	public @NonNull InGameFuture<Void> thenAcceptMCThread(@NonNull Consumer<? super T> action) { return thenAcceptAsync(action, mcExecutor()); }

	@Override public @NonNull InGameFuture<Void> thenRun(@NonNull Runnable action) { return (InGameFuture<Void>) super.thenRun(action); }
	@Override public @NonNull InGameFuture<Void> thenRunAsync(@NonNull Runnable action) { return (InGameFuture<Void>) super.thenRunAsync(action); }
	@Override public @NonNull InGameFuture<Void> thenRunAsync(@NonNull Runnable action, @NonNull Executor executor) { return (InGameFuture<Void>) super.thenRunAsync(action, executor); }
	public @NonNull InGameFuture<Void> thenRunMCThread(@NonNull Runnable action) { return thenRunAsync(action, mcExecutor()); }

	@SuppressWarnings("unchecked") @Override public @NonNull <U, V> InGameFuture<V> thenCombine(@NonNull CompletionStage<? extends U> other, @NonNull BiFunction<? super T, ? super U, ? extends V> fn) { return (InGameFuture<V>) super.thenCombine(other, fn); }
	@SuppressWarnings("unchecked") @Override public @NonNull <U, V> InGameFuture<V> thenCombineAsync(@NonNull CompletionStage<? extends U> other, @NonNull BiFunction<? super T, ? super U, ? extends V> fn) { return (InGameFuture<V>) super.thenCombineAsync(other, fn); }
	@SuppressWarnings("unchecked") @Override public @NonNull <U, V> InGameFuture<V> thenCombineAsync(@NonNull CompletionStage<? extends U> other, @NonNull BiFunction<? super T, ? super U, ? extends V> fn, @NonNull Executor executor) { return (InGameFuture<V>) super.thenCombineAsync(other, fn, executor); }
	public @NonNull <U, V> InGameFuture<V> thenCombineMCThread(@NonNull CompletionStage<? extends U> other, @NonNull BiFunction<? super T, ? super U, ? extends V> fn) { return thenCombineAsync(other, fn, mcExecutor()); }

	@Override public @NonNull <U> InGameFuture<Void> thenAcceptBoth(@NonNull CompletionStage<? extends U> other, @NonNull BiConsumer<? super T, ? super U> action) { return (InGameFuture<Void>) super.thenAcceptBoth(other, action); }
	@Override public @NonNull <U> InGameFuture<Void> thenAcceptBothAsync(@NonNull CompletionStage<? extends U> other, @NonNull BiConsumer<? super T, ? super U> action) { return (InGameFuture<Void>) super.thenAcceptBothAsync(other, action); }
	@Override public @NonNull <U> InGameFuture<Void> thenAcceptBothAsync(@NonNull CompletionStage<? extends U> other, @NonNull BiConsumer<? super T, ? super U> action, @NonNull Executor executor) { return (InGameFuture<Void>) super.thenAcceptBothAsync(other, action, executor); }
	public @NonNull <U> InGameFuture<Void> thenAcceptBothMCThread(@NonNull CompletionStage<? extends U> other, @NonNull BiConsumer<? super T, ? super U> action) { return thenAcceptBothAsync(other, action, mcExecutor()); }

	@Override public @NonNull InGameFuture<Void> runAfterBoth(@NonNull CompletionStage<?> other, @NonNull Runnable action) { return (InGameFuture<Void>) super.runAfterBoth(other, action); }
	@Override public @NonNull InGameFuture<Void> runAfterBothAsync(@NonNull CompletionStage<?> other, @NonNull Runnable action) { return (InGameFuture<Void>) super.runAfterBothAsync(other, action); }
	@Override public @NonNull InGameFuture<Void> runAfterBothAsync(@NonNull CompletionStage<?> other, @NonNull Runnable action, @NonNull Executor executor) { return (InGameFuture<Void>) super.runAfterBothAsync(other, action, executor); }
	public @NonNull InGameFuture<Void> runAfterBothMCThread(@NonNull CompletionStage<?> other, @NonNull Runnable action) { return runAfterBothAsync(other, action, mcExecutor()); }

	@Override public @NonNull <U> InGameFuture<U> applyToEither(@NonNull CompletionStage<? extends T> other, @NonNull Function<? super T, U> fn) { return (InGameFuture<U>) super.applyToEither(other, fn); }
	@Override public @NonNull <U> InGameFuture<U> applyToEitherAsync(@NonNull CompletionStage<? extends T> other, @NonNull Function<? super T, U> fn) { return (InGameFuture<U>) super.applyToEitherAsync(other, fn); }
	@Override public @NonNull <U> InGameFuture<U> applyToEitherAsync(@NonNull CompletionStage<? extends T> other, @NonNull Function<? super T, U> fn, @NonNull Executor executor) { return (InGameFuture<U>) super.applyToEitherAsync(other, fn, executor); }
	public @NonNull <U> InGameFuture<U> applyToEitherMCThread(@NonNull CompletionStage<? extends T> other, @NonNull Function<? super T, U> fn) { return applyToEitherAsync(other, fn, mcExecutor()); }

	@Override public @NonNull InGameFuture<Void> acceptEither(@NonNull CompletionStage<? extends T> other, @NonNull Consumer<? super T> action) { return (InGameFuture<Void>) super.acceptEither(other, action); }
	@Override public @NonNull InGameFuture<Void> acceptEitherAsync(@NonNull CompletionStage<? extends T> other, @NonNull Consumer<? super T> action) { return (InGameFuture<Void>) super.acceptEitherAsync(other, action); }
	@Override public @NonNull InGameFuture<Void> acceptEitherAsync(@NonNull CompletionStage<? extends T> other, @NonNull Consumer<? super T> action, @NonNull Executor executor) { return (InGameFuture<Void>) super.acceptEitherAsync(other, action, executor); }
	public @NonNull InGameFuture<Void> acceptEitherMCThread(@NonNull CompletionStage<? extends T> other, @NonNull Consumer<? super T> action) { return acceptEitherAsync(other, action, mcExecutor()); }

	@Override public @NonNull InGameFuture<Void> runAfterEither(@NonNull CompletionStage<?> other, @NonNull Runnable action) { return (InGameFuture<Void>) super.runAfterEither(other, action); }
	@Override public @NonNull InGameFuture<Void> runAfterEitherAsync(@NonNull CompletionStage<?> other, @NonNull Runnable action) { return (InGameFuture<Void>) super.runAfterEitherAsync(other, action); }
	@Override public @NonNull InGameFuture<Void> runAfterEitherAsync(@NonNull CompletionStage<?> other, @NonNull Runnable action, @NonNull Executor executor) { return (InGameFuture<Void>) super.runAfterEitherAsync(other, action, executor); }
	public @NonNull InGameFuture<Void> runAfterEitherMCThread(@NonNull CompletionStage<?> other, @NonNull Runnable action) { return runAfterEitherAsync(other, action, mcExecutor()); }

	@Override public @NonNull <U> InGameFuture<U> thenCompose(@NonNull Function<? super T, ? extends CompletionStage<U>> fn) { return (InGameFuture<U>) super.thenCompose(fn); }
	@Override public @NonNull <U> InGameFuture<U> thenComposeAsync(@NonNull Function<? super T, ? extends CompletionStage<U>> fn) { return (InGameFuture<U>) super.thenComposeAsync(fn); }
	@Override public @NonNull <U> InGameFuture<U> thenComposeAsync(@NonNull Function<? super T, ? extends CompletionStage<U>> fn, @NonNull Executor executor) { return (InGameFuture<U>) super.thenComposeAsync(fn, executor); }
	public @NonNull <U> InGameFuture<U> thenComposeMCThread(@NonNull Function<? super T, ? extends CompletionStage<U>> fn) { return thenComposeAsync(fn, mcExecutor()); }

	@SuppressWarnings("unchecked") @Override public @NonNull <U> InGameFuture<U> handle(@NonNull BiFunction<? super T, Throwable, ? extends U> fn) { return (InGameFuture<U>) super.handle(fn); }
	@SuppressWarnings("unchecked") @Override public @NonNull <U> InGameFuture<U> handleAsync(@NonNull BiFunction<? super T, Throwable, ? extends U> fn) { return (InGameFuture<U>) super.handleAsync(fn); }
	@SuppressWarnings("unchecked") @Override public @NonNull <U> InGameFuture<U> handleAsync(@NonNull BiFunction<? super T, Throwable, ? extends U> fn, @NonNull Executor executor) { return (InGameFuture<U>) super.handleAsync(fn, executor); }
	public @NonNull <U> InGameFuture<U> handleMCThread(@NonNull BiFunction<? super T, Throwable, ? extends U> fn) { return handleAsync(fn, mcExecutor()); }

	@Override public @NonNull InGameFuture<T> whenComplete(@NonNull BiConsumer<? super T, ? super Throwable> action) { return (InGameFuture<T>) super.whenComplete(action); }
	@Override public @NonNull InGameFuture<T> whenCompleteAsync(@NonNull BiConsumer<? super T, ? super Throwable> action) { return (InGameFuture<T>) super.whenCompleteAsync(action); }
	@Override public @NonNull InGameFuture<T> whenCompleteAsync(@NonNull BiConsumer<? super T, ? super Throwable> action, @NonNull Executor executor) { return (InGameFuture<T>) super.whenCompleteAsync(action, executor); }
	public @NonNull InGameFuture<T> whenCompleteMCThread(@NonNull BiConsumer<? super T, ? super Throwable> action) { return whenCompleteAsync(action, mcExecutor()); }

	@Override public @NonNull InGameFuture<T> exceptionally(@NonNull Function<Throwable, ? extends T> fn) { return (InGameFuture<T>) super.exceptionally(fn); }
	@Override public @NonNull InGameFuture<T> exceptionallyAsync(@NonNull Function<Throwable, ? extends T> fn) { return (InGameFuture<T>) super.exceptionallyAsync(fn); }
	@Override public @NonNull InGameFuture<T> exceptionallyAsync(@NonNull Function<Throwable, ? extends T> fn, @NonNull Executor executor) { return (InGameFuture<T>) super.exceptionallyAsync(fn, executor); }
	public @NonNull InGameFuture<T> exceptionallyMCThread(@NonNull Function<Throwable, ? extends T> fn) { return exceptionallyAsync(fn, mcExecutor()); }

	@Override public @NonNull InGameFuture<T> exceptionallyCompose(@NonNull Function<Throwable, ? extends CompletionStage<T>> fn) { return (InGameFuture<T>) super.exceptionallyCompose(fn); }
	@Override public @NonNull InGameFuture<T> exceptionallyComposeAsync(@NonNull Function<Throwable, ? extends CompletionStage<T>> fn) { return (InGameFuture<T>) super.exceptionallyComposeAsync(fn); }
	@Override public @NonNull InGameFuture<T> exceptionallyComposeAsync(@NonNull Function<Throwable, ? extends CompletionStage<T>> fn, @NonNull Executor executor) { return (InGameFuture<T>) super.exceptionallyComposeAsync(fn, executor); }
	public @NonNull InGameFuture<T> exceptionallyComposeMCThread(@NonNull Function<Throwable, ? extends CompletionStage<T>> fn) { return (InGameFuture<T>) super.exceptionallyComposeAsync(fn, mcExecutor()); }

	@Override public @NonNull InGameFuture<T> copy() { return (InGameFuture<T>) super.copy(); }
	@Override public @NonNull InGameFuture<T> completeOnTimeout(@NonNull T value, long timeout, @NonNull TimeUnit unit) { return (InGameFuture<T>) super.completeOnTimeout(value, timeout, unit); }
	@Override public @NonNull InGameFuture<T> orTimeout(long timeout, @NonNull TimeUnit unit) { return (InGameFuture<T>) super.orTimeout(timeout, unit); }
	@Override public @NonNull InGameFuture<T> toCompletableFuture() { return this; }

	public @NonNull <U extends InGameOperation<U, ?>>
	InGameFuture<ObjectBooleanImmutablePair<U>> thenBasicInGameOperation(Function<? super T, ? extends U> fn) {
		InGameFuture<ObjectBooleanImmutablePair<U>> res = newIncompleteFuture();
		MutableObject<U> operation = new MutableObject<>();
		thenAcceptMCThread(v -> operation.setValue(fn.apply(v).appendOnResultCallback((instance, succeeded)->
			res.complete(new ObjectBooleanImmutablePair<>(instance, succeeded))
		))).exceptionally(ex -> {
			res.completeExceptionally(ex);
			return null;
		});
		res.exceptionally(ex -> {
			if (operation.get() instanceof U u && !u.isRemoved()) mcExecutor().execute(
				() -> u.cancel(RuntimeComponentException.exceptionComponent(ex))
			);
			return null;
		});
		return res;
	}

	public @NonNull <U extends InGameOperation<U, ?>>
	InGameFuture<U> thenBasicInGameOperationOrThrow(Function<? super T, ? extends U> fn) {
		InGameFuture<U> res = newIncompleteFuture();
		MutableObject<U> operation = new MutableObject<>();
		thenAcceptMCThread(v -> operation.setValue(fn.apply(v).appendOnResultCallback((instance, succeeded)->{
			if(succeeded) res.complete(instance);
			else res.completeExceptionally(new RuntimeComponentException(instance.getFailComponent()));
		}))).exceptionally(ex -> {
			res.completeExceptionally(ex);
			return null;
		});
		res.exceptionally(ex -> {
			if (operation.get() instanceof U u && !u.isRemoved()) mcExecutor().execute(
				() -> u.cancel(RuntimeComponentException.exceptionComponent(ex))
			);
			return null;
		});
		return res;
	}

	public @NonNull InGameFuture<ObjectBooleanImmutablePair<BlockBreaking>> thenBreakBlock(Function<T, BlockPos> fn) {
		return thenBasicInGameOperation(v -> BlockBreaking.scheduleBreak(fn.apply(v)));
	}

	public @NonNull InGameFuture<ObjectBooleanImmutablePair<BlockBreaking>> thenBreakBlock(BlockPos pos) {
		var fixedPos = pos.immutable();
		return thenBasicInGameOperation(_ -> BlockBreaking.scheduleBreak(fixedPos));
	}

	public @NonNull InGameFuture<BlockBreaking> thenBreakBlockOrThrow(Function<T, BlockPos> fn) {
		return thenBasicInGameOperationOrThrow(v -> BlockBreaking.scheduleBreak(fn.apply(v)));
	}

	public @NonNull InGameFuture<BlockBreaking> thenBreakBlockOrThrow(BlockPos pos) {
		var fixedPos = pos.immutable();
		return thenBasicInGameOperationOrThrow(_ -> BlockBreaking.scheduleBreak(fixedPos));
	}

	// 多参数的就不做Function版本了，自己去调thenBasicInGameOperation
	public @NonNull InGameFuture<ObjectBooleanImmutablePair<BlockInteraction>> thenInteractBlock(BlockPos pos, Direction direction, BlockInteraction.Prepare prepare) {
		return thenBasicInGameOperation(_ -> BlockInteraction.scheduleInteract(pos, direction, prepare));
	}

	public @NonNull InGameFuture<ObjectBooleanImmutablePair<BlockInteraction>> thenInteractBlock(BlockPos pos, Direction direction, InteractionHand hand) {
		return thenBasicInGameOperation(_ -> BlockInteraction.scheduleInteract(pos, direction, hand));
	}

	public @NonNull InGameFuture<BlockInteraction> thenInteractBlockOrThrow(BlockPos pos, Direction direction, BlockInteraction.Prepare prepare) {
		return thenBasicInGameOperationOrThrow(_ -> BlockInteraction.scheduleInteract(pos, direction, prepare));
	}

	public @NonNull InGameFuture<BlockInteraction> thenInteractBlockOrThrow(BlockPos pos, Direction direction, InteractionHand hand) {
		return thenBasicInGameOperationOrThrow(_ -> BlockInteraction.scheduleInteract(pos, direction, hand));
	}

	public @NonNull InGameFuture<ObjectBooleanImmutablePair<BlockPlacing>>
	thenPlaceBlock(BlockPos pos, Predicate<ItemStack> restockTest, Predicate<BlockState> blockTest,
				   InteractionHand preferredHand, boolean forcePreferredHand,
				   @Nullable Direction requiredInteractDirection, @Nullable DirectionVectorPredicator requiredPlayerDirection) {
		return thenBasicInGameOperation(_ -> BlockPlacing.schedulePlace(pos, restockTest, blockTest, preferredHand, forcePreferredHand, requiredInteractDirection, requiredPlayerDirection));
	}

	public @NonNull InGameFuture<ObjectBooleanImmutablePair<BlockPlacing>>
	thenPlaceBlock(BlockPos pos, Block targetBlock, InteractionHand preferredHand, boolean forcePreferredHand,
				   @Nullable Direction requiredInteractDirection, @Nullable DirectionVectorPredicator requiredPlayerDirection) {
		return thenBasicInGameOperation(_ -> BlockPlacing.schedulePlace(pos, targetBlock, preferredHand, forcePreferredHand, requiredInteractDirection, requiredPlayerDirection));
	}

	public @NonNull InGameFuture<ObjectBooleanImmutablePair<BlockPlacing>> thenPlaceBlock(BlockPos pos, Predicate<ItemStack> restockTest, Predicate<BlockState> blockTest, InteractionHand hand) {
		return thenBasicInGameOperation(_ -> BlockPlacing.schedulePlace(pos, restockTest, blockTest, hand));
	}

	public @NonNull InGameFuture<ObjectBooleanImmutablePair<BlockPlacing>> thenPlaceBlock(BlockPos pos, Block targetBlock, InteractionHand hand) {
		return thenBasicInGameOperation(_ -> BlockPlacing.schedulePlace(pos, targetBlock, hand));
	}

	public @NonNull InGameFuture<BlockPlacing>
	thenPlaceBlockOrThrow(BlockPos pos, Predicate<ItemStack> restockTest, Predicate<BlockState> blockTest,
						  InteractionHand preferredHand, boolean forcePreferredHand,
						  @Nullable Direction requiredInteractDirection, @Nullable DirectionVectorPredicator requiredPlayerDirection) {
		return thenBasicInGameOperationOrThrow(_ -> BlockPlacing.schedulePlace(pos, restockTest, blockTest, preferredHand, forcePreferredHand, requiredInteractDirection, requiredPlayerDirection));
	}

	public @NonNull InGameFuture<BlockPlacing>
	thenPlaceBlockOrThrow(BlockPos pos, Block targetBlock, InteractionHand preferredHand, boolean forcePreferredHand,
						  @Nullable Direction requiredInteractDirection, @Nullable DirectionVectorPredicator requiredPlayerDirection) {
		return thenBasicInGameOperationOrThrow(_ -> BlockPlacing.schedulePlace(pos, targetBlock, preferredHand, forcePreferredHand, requiredInteractDirection, requiredPlayerDirection));
	}

	public @NonNull InGameFuture<BlockPlacing>
	thenPlaceBlockOrThrow(BlockPos pos, Predicate<ItemStack> restockTest, Predicate<BlockState> blockTest, InteractionHand hand) {
		return thenBasicInGameOperationOrThrow(_ -> BlockPlacing.schedulePlace(pos, restockTest, blockTest, hand));
	}

	public @NonNull InGameFuture<BlockPlacing>
	thenPlaceBlockOrThrow(BlockPos pos, Block targetBlock, InteractionHand hand) {
		return thenBasicInGameOperationOrThrow(_ -> BlockPlacing.schedulePlace(pos, targetBlock, hand));
	}

	public @NonNull InGameFuture<ObjectBooleanImmutablePair<EntityAttack>> thenAttackEntity(Entity entity) {
		return thenBasicInGameOperation(_ -> EntityAttack.scheduleAttack(entity));
	}

	public @NonNull InGameFuture<ObjectBooleanImmutablePair<EntityAttack>> thenAttackEntity(Function<T, Entity> fn) {
		return thenBasicInGameOperation(v -> EntityAttack.scheduleAttack(fn.apply(v)));
	}

	public @NonNull InGameFuture<EntityAttack> thenAttackEntityOrThrow(Entity entity) {
		return thenBasicInGameOperationOrThrow(_ -> EntityAttack.scheduleAttack(entity));
	}

	public @NonNull InGameFuture<EntityAttack> thenAttackEntityOrThrow(Function<T, Entity> fn) {
		return thenBasicInGameOperationOrThrow(v -> EntityAttack.scheduleAttack(fn.apply(v)));
	}

	public @NonNull InGameFuture<ObjectBooleanImmutablePair<EntityInteract>> thenInteractEntity(Entity entity, EntityInteract.Prepare prepare) {
		return thenBasicInGameOperation(_ -> EntityInteract.scheduleInteract(entity, prepare));
	}

	public @NonNull InGameFuture<ObjectBooleanImmutablePair<EntityInteract>> thenInteractEntity(Entity entity, InteractionHand hand) {
		return thenBasicInGameOperation(_ -> EntityInteract.scheduleInteract(entity, hand));
	}

	public @NonNull InGameFuture<EntityInteract> thenInteractEntityOrThrow(Entity entity, EntityInteract.Prepare prepare) {
		return thenBasicInGameOperationOrThrow(_ -> EntityInteract.scheduleInteract(entity, prepare));
	}

	public @NonNull InGameFuture<EntityInteract> thenInteractEntityOrThrow(Entity entity, InteractionHand hand) {
		return thenBasicInGameOperationOrThrow(_ -> EntityInteract.scheduleInteract(entity, hand));
	}

	public @NonNull InGameFuture<Void> thenAcceptInGameManager(Consumer<InGameManager> task) {
		return thenRunMCThread(()->task.accept(InGameManager.getOrThrow()));
	}

	public @NonNull InGameFuture<Void> thenAcceptInGameManager(BiConsumer<T, InGameManager> task) {
		return thenAcceptMCThread(v->task.accept(v, InGameManager.getOrThrow()));
	}

	public @NonNull <U> InGameFuture<U> thenApplyInGameManager(BiFunction<T, InGameManager, U> task) {
		return thenApplyMCThread(v->task.apply(v, InGameManager.getOrThrow()));
	}

	// 请自行在registrar执行完成/抛异常时调用res的complete/completeExceptionally()
	public @NotNull <U, V> InGameFuture<V> thenWaitEvent(IUnregistrableRegistryBase<?, U> registry, BiFunction<T, InGameFuture<V>, U> registrarGen) {
		InGameFuture<V> res = newIncompleteFuture();
		MutableObject<U> registrar = new MutableObject<>();
		thenAcceptMCThread(
			v->{
				registrar.setValue(registrarGen.apply(v, res));
				registry.register(registrar.get());
			}
		).exceptionallyMCThread(ex -> {
			U _registrar = registrar.get();
			if(_registrar != null) registry.unregister(_registrar);
			res.completeExceptionally(ex);
			return null;
		});
		return res.whenCompleteMCThread((_, _)->{
			if(registrar.get() instanceof U u)
				registry.unregister(u);
		});
	}

	public @NotNull <U> InGameFuture<U> thenWaitClientEndTick(BiFunction<T, Minecraft, U> action) {
		return thenWaitEvent(Registries.END_CLIENT_TICK, (v, f)->mc->{
			try {
				f.complete(action.apply(v, mc));
			} catch (Throwable ex) {
				f.completeExceptionally(ex);
			}
		});
	}

	public @NotNull InGameFuture<Void> thenWaitClientEndTick() {
		return thenWaitClientEndTick((_, _)-> null);
	}

	public @NotNull InGameFuture<Void> thenAcceptInGameManagerNextTick(Consumer<InGameManager> action) {
		return thenWaitClientEndTick((_, mc) -> {
			action.accept(InGameManager.getOrThrow(mc));
			return null;
		});
	}

	public @NonNull InGameFuture<T> orTimeOutTicks(int ticks, @NonNull Int2ObjectFunction<String> timeOutMessage) {
		class Subscriber implements ClientTickEvents.EndTick {
			final int initialTicks;
			int ticks;
			Subscriber(int ticks) { this.initialTicks = ticks; this.ticks = ticks; }
			@Override public void onEndTick(@NonNull Minecraft client) {
				if(ticks == 0) {
					completeExceptionally(new TimeoutException(timeOutMessage.apply(initialTicks)));
					Registries.END_CLIENT_TICK.unregister(this);
				}
				else if(!client.isPaused()) --ticks;
			}
		}
		if(ticks < 0) throw new IllegalArgumentException();
		var sub = new Subscriber(ticks);
		Registries.END_CLIENT_TICK.register(sub);
		return whenCompleteMCThread((_, _)->Registries.END_CLIENT_TICK.unregister(sub));
	}

	public @NonNull InGameFuture<T> orTimeOutTicks(int ticks) {
		return orTimeOutTicks(ticks, t -> Component.translatable("lpctools.utils.inGame.operation.timeout", t).getString());
	}

	// ── 静态工厂：镜像 CompletableFuture，返回 InGameFuture 保持链式类型 ──

	public static <U> @NonNull InGameFuture<U> completedFuture(U value) {
		InGameFuture<U> res = new InGameFuture<>();
		res.complete(value);
		return res;
	}

	public static <U> @NonNull InGameFuture<U> failedFuture(@NonNull Throwable ex) {
		InGameFuture<U> res = new InGameFuture<>();
		res.completeExceptionally(ex);
		return res;
	}

	public static @NonNull InGameFuture<Void> runAsync(@NonNull Runnable action) {
		return runAsync(action, ForkJoinPool.commonPool());
	}
	public static @NonNull InGameFuture<Void> runAsync(@NonNull Runnable action, @NonNull Executor executor) {
		InGameFuture<Void> res = new InGameFuture<>();
		CompletableFuture.runAsync(action, executor).whenComplete((_, ex) -> {
			if (ex != null) res.completeExceptionally(ex);
			else res.complete(null);
		});
		return res;
	}
	public static @NonNull InGameFuture<Void> runMCThread(@NonNull Runnable action) {
		return runAsync(action, mcExecutor());
	}

	public static <U> @NonNull InGameFuture<U> supplyAsync(@NonNull Supplier<U> supplier) {
		return supplyAsync(supplier, ForkJoinPool.commonPool());
	}
	public static <U> @NonNull InGameFuture<U> supplyAsync(@NonNull Supplier<U> supplier, @NonNull Executor executor) {
		InGameFuture<U> res = new InGameFuture<>();
		CompletableFuture.supplyAsync(supplier, executor).whenComplete((v, ex) -> {
			if (ex != null) res.completeExceptionally(ex);
			else res.complete(v);
		});
		return res;
	}
	public static <U> @NonNull InGameFuture<U> supplyMCThread(@NonNull Supplier<U> supplier) {
		return supplyAsync(supplier, mcExecutor());
	}

	public static @NonNull InGameFuture<Void> allOf(@NonNull CompletableFuture<?>... cfs) {
		InGameFuture<Void> res = new InGameFuture<>();
		CompletableFuture.allOf(cfs).whenComplete((_, ex) -> {
			if (ex != null) res.completeExceptionally(ex);
			else res.complete(null);
		});
		return res;
	}

	public static @NonNull InGameFuture<Object> anyOf(@NonNull CompletableFuture<?>... cfs) {
		InGameFuture<Object> res = new InGameFuture<>();
		CompletableFuture.anyOf(cfs).whenComplete((v, ex) -> {
			if (ex != null) res.completeExceptionally(ex);
			else res.complete(v);
		});
		return res;
	}

	public static <U> @NonNull InGameFuture<U> toInGameFuture(@NonNull CompletionStage<U> stage) {
		if (stage instanceof InGameFuture<U> igf) return igf;
		InGameFuture<U> res = new InGameFuture<>();
		stage.whenComplete((v, ex) -> {
			if (ex != null) res.completeExceptionally(ex);
			else res.complete(v);
		});
		return res;
	}

	public static @NonNull <U extends InGameOperationRunner.BasicOperation<U, ?, ?>>
	InGameFuture<ObjectBooleanImmutablePair<U>> basicInGameOperation(Supplier<? extends U> fn) {
		InGameFuture<ObjectBooleanImmutablePair<U>> res = new InGameFuture<>();
		supplyMCThread(() -> fn.get().appendOnResultCallback((instance, succeeded)->res.complete(new ObjectBooleanImmutablePair<>(instance, succeeded))))
			.exceptionally(ex -> { res.completeExceptionally(ex); return null; });
		return res;
	}

	public static @NonNull InGameFuture<ObjectBooleanImmutablePair<BlockBreaking>> breakBlock(BlockPos pos) {
		var fixedPos = pos.immutable();
		return basicInGameOperation(() -> BlockBreaking.scheduleBreak(fixedPos));
	}

	// 多参数的就不做Function版本了，自己去调thenBasicInGameOperation
	public static @NonNull InGameFuture<ObjectBooleanImmutablePair<BlockInteraction>> interactBlock(BlockPos pos, Direction direction, BlockInteraction.Prepare prepare) {
		return basicInGameOperation(() -> BlockInteraction.scheduleInteract(pos, direction, prepare));
	}

	public static @NonNull InGameFuture<ObjectBooleanImmutablePair<BlockInteraction>> interactBlock(BlockPos pos, Direction direction, InteractionHand hand) {
		return basicInGameOperation(() -> BlockInteraction.scheduleInteract(pos, direction, hand));
	}

	public static @NonNull InGameFuture<ObjectBooleanImmutablePair<BlockPlacing>> placeBlock(BlockPos pos, Predicate<ItemStack> restockTest, Predicate<BlockState> blockTest,
	                                                                                      InteractionHand preferredHand, boolean forcePreferredHand,
	                                                                                      @Nullable Direction requiredInteractDirection, @Nullable DirectionVectorPredicator requiredPlayerDirection) {
		return basicInGameOperation(() -> BlockPlacing.schedulePlace(pos, restockTest, blockTest, preferredHand, forcePreferredHand, requiredInteractDirection, requiredPlayerDirection));
	}

	public static @NonNull InGameFuture<ObjectBooleanImmutablePair<BlockPlacing>> placeBlock(BlockPos pos, Block targetBlock, InteractionHand preferredHand, boolean forcePreferredHand,
	                                                                                      @Nullable Direction requiredInteractDirection, @Nullable DirectionVectorPredicator requiredPlayerDirection) {
		return basicInGameOperation(() -> BlockPlacing.schedulePlace(pos, targetBlock, preferredHand, forcePreferredHand, requiredInteractDirection, requiredPlayerDirection));
	}

	public static @NonNull InGameFuture<ObjectBooleanImmutablePair<BlockPlacing>> placeBlock(BlockPos pos, Predicate<ItemStack> restockTest, Predicate<BlockState> blockTest, InteractionHand hand) {
		return basicInGameOperation(() -> BlockPlacing.schedulePlace(pos, restockTest, blockTest, hand));
	}

	public static @NonNull InGameFuture<ObjectBooleanImmutablePair<BlockPlacing>> placeBlock(BlockPos pos, Block targetBlock, InteractionHand hand) {
		return basicInGameOperation(() -> BlockPlacing.schedulePlace(pos, targetBlock, hand));
	}

	public static @NonNull InGameFuture<Void> acceptInGameManager(Consumer<InGameManager> task) {
		return runMCThread(()->task.accept(InGameManager.getOrThrow()));
	}

	@Override public int compareTo(@NonNull InGameFuture<?> o) { return Long.compare(id, o.id); }

	private static final class MCExecutor implements Executor {
		static final Executor instance = new MCExecutor(Minecraft.getInstance());
		final Minecraft mc;
		final ArrayDeque<Runnable> commands = new ArrayDeque<>();
		boolean isExecuting = false;
		MCExecutor(Minecraft mc) { this.mc = mc; }

		@Override public void execute(@NonNull Runnable command) {
			if (mc.isSameThread()) {
				commands.add(command);
				if(isExecuting) return;
				isExecuting = true;
				try {
					RuntimeException ex = null;
					while (commands.poll() instanceof Runnable runnable) {
						try {
							runnable.run();
						} catch (Throwable throwable) {
							if(ex == null) ex = new RuntimeException(throwable);
							else ex.addSuppressed(throwable);
						}
					}
					if(ex != null) throw ex;
				} finally {
					isExecuting = false;
				}
			}
			else mc.schedule(command);
		}

	}
	public static Executor mcExecutor() { return MCExecutor.instance; }
}
