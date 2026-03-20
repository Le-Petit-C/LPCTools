package lpctools.debugs.ThreeBodyDisplay;

import lpctools.debugs.DebugConfigs;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.BooleanThirdListConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.UniqueDoubleConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.UniqueIntegerConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.Vector3dConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;

public class ThreeBodyDisplay {
	public static BooleanThirdListConfig threeBody = new BooleanThirdListConfig(DebugConfigs.debugs,
		"threeBody", false, ThreeBodyDisplay::mainCallback);
	static { listStack.push(threeBody); }
	private static final ILPCValueChangeCallback dataPackUpdater = consumeRunnerCallback(Runner::updateRandomizeDataPack);
	public static final Vector3dConfig massCenter = addConfigEx(l->new Vector3dConfig(l, "massCenter", new Vec3d(0, 0, 0), null));
	public static final UniqueIntegerConfig renderTrackCount = addConfigEx(l->new UniqueIntegerConfig(l, "renderTrackCount", 1200, 0, 65536, consumeRunnerCallback(Runner::updateTracks)));
	public static final UniqueDoubleConfig maxTrackSpeed = addConfigEx(l->new UniqueDoubleConfig(l, "maxTrackSpeed", 60, 0, Double.MAX_VALUE, dataPackUpdater));
	public static final UniqueDoubleConfig distanceLimit = addConfigEx(l->new UniqueDoubleConfig(l, "distanceLimit", 16, 0, Double.MAX_VALUE, dataPackUpdater));
	public static final UniqueDoubleConfig timeSpeed = addConfigEx(l->new UniqueDoubleConfig(l, "timeSpeed", 4, 0, Double.MAX_VALUE, dataPackUpdater));
	public static final UniqueDoubleConfig spreadRadius = addConfigEx(l->new UniqueDoubleConfig(l, "spreadRadius", 4, 0, Double.MAX_VALUE, dataPackUpdater));
	public static final UniqueDoubleConfig spreadSpeed = addConfigEx(l->new UniqueDoubleConfig(l, "spreadSpeed", 0.25, 0, Double.MAX_VALUE, dataPackUpdater));
	public static final UniqueDoubleConfig massDeviation = addConfigEx(l->new UniqueDoubleConfig(l, "massDeviation", 0.5, 0, Double.MAX_VALUE, dataPackUpdater));
	public static final UniqueDoubleConfig starRadiusFactor = addConfigEx(l->new UniqueDoubleConfig(l, "starRadiusFactor", 0.5, 0, Double.MAX_VALUE, dataPackUpdater));
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
