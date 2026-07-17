package lpctools.debugs.ThreeBodyDisplay;

import fi.dy.masa.malilib.util.data.Color4f;
import lpctools.debugs.DebugConfigs;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.BooleanConfig;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.ColorConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.BooleanThirdListConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.UniqueDoubleConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.UniqueIntegerConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.Vector3dConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;

public class ThreeBodyDisplay {
	public static final BooleanThirdListConfig threeBody = new BooleanThirdListConfig(DebugConfigs.debugs,
		"threeBody", false, ThreeBodyDisplay::mainCallback);
	static { listStack.push(threeBody); }
	private static final ILPCValueChangeCallback dataPackUpdater = consumeRunnerCallback(Runner::updateRandomizeDataPack);
	public static final Vector3dConfig massCenter = addConfigEx(l->new Vector3dConfig(l, "massCenter", new Vec3(0, 0, 0), null));
	public static final UniqueIntegerConfig renderTrackCount = addConfigEx(l->new UniqueIntegerConfig(l, "renderTrackCount", 1200, 0, 65536, consumeRunnerCallback(Runner::updateTracks)));
	public static final UniqueDoubleConfig maxTrackSpeed = addConfigEx(l->new UniqueDoubleConfig(l, "maxTrackSpeed", 60, 0, Double.MAX_VALUE, dataPackUpdater));
	public static final UniqueDoubleConfig distanceLimit = addConfigEx(l->new UniqueDoubleConfig(l, "distanceLimit", 16, 0, Double.MAX_VALUE, dataPackUpdater));
	public static final UniqueDoubleConfig timeSpeed = addConfigEx(l->new UniqueDoubleConfig(l, "timeSpeed", 4, 0, Double.MAX_VALUE, dataPackUpdater));
	public static final UniqueDoubleConfig spreadRadius = addConfigEx(l->new UniqueDoubleConfig(l, "spreadRadius", 4, 0, Double.MAX_VALUE, dataPackUpdater));
	public static final UniqueDoubleConfig spreadSpeed = addConfigEx(l->new UniqueDoubleConfig(l, "spreadSpeed", 0.25, 0, Double.MAX_VALUE, dataPackUpdater));
	public static final UniqueDoubleConfig massDeviation = addConfigEx(l->new UniqueDoubleConfig(l, "massDeviation", 0.5, 0, Double.MAX_VALUE, dataPackUpdater));
	public static final UniqueDoubleConfig starRadiusFactor = addConfigEx(l->new UniqueDoubleConfig(l, "starRadiusFactor", 0.5, 0, Double.MAX_VALUE, null));
	public static final BooleanConfig renderStarProjection = addBooleanConfig("renderStarProjection", true);
	public static final BooleanConfig renderProjectionPlaneGrid = addBooleanConfig("renderProjectionPlaneGrid", true);
	public static final UniqueDoubleConfig projectionPlaneYOffset = addConfigEx(l->new UniqueDoubleConfig(l, "projectionPlaneYOffset", 0));
	public static final UniqueDoubleConfig projectionAlpha = addConfigEx(l->new UniqueDoubleConfig(l, "projectionAlpha", 0.5, 0, 1, null));
	public static final UniqueIntegerConfig gridSize = addConfigEx(l->new UniqueIntegerConfig(l, "gridSize", 16, 0, 65536, null));
	public static final UniqueDoubleConfig gridUnitLength = addConfigEx(l->new UniqueDoubleConfig(l, "gridUnitLength", 1));
	public static final ColorConfig gridColor = addColorConfig("gridColor", Color4f.fromColor(0x7fffffff));
	static { listStack.pop(); }
	private static @Nullable Runner runner;
	
	private static void consumeRunner(Consumer<Runner> consumer) {if(runner != null) consumer.accept(runner);}
	private static ILPCValueChangeCallback consumeRunnerCallback(Consumer<Runner> consumer) { return ()->consumeRunner(consumer); }
	
	private static void mainCallback(){
		if(threeBody.getBooleanValue()) {
			if(runner == null)
				runner = new Runner();
		}
		else {
			if(runner != null){
				runner.close();
				runner = null;
			}
		}
	}
	
}
