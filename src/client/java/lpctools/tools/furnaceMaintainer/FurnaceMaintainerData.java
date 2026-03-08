package lpctools.tools.furnaceMaintainer;

import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class FurnaceMaintainerData {
    static @Nullable DataInstance dataInstance;
    static @Nullable FurnaceMaintainerRunner runner;
    static void applyToDataInstance(Consumer<DataInstance> consumer) {
        if(dataInstance != null) consumer.accept(dataInstance);
    }
    static ILPCValueChangeCallback applyToDataInstanceCallback(Consumer<DataInstance> consumer) {
        return ()->applyToDataInstance(consumer);
    }
}
