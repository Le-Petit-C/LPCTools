package lpctools.mixinInterfaces.MASAMixins;

@SuppressWarnings("unused")
public interface MuteMASAConfigMinMaxMixin<T> {
    T lPCTools$setMax(T value);
    T lPCTools$setMin(T value);
    interface MuteMASAConfigMinMaxDouble{
        double lPCTools$setMax(double value);
        double lPCTools$setMin(double value);
    }
}
