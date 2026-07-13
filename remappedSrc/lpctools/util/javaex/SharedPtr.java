package lpctools.util.javaex;

import org.jetbrains.annotations.Contract;

@SuppressWarnings("unused")
public class SharedPtr<T extends AutoCloseable>{
    public final T closeable;
    int refCount = 0;
    public SharedPtr(T closeable){this.closeable = closeable;}
    public SharedPtr<T> take(){++refCount; return this;}
    public void releaseNoexcept(){
        if(--refCount == 0){
            try {closeable.close();
            } catch (Exception ignored) {}
        }
    }
    public void release() throws Exception {if(--refCount == 0) closeable.close();}
    @Contract(pure = true) public T get(){return closeable;}
}
