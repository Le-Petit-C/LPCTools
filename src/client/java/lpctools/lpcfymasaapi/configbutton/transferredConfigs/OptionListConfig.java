package lpctools.lpcfymasaapi.configbutton.transferredConfigs;

import com.google.gson.JsonElement;
import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import fi.dy.masa.malilib.config.options.ConfigOptionList;
import fi.dy.masa.malilib.util.StringUtils;
import lpctools.lpcfymasaapi.configbutton.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.function.Supplier;

public class OptionListConfig<T> extends ConfigOptionList implements ILPC_MASAConfigWrapper<ConfigOptionList>, IButtonDisplay, Supplier<T> {
    public record OptionData<T>(@NotNull ArrayList<OptionData<T>> options, @NotNull String translationKey, @Nullable T userData, int index) implements IConfigOptionListEntry{
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
    public static class OptionList<T> extends ArrayList<OptionData<T>>{
        public void addOption(@NotNull String translationKey, @Nullable T userData){
            add(new OptionData<>(this, translationKey, userData, size()));
        }
    }
    public OptionListConfig(@NotNull ILPCConfigList defaultParent, @NotNull String nameKey, @NotNull IConfigOptionListEntry defaultValue) {
        this(defaultParent, nameKey, defaultValue, null);
    }
    public OptionListConfig(@NotNull ILPCConfigList defaultParent, @NotNull String nameKey, @NotNull IConfigOptionListEntry defaultValue, @Nullable ILPCValueChangeCallback callback) {
        super(nameKey, defaultValue);
        data = new Data(defaultParent, false);
        ILPC_MASAConfigWrapperDefaultInit(callback);
    }
    public T getCurrentUserdata(){return getCurrentOptionData().userData;}
    @SuppressWarnings("unchecked")
    @NotNull OptionData<T> getCurrentOptionData(){return ((OptionData<T>)getOptionListValue());}

    @Override public void setValueFromJsonElement(JsonElement element) {
        String lastString = getStringValue();
        super.setValueFromJsonElement(element);
        if(!lastString.equals(getStringValue())) onValueChanged();
    }
    @Override @NotNull public String getDisplayName(){return getCurrentOptionData().getDisplayName();}
    @Override public T get(){return getCurrentUserdata();}
    @Override public @NotNull Data getLPCConfigData() {return data;}
    private final @NotNull Data data;
}
