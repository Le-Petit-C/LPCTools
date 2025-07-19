package lpctools.tools.autoReconnect;

import lpctools.lpcfymasaapi.configButtons.transferredConfigs.DoubleConfig;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.IntegerConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.BooleanHotkeyThirdListConfig;
import lpctools.tools.ToolConfigs;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;

import java.util.Timer;
import java.util.TimerTask;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;
import static lpctools.tools.autoReconnect.AutoReconnectData.*;

public class AutoReconnect {
    public static final BooleanHotkeyThirdListConfig ARConfig = new BooleanHotkeyThirdListConfig(ToolConfigs.toolConfigs, "AR");
    static {listStack.push(ARConfig);}
    public static final DoubleConfig firstAttemptDelay = addDoubleConfig("firstAttemptDelay", 1, 0, 60);
    public static final DoubleConfig delayLinearFactor = addDoubleConfig("delayLinearFactor", 0, 0, 60);
    public static final DoubleConfig delayExpFactor = addDoubleConfig("delayExpFactor", 1.6180339887, 1, 10);
    public static final IntegerConfig maxAttemptTimes = addIntegerConfig("maxAttemptTimes", -1, -1, Integer.MAX_VALUE);
    static {listStack.pop();}
    public static void resetAttemptTimes(){
        attemptTimes = 0;
    }
    public static void cancelReconnect(){
        if(reconnectTask != null){
            reconnectTask.cancel();
            reconnectTask = null;
        }
    }
    private static void delayedDisconnect(ServerData serverData, double seconds){
        cancelReconnect();
        reconnectTask = new Timer();
        reconnectTask.schedule(new TimerTask() {
            @Override public void run() {
                serverData.mc().send(()->ConnectScreen.connect(null, serverData.mc(), serverData.address(), serverData.info(), serverData.quickPlay(), serverData.cookieStorage()));
            }
        }, (long)(seconds * 1000));
    }
    public static void disconnected(){
        if(ARConfig.getBooleanValue()){
            if(maxAttemptTimes.getAsInt() == attemptTimes) return;
            delayedDisconnect(lastServer, getNextAttemptDelay(attemptTimes));
            ++attemptTimes;
        }
    }
    private static double getNextAttemptDelay(int attemptTimes){
        return (firstAttemptDelay.getAsDouble() + delayLinearFactor.getAsDouble() * attemptTimes) * Math.pow(delayExpFactor.getAsDouble(), attemptTimes);
    }
}
