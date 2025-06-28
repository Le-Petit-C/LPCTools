package lpctools.lpcfymasaapi.configbutton.derivedConfigs;

import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigList;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
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
    public ILPCConfigList addList(String nameKey){
        ILPCConfigList list = new LPCConfigList(getParent(), getNameKey() + '.' + nameKey);
        return super.addList(list);
    }
}
