package lpctools.mixinInterfaces.MASAMixins;

@SuppressWarnings("unused")
public interface MuteMASAConfigMinMaxMixin<T> {
    T lPCTools$setMax(T value);
    T lPCTools$setMin(T value);
    interface MuteMASAConfigMinMaxDouble{
        double lPCTools$setMin(double value);
        double lPCTools$setMax(double value);
    }
    interface MuteMASAConfigMinMaxInteger{
        int lPCTools$setMin(int value);
        int lPCTools$setMax(int value);
    }
}
