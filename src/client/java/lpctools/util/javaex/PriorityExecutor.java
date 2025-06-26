package lpctools.util.javaex;

import org.jetbrains.annotations.NotNull;

public interface PriorityExecutor{
    void execute(@NotNull Runnable command, double priority);
}
