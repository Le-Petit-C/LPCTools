package lpctools.lpcfymasaapi.configbutton.transferredConfigs;

import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import fi.dy.masa.malilib.config.options.ConfigOptionList;
import fi.dy.masa.malilib.util.StringUtils;
import lpctools.lpcfymasaapi.configbutton.IButtonDisplay;
import lpctools.lpcfymasaapi.configbutton.ILPCConfigList;
import lpctools.lpcfymasaapi.configbutton.IValueRefreshCallback;
import lpctools.lpcfymasaapi.configbutton.LPCConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class OptionListConfig<T> extends LPCConfig<ConfigOptionList> implements IButtonDisplay {
    public OptionListConfig(@NotNull ILPCConfigList defaultParent, @NotNull String nameKey) {
        this(defaultParent, nameKey, null);
    }
    public OptionListConfig(@NotNull ILPCConfigList defaultParent, @NotNull String nameKey, @Nullable IValueRefreshCallback callback) {
        super(defaultParent, nameKey, false);
        setCallback(callback);
    }
    //构造后应立即调用至少一次addOption
    public void addOption(@NotNull String translationKey, @Nullable T userData){
        options.add(new OptionData<>(options, translationKey, userData, options.size()));
    }
    public T getCurrentUserdata(){return getCurrentOptionData().userData;}
    @Override @NotNull public String getDisplayName(){return getCurrentOptionData().getDisplayName();}

    @Override protected @NotNull ConfigOptionList createInstance() {
        ConfigOptionList config = new ConfigOptionList(getNameKey(), options.isEmpty() ? null : options.getFirst());
        config.setValueChangeCallback(new LPCConfigCallback<>(this));
        return config;
    }

    @SuppressWarnings("unchecked")
    @NotNull OptionData<T> getCurrentOptionData(){
        ConfigOptionList instance = getInstance();
        if(instance == null) return options.getFirst();
        return ((OptionData<T>)instance.getOptionListValue());
    }
    @NotNull private final ArrayList<@NotNull OptionData<T>> options = new ArrayList<>();
    private record OptionData<T>(@NotNull ArrayList<OptionData<T>> options, @NotNull String translationKey, @Nullable T userData, int index) implements IConfigOptionListEntry{
        @Override public String getStringValue() {
            return translationKey;
        }
        @Override public String getDisplayName() {
            return StringUtils.translate(translationKey);
        }
        @Override public IConfigOptionListEntry cycle(boolean forward) {
            int n = index;
            if(forward){if(++n >= options.size()) n = 0;}
            else if(--n < 0) n = options.size() - 1;
            return options.get(n);
        }

        //感觉这是一个“半static”方法
        @Override public IConfigOptionListEntry fromString(String value) {
            for(OptionData<T> option : options)
                if(option.getStringValue().equals(value))
                    return option;
            return null;
        }
    }
}
