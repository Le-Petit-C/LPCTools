package lpctools.lpcfymasaapi.configbutton;

import fi.dy.masa.malilib.config.options.ConfigBoolean;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import lpctools.lpcfymasaapi.LPCConfigList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

public class BooleanConfig extends LPCConfig<ConfigBoolean> implements BooleanSupplier, BooleanConsumer {
    public final boolean defaultBoolean;
    public BooleanConfig(@NotNull LPCConfigList list,@NotNull String nameKey, boolean defaultBoolean){
        this(list, nameKey, defaultBoolean, null);
    }
    public BooleanConfig(@NotNull LPCConfigList list, @NotNull String nameKey, boolean defaultBoolean, @Nullable IValueRefreshCallback callback){
        super(list, nameKey, false, callback);
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
        config.apply(getList().getFullTranslationKey());
        config.setValueChangeCallback(new LPCConfigCallback<>(this));
        return config;
    }
}
