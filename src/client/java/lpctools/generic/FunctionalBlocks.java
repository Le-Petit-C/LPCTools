package lpctools.generic;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.BlockListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

public class FunctionalBlocks extends BlockListConfig {
	public static final ImmutableList<Block> scannedFunctionalBlocks;
	FunctionalBlocks(@NotNull ILPCConfigReadable parent, String nameKey) {
		super(parent, nameKey, scannedFunctionalBlocks, null);
	}
	static {
		Object2BooleanOpenHashMap<Class<?>> cache = new Object2BooleanOpenHashMap<>();
		var builder = ImmutableList.<Block>builder();
		BuiltInRegistries.BLOCK.forEach(block -> {
			if(overridesUseMethod(block.getClass(), cache)) builder.add(block);
		});
		scannedFunctionalBlocks = builder.build();
	}

	// 沿继承链检查是否重写了 useItemOn / useWithoutItem（任意一层声明即为有功能）
	private static boolean overridesUseMethod(Class<?> blockClass, Object2BooleanOpenHashMap<Class<?>> cache) {
		if(cache.containsKey(blockClass)) return cache.getBoolean(blockClass);
		boolean res;
		if(blockClass == BlockBehaviour.class) res = false;
		else if(overrides(blockClass, "useItemOn",
			ItemStack.class, BlockState.class, Level.class, BlockPos.class, Player.class, InteractionHand.class, BlockHitResult.class))
			res = true;
		else if(overrides(blockClass, "useWithoutItem",
			BlockState.class, Level.class, BlockPos.class, Player.class, BlockHitResult.class))
			res = true;
		else res = overridesUseMethod(blockClass.getSuperclass(), cache);
		cache.put(blockClass, res);
		return res;
	}

	private static boolean overrides(Class<?> clazz, String methodName, Class<?>... paramTypes) {
		try {
			clazz.getDeclaredMethod(methodName, paramTypes);
			return true;
		} catch(NoSuchMethodException ignored) {}
		return false;
	}
}
