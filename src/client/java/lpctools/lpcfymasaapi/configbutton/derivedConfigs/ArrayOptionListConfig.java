package lpctools.lpcfymasaapi.configbutton.derivedConfigs;

import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import fi.dy.masa.malilib.util.StringUtils;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.OptionListConfig;
import lpctools.lpcfymasaapi.implementations.ILPCConfigList;
import lpctools.lpcfymasaapi.implementations.ILPCValueChangeCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.function.Supplier;

public class ArrayOptionListConfig<T> extends OptionListConfig implements Supplier<T> {
    public static class OptionList<T> extends ArrayList<OptionData<T>>{
        public void addOption(@NotNull String translationKey, @Nullable T userData){
            add(new OptionData<>(this, translationKey, userData, size()));
        }
    }
    public interface IArrayConfigOptionListEntry<T> extends IConfigOptionListEntry{
        OptionList<T> options();
        T userData();
    }
    public record OptionData<T>(@NotNull OptionList<T> options, @NotNull String translationKey, @Nullable T userData, int index) implements IArrayConfigOptionListEntry<T>{
        @Override public String getStringValue() {return translationKey;}
        @Override public String getDisplayName() {return StringUtils.translate(translationKey);}
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
    public record EmptyOptionData<T>(@NotNull OptionList<T> options) implements IArrayConfigOptionListEntry<T>{
        public static <T> EmptyOptionData<T> of(){return new EmptyOptionData<>(new OptionList<>());}
        @Override public String getStringValue() {return "";}
        @Override public String getDisplayName() {return "";}
        @Override public IConfigOptionListEntry cycle(boolean b) {return this;}
        @Override public IConfigOptionListEntry fromString(String value) {return null;}
        @Override public T userData() {return null;}
    }
    @SuppressWarnings("unchecked")
    @NotNull IArrayConfigOptionListEntry<T> getCurrentOptionData(){return ((IArrayConfigOptionListEntry<T>)getOptionListValue());}
    @Override public T get() {return getCurrentUserdata();}
    public T getCurrentUserdata(){return getCurrentOptionData().userData();}
    public ArrayOptionListConfig(@NotNull ILPCConfigList parent, @NotNull String nameKey) {
        this(parent, nameKey, null);
    }
    public ArrayOptionListConfig(@NotNull ILPCConfigList parent, @NotNull String nameKey, @Nullable ILPCValueChangeCallback callback) {
        super(parent, nameKey, EmptyOptionData.of(), callback);
    }
    public void addOption(@NotNull String translationKey, T userData){
        OptionList<T> list = getCurrentOptionData().options();
        list.addOption(translationKey, userData);
        if(list.size() == 1) setOptionListValue(list.getFirst());
    }
}
