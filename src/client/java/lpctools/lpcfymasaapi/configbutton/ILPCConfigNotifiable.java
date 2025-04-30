package lpctools.lpcfymasaapi.configbutton;

import org.jetbrains.annotations.Nullable;

public interface ILPCConfigNotifiable{
    //调用回调函数
    void onValueChanged();
    //设置回调函数
    void setValueChangeCallback(@Nullable ILPCValueChangeCallback callBack);
}
