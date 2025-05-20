package lpctools.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static lpctools.util.MathUtils.*;

@SuppressWarnings("unused")
public class AlgorithmUtils {
    public static Iterable<BlockPos> iterateInManhattanDistance(BlockPos center, int distance){
        return new Iterable<>(){
            @Override public @NotNull Iterator<BlockPos> iterator() {
                return new Iterator<>() {
                    final BlockPos.Mutable currentPos = new BlockPos.Mutable(-distance - 1, 0, 0);
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
    //从近到远遍历方块
    public static class NearstIterator implements Iterator<BlockPos>{
        private final @NotNull Vec3d center;
        protected final PriorityQueue<BlockPos> poses;
        protected final HashSet<BlockPos> posSet;
        NearstIterator(@NotNull Vec3d center){
            this.center = center;
            poses = new PriorityQueue<>(
                    (o1, o2) -> {
                        double v = getDistance(o1) - getDistance(o2);
                        return v == 0 ? 0 : (v < 0 ? -1 : 1);
                    }
            );
            BlockPos pos = BlockPos.ofFloored(center);
            for (int x = 0; x < 2; ++x)
                for (int y = 0; y < 2; ++y)
                    for (int z = 0; z < 2; ++z)
                        poses.add(pos.add(x, y, z));
            posSet = new HashSet<>(poses);
        }
        public double getDistance(BlockPos pos) {
            return pos.getSquaredDistance(center);
        }
        public double getNextDistance(){
            assert !poses.isEmpty();
            return getDistance(poses.peek());
        }
        @Override public boolean hasNext() {return true;}
        @Override public BlockPos next() {
            assert !poses.isEmpty();
            BlockPos ret = poses.remove();
            posSet.remove(ret);
            for (Direction direction : Direction.values()) {
                BlockPos nextPos = ret.offset(direction);
                if (getDistance(nextPos) <= getDistance(ret)) continue;
                if (posSet.contains(nextPos)) continue;
                posSet.add(nextPos);
                poses.add(nextPos);
            }
            return ret;
        }
    }
    public static Iterable<BlockPos> iterateFromClosest(Vec3d center){
        return () -> new NearstIterator(center);
    }
    public static class InNearstIterator extends NearstIterator{
        public final double maxSquaredDistance;
        InNearstIterator(@NotNull Vec3d center, double maxDistance) {
            super(center);
            maxSquaredDistance = maxDistance * maxDistance;
        }
        @Override public boolean hasNext() {return getNextDistance() <= maxSquaredDistance;}
    }
    public static Iterable<BlockPos> iterateFromClosestInDistance(Vec3d center, double distance){
        return () -> new InNearstIterator(center, distance);
    }
    public static Iterable<BlockPos> iterateFromFurthestInDistance(Vec3d center, double distance){
        ArrayList<BlockPos> list = new ArrayList<>();
        for(BlockPos pos : iterateFromClosestInDistance(center, distance))
            list.add(pos.mutableCopy());
        Collections.reverse(list);
        return list;
    }
}
