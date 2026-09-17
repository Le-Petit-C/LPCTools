package lpctools.mixin.client.accessors;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockStatePredictionHandler.class)
public interface BlockStatePredictionHandlerAccessor {
	@Accessor("currentSequenceNr") int getCurrentSequenceNr();
	@Accessor("currentSequenceNr") void setCurrentSequenceNr(int value);
	@Accessor("isPredicting") boolean getPredicting();
	@Accessor("isPredicting") void setPredicting(boolean value);
	/** 键是方块坐标，值是私有的 ServerVerifiedState，这里只当 Object 用（由调用方按需转换） */
	@Accessor("serverVerifiedStates") Long2ObjectOpenHashMap<?> getServerVerifiedStates();
}
