package lpctools.tools.leakPreventer;

import fi.dy.masa.malilib.util.data.Color4f;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.ColorConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.BooleanHotkeyThirdListConfig;
import lpctools.lpcfymasaapi.render.BlockOuterEdgeHighlightInstance;
import lpctools.tools.ToolConfigs;
import lpctools.tools.ToolUtils;
import lpctools.util.DataUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static lpctools.lpcfymasaapi.LPCConfigStatics.addColorConfig;
import static lpctools.lpcfymasaapi.LPCConfigStatics.listStack;

public class LeakPreventer {
	public static final BooleanHotkeyThirdListConfig LPConfig = new BooleanHotkeyThirdListConfig(ToolConfigs.toolConfigs, "LP", LeakPreventer::callback);
	static { ToolUtils.setLPCToolsToggleText(LPConfig); }
	static { listStack.push(LPConfig); }
	public static final ColorConfig markingColor = addColorConfig("markingColor", Color4f.fromColor(0x1fffffff), LeakPreventer::onColorChanged);
	static { listStack.pop(); }
	
	public static boolean testLeak(Level level, BlockPos pos) {
		if(highlightInstance == null) return false;
		BlockPos.MutableBlockPos cache = new BlockPos.MutableBlockPos();
		for(Direction d : testDirections) {
			if(level.getBlockState(cache.set(pos).move(d)).getFluidState().getAmount() != 0) {
				highlightInstance.mark(pos, color);
				return true;
			}
		}
		return false;
	}
	
	private static final Direction[] testDirections;
	static {
		ArrayList<Direction> directions = new ArrayList<>(List.of(Direction.values()));
		// directions.remove(Direction.DOWN);
		testDirections = directions.toArray(new Direction[0]);
	}
	
	private static @Nullable BlockOuterEdgeHighlightInstance highlightInstance = null;
	
	private static final MutableInt color = new MutableInt();
	
	static { onColorChanged(); }
	
	private static void callback() {
		if(LPConfig.getBooleanValue()) {
			if(highlightInstance == null)
				highlightInstance = new BlockOuterEdgeHighlightInstance();
		}
		else {
			if(highlightInstance != null)
				highlightInstance.close();
			highlightInstance = null;
		}
	}
	
	private static void onColorChanged() {
		color.setValue(DataUtils.swapRedBlue(markingColor.getIntegerValue()));
		if(highlightInstance != null) highlightInstance.reshapesAsync();
	}
}
