package lpctools.lpcfymasaapi.configbutton;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigNotifiable;
import org.jetbrains.annotations.Nullable;

public interface ILPC_MASAConfigWrapper<T extends IConfigBase> extends ILPCConfig, IConfigNotifiable<T> {
    @Override default void setValueChangeCallback(@Nullable ILPCValueChangeCallback callBack){
        if(callBack != null) setValueChangeCallback(config->callBack.onValueChanged());
        else setValueChangeCallback(config->{});
    }
}
