package lpctools.lpcfymasaapi.configbutton.derivedConfigs;

import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.implementations.ILPCConfigList;
import lpctools.lpcfymasaapi.implementations.ILPCValueChangeCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//配置中单击可以切换列表的第三级列表
@SuppressWarnings("unused")
public class ConfigListOptionListConfig extends ConfigListOptionListConfigEx<ILPCConfigList> {
    public ConfigListOptionListConfig(@NotNull ILPCConfigList parent, @NotNull String nameKey) {
        this(parent, nameKey, null);
    }
    public ConfigListOptionListConfig(@NotNull ILPCConfigList parent, @NotNull String nameKey, @Nullable ILPCValueChangeCallback callback) {
        super(parent, nameKey, callback);
    }
    ILPCConfigList addList(String nameKey){
        ILPCConfigList list = new LPCConfigList(getParent(), getNameKey() + '.' + nameKey);
        return super.addList(list);
    }
}
