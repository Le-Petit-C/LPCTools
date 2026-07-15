package lpctools.tools.autoReconnect;

import lpctools.lpcfymasaapi.Registries;
import lpctools.mixin.client.accessors.MultiPlayerGameModeAccessor;
import lpctools.tools.ToolUtils;
import lpctools.util.javaex.QuietAutoCloseable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import org.jetbrains.annotations.Nullable;

import java.util.Timer;
import java.util.TimerTask;

import static lpctools.tools.autoReconnect.AutoReconnect.*;

public class AutoReconnectRunner implements QuietAutoCloseable, Registries.BeforeScreenChangeCallback, Registries.ScreenChangedCallback, ToolUtils.ToolRunner {
	int attemptTimes = 0;
	@Nullable Timer reconnectTimer;
	@Nullable TimerTask reconnectTask;
	@Nullable ServerData capturedServerData;

	AutoReconnectRunner() {
		tryCaptureServerData();
	}

	@Override public void registerAll(boolean b) {
		Registries.ON_SCREEN_CHANGED.register(this, b);
		Registries.BEFORE_SCREEN_CHANGE.register(this, b);
	}

	@Override public boolean beforeScreenChange(Screen newScreen) {
		if (newScreen == null) {
			cancelScheduledReconnect();
			attemptTimes = 0;
		}
		return false;
	}

	@Override public void onScreenChanged(Screen newScreen) {
		tryCaptureServerData();
		if(newScreen instanceof DisconnectedScreen) {
			if(maxAttemptTimes.getAsInt() == attemptTimes) return;
			delayedReconnect(getNextAttemptDelay(attemptTimes));
			++attemptTimes;
		}
	}

	private void delayedReconnect(double seconds){
		cancelScheduledReconnect();
		if(capturedServerData != null) {
			ServerData capturedServerData = this.capturedServerData;
			reconnectTask = new TimerTask() {
				@Override public void run() {
					Minecraft.getInstance().tell(
						() -> ConnectScreen.startConnecting(
							new TitleScreen(), Minecraft.getInstance(),
							ServerAddress.parseString(capturedServerData.ip),
							capturedServerData, false,
							null));
				}
			};
			if(reconnectTimer == null) reconnectTimer = new Timer();
			reconnectTimer.schedule(reconnectTask, (long)(seconds * 1000));
		}
	}

	private void cancelScheduledReconnect() {
		if(reconnectTask != null) {
			reconnectTask.cancel();
			reconnectTask = null;
		}
	}
	private static double getNextAttemptDelay(int attemptTimes) {
		return (firstAttemptDelay.getAsDouble() + delayLinearFactor.getAsDouble() * attemptTimes) * Math.pow(delayExpFactor.getAsDouble(), attemptTimes);
	}
	private void tryCaptureServerData() {
		Minecraft mc = Minecraft.getInstance();
		if(mc.gameMode instanceof MultiPlayerGameModeAccessor gameMode && gameMode.getConnection() instanceof ClientPacketListener listener)
			capturedServerData = listener.getServerData();
	}

	@Override public void close() {
		registerAll(false);
		if(reconnectTimer != null){
			reconnectTimer.cancel();
			reconnectTimer = null;
		}
	}
}
