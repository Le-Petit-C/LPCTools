package lpctools.lpcfymasaapi.interfaces;

@SuppressWarnings("UnusedReturnValue")
public interface IUnregistrableRegistryBase<RUNNER, SUBSCRIBER> {
    boolean register(SUBSCRIBER callback, boolean register);
    boolean isEmpty();
    RUNNER runner();
    default boolean register(SUBSCRIBER callback){ return register(callback, true); }
    default boolean unregister(SUBSCRIBER callback){ return register(callback, false); }
}
