package lpctools.lpcfymasaapi.configbutton.derivedConfigs;

import lpctools.lpcfymasaapi.implementations.ILPCConfigList;
import lpctools.lpcfymasaapi.implementations.ILPCValueChangeCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

//并不是带着几个Int调整框的选项，只是强制每个选项都对应着一个User设置的Int功能类
//其实是给HotkeyConfig.IntegerChanger用的
public class IntegerArrayListConfig<T extends IntSupplier & IntConsumer>
        extends ArrayOptionListConfig<T> implements IntSupplier, IntConsumer{
    public IntegerArrayListConfig(@NotNull ILPCConfigList parent, @NotNull String nameKey, @NotNull Iterable<T> values) {
        this(parent, nameKey, values, null);
    }
    public IntegerArrayListConfig(@NotNull ILPCConfigList parent, @NotNull String nameKey, @NotNull Iterable<T> values, @Nullable ILPCValueChangeCallback callback) {
        super(parent, nameKey, callback);
        for(T value : values) addOption(value);
    }
    void addOption(T value){addOption(String.valueOf(value.getAsInt()), value);}

    @Override public int getAsInt() {return get().getAsInt();}
    @Override public void accept(int value) {
        get().accept(value);
    }
}
