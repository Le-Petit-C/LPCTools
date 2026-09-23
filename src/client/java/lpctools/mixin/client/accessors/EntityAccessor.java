package lpctools.mixin.client.accessors;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityAccessor {
	@Accessor EntityDimensions getDimensions();
	/** 共享标志位那一个 byte（里面含猝翅滑翔的 flag7）；该方法体在 mixin 应用时会被替换掉 */
	@Accessor("DATA_SHARED_FLAGS_ID") static EntityDataAccessor<Byte> getSharedFlagsId() { throw new AssertionError(); }
}
