package lpctools.util.javaex;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.*;
import java.util.function.DoubleSupplier;

@SuppressWarnings("unused")
public class PriorityThreadPoolExecutor extends ThreadPoolExecutor implements PriorityExecutor{
    public PriorityThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, @NotNull TimeUnit unit) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, newQueue());
    }
    public PriorityThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, @NotNull TimeUnit unit, @NotNull ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, newQueue(), threadFactory);
    }
    public PriorityThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, @NotNull TimeUnit unit, @NotNull RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, newQueue(), handler);
    }
    public PriorityThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, @NotNull TimeUnit unit, @NotNull ThreadFactory threadFactory, @NotNull RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, newQueue(), threadFactory, handler);
    }
    //priority越小视为优先级越高
    @Override public void execute(@NotNull Runnable command, double priority) {
        execute(new PriorityRunnable(command, priority));
    }
    private record PriorityRunnable(Runnable runnable, double priority) implements Runnable, DoubleSupplier{
        @Override public void run() {runnable.run();}
        @Override public double getAsDouble() {return priority;}
    }
    private static PriorityBlockingQueue<Runnable> newQueue(){
        return new PriorityBlockingQueue<>(11, PriorityThreadPoolExecutor::comparator);
    }
    private static int comparator(Runnable r1, Runnable r2){
        double p1 = (r1 instanceof DoubleSupplier d) ? d.getAsDouble() : 0;
        double p2 = (r2 instanceof DoubleSupplier d) ? d.getAsDouble() : 0;
        return Double.compare(p1, p2);
    }
}
