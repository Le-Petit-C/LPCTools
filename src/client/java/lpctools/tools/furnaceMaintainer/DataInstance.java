package lpctools.tools.furnaceMaintainer;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lpctools.generic.ChunkedTaskInstance;
import lpctools.lpcfymasaapi.Registries;
import lpctools.lpcfymasaapi.render.BlockOuterEdgeHighlightInstance;
import lpctools.util.AlgorithmUtils;
import lpctools.util.DataUtils;
import lpctools.util.Packed;
import lpctools.util.javaex.QuietAutoCloseable;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import static lpctools.tools.furnaceMaintainer.FurnaceMaintainerData.*;

public class DataInstance implements QuietAutoCloseable, Registries.ClientWorldChunkSetBlockState, ClientTickEvents.EndTick {
	final MutableInt color = new MutableInt();
	final BlockOuterEdgeHighlightInstance highlightInstance = new BlockOuterEdgeHighlightInstance();
	final ChunkedTaskInstance detectTasks = new ChunkedTaskInstance();
	
	public DataInstance() {
		refreshColor();
		refreshRenderXRays();
		refreshUseCullFace();
		registerAll(true);
	}
	
	public void setColor(int color) {
		this.color.setValue(color);
		highlightInstance.reshapesAsync();
	}
	public void refreshColor() { setColor(DataUtils.argb2agbr(FurnaceMaintainer.markingColor.getIntegerValue())); }
	public void setUseCullFace(boolean useCullFace) { highlightInstance.setUseCullFace(useCullFace); }
	public void refreshUseCullFace() { setUseCullFace(FurnaceMaintainer.useCullFace.getBooleanValue()); }
	public void setRenderXRays(boolean xRays) { highlightInstance.setRenderXRays(xRays); }
	public void refreshRenderXRays() { setRenderXRays(FurnaceMaintainer.renderXRays.getBooleanValue()); }
	
	public boolean isEmpty() {
		return highlightInstance.isEmpty() && detectTasks.isEmpty();
	}
	
	public void retestFurnaces() {
		boolean includesHopperAbove = FurnaceMaintainer.includesHopperAbove.getBooleanValue();
		detectTasks.clearTasks();
		highlightInstance.clearData();
		ClientLevel world = Minecraft.getInstance().level;
		if(world == null) return;
		for(var chunk : DataUtils.loadedChunks(world)) {
			long packedChunkPos = chunk.getPos().toLong();
			detectTasks.scheduleTask(packedChunkPos, callback->callback.task = ()->{
				var res = detectFurnace(chunk, includesHopperAbove);
				return ()->{
					highlightInstance.resetChunk(packedChunkPos, res);
					return ChunkedTaskInstance.CallbackStatus.CONTINUE;
				};
			});
		}
	}
	
	@Override public void onClientWorldChunkSetBlockState(LevelChunk chunk, BlockPos pos, @Nullable BlockState lastState, @Nullable BlockState newState) {
		if(lastState != null && lastState.getBlock() instanceof AbstractFurnaceBlock && (newState == null || !(newState.getBlock() instanceof AbstractFurnaceBlock))) {
			highlightInstance.mark(pos.asLong(), null);
			var upperPos = pos.above();
			if(!(chunk.getBlockState(upperPos).getBlock() instanceof AbstractFurnaceBlock))
				highlightInstance.mark(upperPos, null);
		}
		else if(lastState != null && lastState.getBlock() instanceof HopperBlock && (newState == null || !(newState.getBlock() instanceof HopperBlock)))
			highlightInstance.mark(pos.asLong(), null);
	}
	
	@Override public void onEndTick(@NonNull Minecraft client) {
		if(dataInstance != this || isEmpty()) {
			DataUtils.clientMessage(Component.translatable("lpctools.configs.tools.FM.markedBlocksCleared"), true);
			if(dataInstance == this) {
				if(runner != null) {
					runner.close();
					runner = null;
				}
				dataInstance = null;
			}
			close();
		}
	}
	
	@Override public void close() {
		registerAll(false);
		highlightInstance.close();
		detectTasks.close();
	}
	
	private void registerAll(boolean b) {
		Registries.CLIENT_WORLD_CHUNK_SET_BLOCK_STATE.register(this, b);
		Registries.END_CLIENT_TICK.register(this, b);
	}
	
	private Int2ObjectOpenHashMap<MutableInt> detectFurnace(ChunkAccess chunk, boolean includesHopperAbove){
		Int2ObjectOpenHashMap<MutableInt> result = new Int2ObjectOpenHashMap<>();
		for(BlockPos pos : AlgorithmUtils.iterateInBox(0, chunk.getMinY(), 0, 15, chunk.getMinY() + chunk.getHeight() - 1, 15)){
			if(chunk.getBlockState(pos).getBlock() instanceof AbstractFurnaceBlock) {
				result.put(Packed.ChunkLocal.pack(pos), color);
				if(includesHopperAbove) {
					var up = pos.above();
					var upperBlockState = chunk.getBlockState(up);
					if(upperBlockState.getBlock() instanceof HopperBlock) {
						var facing = upperBlockState.getValue(HopperBlock.FACING);
						if(facing == Direction.DOWN)
							result.put(Packed.ChunkLocal.pack(up), color);
					}
				}
			}
		}
		return result;
	}
}
