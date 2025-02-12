package lpctools.util;

public class ThreadInteger {
    public void waitUntilZero() throws InterruptedException{
        synchronized (this){
            if(value != 0) wait();
        }
    }
    public void add(int n){
        synchronized (this){
            value += n;
            if(value == 0) notifyAll();
        }
    }
    public void increase(){add(1);}
    public void decline(){add(-1);}
    private int value = 0;
}
