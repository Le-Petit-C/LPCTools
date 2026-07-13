package lpctools.generic;

import lpctools.lpcfymasaapi.Registries;

public class UpdateCounter {
	private static int updateCount = 0;
	public static void reset() {
		int updateLimit = GenericConfigs.updateLimitPerFrame.getAsInt();
		if(updateCount > 0) updateCount = updateLimit;
		else updateCount += updateLimit;
		// 小于0重设为0感觉是一个可选功能，不一定要做
		if(updateCount < 0) updateCount = 0;
	}
	public static boolean reserves() { return updateCount > 0; }
	public static boolean isTired() { return updateCount <= 0; }
	public static boolean updated(int count) { return (updateCount -= count) > 0; }
	public static boolean updated() { return --updateCount > 0; }
	
	static {
		Registries.BETWEEN_RENDER_FRAMES.register(UpdateCounter::reset);
	}
}
