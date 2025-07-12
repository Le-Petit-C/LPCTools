package lpctools.lpcfymasaapi.configbutton.derivedConfigs;

import lpctools.lpcfymasaapi.configbutton.transferredConfigs.DoubleConfig;
import lpctools.lpcfymasaapi.configbutton.uniqueConfigs.BooleanThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.util.HandRestock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

import static lpctools.lpcfymasaapi.configbutton.derivedConfigs.DerivedConfigUtils.*;

@SuppressWarnings("unused")
public class LimitOperationSpeedConfig extends BooleanThirdListConfig {
    public final @NotNull DoubleConfig maxOperationSpeed;
    public LimitOperationSpeedConfig(ILPCConfigReadable parent, boolean defaultBoolean, double defaultDouble) {
        super(parent, "limitOperationSpeed", defaultBoolean, null);
        maxOperationSpeed = addConfig(new DoubleConfig(this, "maxOperationSpeed", defaultDouble, 0, 64){
            @Override public @NotNull String getFullTranslationKey() {return fullKeyByParent(this);}
        });
    }
    //重置剩余操作次数
    public void resetOperationTimes(){
        restocked = false;
        if(!getBooleanValue()){
            reservedTimes = Double.MAX_VALUE;
            return;
        }
        double t = maxOperationSpeed.getAsDouble();
        if(reservedTimes < 1) reservedTimes += t;
        else reservedTimes = t;
    }
    //检测是否可以进行一次操作
    public boolean hasNext(){
        return reservedTimes >= 1;
    }
    //检测是否可以进行一次操作，并且如果可以操作的话将剩余操作数减一
    public boolean next(){
        if(reservedTimes < 1) return false;
        --reservedTimes;
        return true;
    }
    //强制将剩余操作数减1
    public void forcedNext(){--reservedTimes;}
    //限制这一次的最大操作次数，但是仅限这一次，所以应该在重置剩余操作次数后调用。
    public void limitReservedTimes(int limit){
        if(reservedTimes > limit) reservedTimes = limit;
    }
    //一个合适的示例:适合Restock后限制操作次数为物品数量
    //自带Restocked测试，每重置一次剩余操作次数最多只能Restock一次，多次调用无效
    public void limitWithRestock(HandRestock.IRestockTest restockTest, int offhandPriority){
        if(restocked) return;
        restocked = true;
        int count = HandRestock.restock(restockTest, offhandPriority);
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if(player != null && player.isCreative()) return;
        limitReservedTimes(count);
    }
    //执行操作直到操作次数用完或者方法返回了SHOULD_BREAK
    public void operate(OperationRunnable runnable){
        while (hasNext()){
            switch (runnable.runOperation()){
                case OPERATED -> forcedNext();
                case SHOULD_BREAK -> {return;}
            }
        }
    }
    //执行遍历操作直到遍历完成，操作次数用完或者方法返回了SHOULD_BREAK
    public <T> void iterableOperate(Iterable<T> iterable, IterateOperationRunnable<T> runnable){
        if(!hasNext()) return;
        for(T obj : iterable){
            switch (runnable.runIterateOperation(obj)){
                case OPERATED -> forcedNext();
                case SHOULD_BREAK -> {return;}
            }
            if(!hasNext()) return;
        }
    }
    //执行遍历操作直到遍历完成，操作次数用完或者方法返回了SHOULD_BREAK
    public <T> void iteratorOperate(Iterator<T> iterator, IterateOperationRunnable<T> runnable){
        while(hasNext() && iterator.hasNext()){
            switch (runnable.runIterateOperation(iterator.next())){
                case OPERATED -> forcedNext();
                case SHOULD_BREAK -> {return;}
            }
        }
    }
    //操作结果类
    public enum OperationResult{
        OPERATED,//操作了一次
        NO_OPERATION,//没有操作
        SHOULD_BREAK//应当结束循环了
    }
    //执行操作调用的方法
    public interface OperationRunnable{ OperationResult runOperation();}
    public interface IterateOperationRunnable<T>{ OperationResult runIterateOperation(T obj);}

    @Override public @NotNull String getFullTranslationKey() {return fullKeyFromUtilBase(this);}

    private double reservedTimes = 0;
    private boolean restocked = false;
}
