package lpctools.lpcfymasaapi.configButtons.derivedConfigs;

import lpctools.generic.OperationSpeedLimit;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.DoubleConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.BooleanThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import org.jetbrains.annotations.NotNull;

import static lpctools.lpcfymasaapi.configButtons.derivedConfigs.DerivedConfigUtils.*;

public class LimitOperationSpeedConfig extends BooleanThirdListConfig {
    private final OperationSpeedLimit.ReplenishingLimit limit;
    public final @NotNull DoubleConfig maxOperationSpeed;
    public LimitOperationSpeedConfig(ILPCConfigReadable parent, OperationSpeedLimit limitParent, boolean defaultBoolean) {
        super(parent, "limitOperationSpeed", defaultBoolean, null);
        limit = limitParent.createSubReplenishing();
        maxOperationSpeed = addConfig(new DoubleConfig(this, "maxOperationSpeed", 1, 0, 64) {
            @Override public @NotNull String getFullTranslationKey() { return fullKeyByParent(this); }
            @Override public void onValueChanged() { updateReplenishingSpeed(); super.onValueChanged(); }
        });
        updateReplenishingSpeed();
    }

    public OperationSpeedLimit.ReplenishingLimit getLimit() { return limit; }

    private void updateReplenishingSpeed() { limit.setReplenishSpeed(getBooleanValue() ? maxOperationSpeed.getDoubleValue() : Double.POSITIVE_INFINITY); }

    @Override public @NotNull String getFullTranslationKey() { return fullKeyFromUtilBase(this); }
    @Override public void onValueChanged() { updateReplenishingSpeed(); super.onValueChanged(); }
}
