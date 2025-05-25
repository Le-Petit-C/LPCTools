package lpctools.lpcfymasaapi.implementations;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigNotifiable;
import org.jetbrains.annotations.Nullable;

public interface ILPC_MASAConfigWrapper<T extends IConfigBase> extends ILPCDefaultConfigStyle, IConfigNotifiable<T> {
    @Override default void setValueChangeCallback(@Nullable ILPCValueChangeCallback callBack){
        if(callBack != null) setValueChangeCallback(config->callBack.onValueChanged());
        else setValueChangeCallback(config->{});
    }
    //默认初始化，应在getLPCConfigData有效化后调用
    default void ILPC_MASAConfigWrapperDefaultInit(@Nullable ILPCValueChangeCallback callback){
        setValueChangeCallback(callback);
        setComment(getFullCommentTranslationKey());
    }
}
