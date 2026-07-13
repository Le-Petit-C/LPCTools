package lpctools.tools.canSpawnDisplay;

import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;

import java.util.function.Consumer;

public class CanSpawnDisplayData {
    public static final IRenderMethod[] renderMethods = {
        new MinihudStyleRenderMethod(),
        new FullSurfaceRenderMethod(),
        new LineCubeRenderMethod()
    };
    static DataInstance dataInstance;
    static void applyToDataInstance(Consumer<DataInstance> consumer) {
        if(dataInstance != null) consumer.accept(dataInstance);
    }
    static ILPCValueChangeCallback dataApplyCallback(Consumer<DataInstance> consumer) {
        return ()->applyToDataInstance(consumer);
    }
}
