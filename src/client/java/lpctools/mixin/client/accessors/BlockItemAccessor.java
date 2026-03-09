package lpctools.mixin.client.accessors;

import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BlockItem.class)
public interface BlockItemAccessor {
	@Invoker @Nullable BlockState invokeGetPlacementState(ItemPlacementContext context);
	@Invoker BlockState invokePlaceFromNbt(BlockPos pos, World world, ItemStack stack, BlockState state);
	@Invoker SoundEvent invokeGetPlaceSound(BlockState state);
}
