package lpctools.lpcfymasaapi.configbutton;

import lpctools.lpcfymasaapi.LPCConfigList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

//并不是带着几个Int调整框的选项，只是强制每个选项都对应着一个User设置的Int功能类
//其实是给HotkeyConfig.IntegerChanger用的
public class IntegerListConfig<T extends IntSupplier & IntConsumer>
        extends OptionListConfig<T> implements IntSupplier, IntConsumer{
    public IntegerListConfig(@NotNull LPCConfigList list, @NotNull String nameKey) {super(list, nameKey);}
    public IntegerListConfig(@NotNull LPCConfigList list, @NotNull String nameKey, @Nullable IValueRefreshCallback callback) {
        super(list, nameKey, callback);
    }
    @Override public int getAsInt() {return getCurrentUserdata().getAsInt();}
    @Override public void accept(int value) {getCurrentUserdata().accept(value);}
}
