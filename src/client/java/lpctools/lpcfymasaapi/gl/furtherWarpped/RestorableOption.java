package lpctools.lpcfymasaapi.gl.furtherWarpped;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.jetbrains.annotations.NotNull;

public interface RestorableOption {
    void push(@NotNull IntArrayList list);
    void pop(@NotNull IntArrayList list);
    interface EnableOption extends RestorableOption {
        void enable(boolean b);
        default void enable(){enable(true);}
        default void disable(){enable(false);}
    }
    interface SimpleEnableOption extends EnableOption {
        boolean isEnabled();
        @Override default void push(@NotNull IntArrayList list){list.add(isEnabled() ? 1 : 0);}
        @Override default void pop(@NotNull IntArrayList list){enable(list.removeLast() != 0);}
    }
}
