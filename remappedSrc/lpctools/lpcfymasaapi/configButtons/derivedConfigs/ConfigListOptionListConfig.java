package lpctools.lpcfymasaapi.configButtons.derivedConfigs;

import lpctools.lpcfymasaapi.interfaces.ILPCConfigList;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//配置中单击可以切换列表的第三级列表
public class ConfigListOptionListConfig extends ConfigListOptionListConfigEx<Object> {
    public ConfigListOptionListConfig(@NotNull ILPCConfigReadable parent, @NotNull String nameKey) {
        this(parent, nameKey, null);
    }
    public ConfigListOptionListConfig(@NotNull ILPCConfigReadable parent, @NotNull String nameKey, @Nullable ILPCValueChangeCallback callback) {
        super(parent, nameKey, callback);
    }
    @SuppressWarnings("unused")
    public ILPCConfigList addList(String nameKey){return super.addList(nameKey, null);}
}
