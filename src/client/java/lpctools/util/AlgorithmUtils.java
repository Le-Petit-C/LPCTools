package lpctools.util;

import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

import static lpctools.util.MathUtils.*;

public class AlgorithmUtils {
    public static Iterable<BlockPos> iterateInManhattanDistance(BlockPos center, int distance){
        return new Iterable<>(){
            @Override public @NotNull Iterator<BlockPos> iterator() {
                return new Iterator<>() {
                    final BlockPos.Mutable currentPos = new BlockPos.Mutable(-distance, 0, 0);
                    final BlockPos.Mutable returnPos = new BlockPos.Mutable();
                    @Override public boolean hasNext() {
                        return currentPos.getX() < distance;
                    }
                    @Override public BlockPos next() {
                        currentPos.setZ(currentPos.getZ() + 1);
                        if(getManhattanDistanceToZero(currentPos) > distance){
                            int lastX = currentPos.getX();
                            int lastY = currentPos.getY();
                            currentPos.setY(lastY + 1);
                            currentPos.setZ(Math.abs(lastY + 1) + Math.abs(lastX) - distance);
                            if(currentPos.getZ() > 0){
                                currentPos.setX(lastX + 1);
                                currentPos.setY(Math.abs(lastX + 1) - distance);
                                currentPos.setZ(0);
                            }
                        }
                        return returnPos.set(
                                center.getX() + currentPos.getX(),
                                center.getY() + currentPos.getY(),
                                center.getZ() + currentPos.getZ()
                        );
                    }
                };
            }
        };
    }
}
