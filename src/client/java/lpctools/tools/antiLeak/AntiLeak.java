package lpctools.tools.antiLeak;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.BooleanHotkeyThirdListConfig;
import lpctools.lpcfymasaapi.render.BlockOuterEdgeHighlightInstance;
import lpctools.tools.ToolConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AntiLeak {
	public static final BooleanHotkeyThirdListConfig ALConfig = new BooleanHotkeyThirdListConfig(ToolConfigs.toolConfigs, "AL", AntiLeak::callback);
	
	public static boolean testLeak(Level level, BlockPos pos) {
		if(highlightInstance == null) return false;
		boolean willLeak = false;
		BlockPos.MutableBlockPos cache = new BlockPos.MutableBlockPos();
		for(Direction d : testDirections) {
			if(level.getBlockState(cache.set(pos).move(d)).getFluidState().getAmount() != 0) {
				willLeak = true;
				highlightInstance.mark(cache, color);
			}
		}
		return willLeak;
	}
	
	private static final MutableInt color = new MutableInt(0x1fffffff);
	private static final Direction[] testDirections;
	static {
		ArrayList<Direction> directions = new ArrayList<>(List.of(Direction.values()));
		// directions.remove(Direction.DOWN);
		testDirections = directions.toArray(new Direction[0]);
	}
	
	private static @Nullable BlockOuterEdgeHighlightInstance highlightInstance = null;
	
	private static void callback() {
		if(ALConfig.getBooleanValue()) {
			if(highlightInstance == null)
				highlightInstance = new BlockOuterEdgeHighlightInstance();
		}
		else {
			if(highlightInstance != null)
				highlightInstance.close();
			highlightInstance = null;
		}
	}
}
