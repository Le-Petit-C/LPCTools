package lpctools.debugs;

import lpctools.lpcfymasaapi.configButtons.transferredConfigs.StringConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.ButtonThirdListConfig;
import lpctools.util.GameTime;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;

public class TimeTest {
	public static final ButtonThirdListConfig timeTest = new ButtonThirdListConfig(DebugConfigs.debugs, "timeTest", null);
	static {
		listStack.push(timeTest);
	}
	private static final StringConfig realTimeMillis = addStringConfig("realTimeMillis");
	private static final StringConfig gameTimeMillis = addStringConfig("gameTimeMillis");
	private static final StringConfig worldTicks = addStringConfig("worldTicks");
	private static final StringConfig dayTicks = addStringConfig("dayTicks");
	static {
		listStack.pop();
		timeTest.setListener((button, mouseButton)->{
			GameTime time = GameTime.ofCurrent();
			realTimeMillis.setValueFromString(String.valueOf(time.realTimeMillis));
			gameTimeMillis.setValueFromString(String.valueOf(time.gameTimeMillis));
			worldTicks.setValueFromString(String.valueOf(time.worldTicks));
			dayTicks.setValueFromString(String.valueOf(time.dayTicks));
			timeTest.getPage().markNeedUpdate();
		});
	}
}
