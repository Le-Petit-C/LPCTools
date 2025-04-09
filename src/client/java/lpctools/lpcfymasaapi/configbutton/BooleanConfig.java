package lpctools.lpcfymasaapi.configbutton;

import fi.dy.masa.malilib.config.options.ConfigBoolean;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

public class BooleanConfig extends LPCConfig<ConfigBoolean> implements BooleanSupplier, BooleanConsumer {
    public final boolean defaultBoolean;
    public BooleanConfig(@NotNull ILPCConfigList defaultParent, @NotNull String nameKey, boolean defaultBoolean){
        this(defaultParent, nameKey, defaultBoolean, null);
    }
    public BooleanConfig(@NotNull ILPCConfigList defaultParent, @NotNull String nameKey, boolean defaultBoolean, @Nullable IValueRefreshCallback callback){
        super(defaultParent, nameKey, false, callback);
        this.defaultBoolean = defaultBoolean;
    }
    @Override public boolean getAsBoolean() {
        return getInstance() != null ? getInstance().getBooleanValue() : defaultBoolean;
    }
    @Override public void accept(boolean b) {
        getConfig().setBooleanValue(b);
    }

    @Override @NotNull protected ConfigBoolean createInstance(){
        ConfigBoolean config = new ConfigBoolean(getNameKey(), defaultBoolean);
        config.setValueChangeCallback(new LPCConfigCallback<>(this));
        return config;
    }
}
