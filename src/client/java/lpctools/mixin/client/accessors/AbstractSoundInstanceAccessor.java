package lpctools.mixin.client.accessors;

import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractSoundInstance.class)
public interface AbstractSoundInstanceAccessor {
	// 方法名必须避开目标类已有的同名方法（如 getPitch() / getVolume()）：Mixin 把访问器也走 mergeMethod，
	// 目标类存在同名同签名的方法时（本仓库没开 requireAnnotations），它会直接把原方法 remove 掉再插入访问器，
	// 而那两个原版实现是要乘音效自带采样值的（this.sound.getPitch().sample(random)）。
	@Accessor("pitch") float lpctools$getPitch();
	@Accessor("pitch") void lpctools$setPitch(float pitch);
}
