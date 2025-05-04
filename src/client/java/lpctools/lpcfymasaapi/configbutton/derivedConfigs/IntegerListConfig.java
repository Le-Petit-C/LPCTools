package lpctools.lpcfymasaapi.configbutton.derivedConfigs;

import lpctools.lpcfymasaapi.configbutton.ILPCConfigList;
import lpctools.lpcfymasaapi.configbutton.ILPCValueChangeCallback;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.OptionListConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

//并不是带着几个Int调整框的选项，只是强制每个选项都对应着一个User设置的Int功能类
//其实是给HotkeyConfig.IntegerChanger用的
public class IntegerListConfig<T extends IntSupplier & IntConsumer>
        extends OptionListConfig<T> implements IntSupplier, IntConsumer{
    public IntegerListConfig(@NotNull ILPCConfigList defaultParent, @NotNull String nameKey, @NotNull Iterable<T> values) {
        this(defaultParent, nameKey, values, null);
    }
    public IntegerListConfig(@NotNull ILPCConfigList defaultParent, @NotNull String nameKey, @NotNull Iterable<T> values, @Nullable ILPCValueChangeCallback callback) {
        super(defaultParent, nameKey, buildOptionList(values).getFirst(), callback);
    }
    @Override public int getAsInt() {return get().getAsInt();}
    @Override public void accept(int value) {get().accept(value);}
    private static <T extends IntSupplier & IntConsumer> OptionList<T> buildOptionList(@NotNull Iterable<T> values){
        OptionList<T> list = new OptionList<>();
        for(T value : values) list.addOption(String.valueOf(value.getAsInt()), value);
        return list;
    }
}
