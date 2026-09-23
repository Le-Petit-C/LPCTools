package lpctools.mixinData;

import lpctools.util.GameTime;

public class MultiPlayerGameModeExtraData {
	protected long lastContinueBreakTick;

	public long getLastContinueBreakTick() { return lastContinueBreakTick; }
	public boolean continueBreakUpdatedThisTick() { return lastContinueBreakTick == GameTime.getClientTickCount(); }

	public static class Mutable extends MultiPlayerGameModeExtraData {
		public void setLastContinueBreakTick(long tick) { lastContinueBreakTick = tick; }
	}
	public interface Getter {
		MultiPlayerGameModeExtraData lpctools$getMultiPlayerGameModeExtraData();
	}
}
