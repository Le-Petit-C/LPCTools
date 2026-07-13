package lpctools.mixin.client.accessors;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BlockItem.class)
public interface BlockItemAccessor {
	@Invoker @Nullable BlockState invokeGetPlacementState(BlockPlaceContext context);
	@Invoker BlockState invokeUpdateBlockStateFromTag(BlockPos pos, Level world, ItemStack stack, BlockState state);
	@Invoker SoundEvent invokeGetPlaceSound(BlockState state);
}
