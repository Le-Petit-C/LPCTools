package lpctools.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import java.util.*;

import static lpctools.util.MathUtils.*;

@SuppressWarnings("unused")
public class AlgorithmUtils {
    //遍历长方体形状内的方块
    public static Iterable<BlockPos> iterateInBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ){
        return new InBoxIterable(minX, minY, minZ, maxX, maxY, maxZ);
    }
    public static Iterable<BlockPos> iterateInBox(BlockPos minPos, BlockPos maxPos){
        return new InBoxIterable(minPos.getX(), minPos.getY(), minPos.getZ(), maxPos.getX(), maxPos.getY(), maxPos.getZ());
    }
    //遍历曼哈顿距离内的方块
    public static Iterable<BlockPos> iterateInManhattanDistance(BlockPos center, int distance){
        return new ManhattanIterable(center, distance);
    }
    //从近到远遍历方块
    public static Iterable<BlockPos> iterateFromClosest(Vec3d center){
        return () -> new NearstIterator3D(center);
    }
    public static Iterable<BlockPos> iterateFromClosestInDistance(Vec3d center, double distance){
        return () -> new InNearstIterator3D(center, distance);
    }
    //从远到近遍历方块
    public static Iterable<BlockPos> iterateFromFurthestInDistance(Vec3d center, double distance){
        ArrayList<BlockPos> list = new ArrayList<>();
        for(BlockPos pos : iterateFromClosestInDistance(center, distance))
            list.add(pos.mutableCopy());
        Collections.reverse(list);
        return list;
    }
    //从近到远遍历格点
    public static Iterable<Vector2i> iterateFromClosest(Vector2i center){
        return () -> new NearstIterator2i(center);
    }
    public static Iterable<Vector2i> iterateFromClosestInDistance(Vector2i center, double distance){
        return () -> new InNearstIterator2i(center, distance);
    }
    //从远到近遍历格点
    public static ArrayList<Vector2i> iterateFromFurthestInDistance(Vector2i center, double distance){
        ArrayList<Vector2i> result = new ArrayList<>();
        for(Vector2i pos : iterateFromClosestInDistance(center, distance))
            result.add(pos);
        Collections.reverse(result);
        return result;
    }
    //数据中是否有null
    public static boolean hasNull(Object... objects){
        for(Object object : objects)
            if(object == null) return true;
        return false;
    }
    //只是转化而已
    public static Vector2i toVector2i(ChunkPos chunkPos){
        return new Vector2i(chunkPos.x, chunkPos.z);
    }
    public static ChunkPos toChunkPos(Vector2i vec){
        return new ChunkPos(vec.x, vec.y);
    }
    
    public record InBoxIterable(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) implements Iterable<BlockPos>{
        @Override public @NotNull Iterator<BlockPos> iterator() {
            return new Iterator<>() {
                private final BlockPos.Mutable mutable = new BlockPos.Mutable(maxX, maxY, minZ - 1);
                @Override public boolean hasNext() {
                    return mutable.getZ() < maxZ || mutable.getY() < maxY || mutable.getX() < maxX;
                }
                @Override public BlockPos next() {
                    mutable.setX(mutable.getX() + 1);
                    if(mutable.getX() > maxX){
                        mutable.setX(minX);
                        mutable.setY(mutable.getY() + 1);
                        if(mutable.getY() > maxY){
                            mutable.setY(minY);
                            mutable.setZ(mutable.getZ() + 1);
                        }
                    }
                    return mutable;
                }
            };
        }
    }
    
    public record ManhattanIterable(BlockPos center, int distance) implements Iterable<BlockPos>{
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
    }
    
    public static class NearstIterator3D implements Iterator<BlockPos>{
        private final @NotNull Vec3d center;
        protected final PriorityQueue<BlockPos> poses;
        protected final HashSet<BlockPos> posSet;
        NearstIterator3D(@NotNull Vec3d center){
            this.center = center;
            poses = new PriorityQueue<>(
                    (o1, o2) -> {
                        double v = getSquaredDistance(o1) - getSquaredDistance(o2);
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
        public double getSquaredDistance(BlockPos pos) {
            return pos.getSquaredDistance(center);
        }
        public double getNextDistance(){
            assert !poses.isEmpty();
            return getSquaredDistance(poses.peek());
        }
        @Override public boolean hasNext() {return true;}
        @Override public BlockPos next() {
            assert !poses.isEmpty();
            BlockPos ret = poses.remove();
            posSet.remove(ret);
            for (Direction direction : Direction.values()) {
                BlockPos nextPos = ret.offset(direction);
                if (getSquaredDistance(nextPos) <= getSquaredDistance(ret)) continue;
                if (posSet.contains(nextPos)) continue;
                posSet.add(nextPos);
                poses.add(nextPos);
            }
            return ret;
        }
    }
    public static class InNearstIterator3D extends NearstIterator3D {
        public final double maxSquaredDistance;
        InNearstIterator3D(@NotNull Vec3d center, double maxDistance) {
            super(center);
            maxSquaredDistance = maxDistance * maxDistance;
        }
        @Override public boolean hasNext() {return getNextDistance() <= maxSquaredDistance;}
    }
    public static class NearstIterator2i implements Iterator<Vector2i>{
        private final @NotNull Vector2i center;
        protected final PriorityQueue<Vector2i> poses;
        protected final HashSet<Vector2i> posSet;
        NearstIterator2i(@NotNull Vector2i center){
            this.center = center;
            poses = new PriorityQueue<>(
                (o1, o2) -> {
                    long v = getSquaredDistance(o1) - getSquaredDistance(o2);
                    return v == 0 ? 0 : (v < 0 ? -1 : 1);
                }
            );
            poses.add(center);
            posSet = new HashSet<>(poses);
        }
        public long getSquaredDistance(Vector2i pos) {
            return center.distanceSquared(pos);
        }
        public double getNextDistance(){
            assert !poses.isEmpty();
            return getSquaredDistance(poses.peek());
        }
        @Override public boolean hasNext() {return true;}
        @Override public Vector2i next() {
            assert !poses.isEmpty();
            Vector2i ret = poses.remove();
            posSet.remove(ret);
            for (Vector2i direction : directions) {
                Vector2i nextPos = new Vector2i(ret).add(direction);
                if (getSquaredDistance(nextPos) <= getSquaredDistance(ret)) continue;
                if (posSet.contains(nextPos)) continue;
                posSet.add(nextPos);
                poses.add(nextPos);
            }
            return ret;
        }
        Vector2i[] directions = {
            new Vector2i(1, 0),
            new Vector2i(0, 1),
            new Vector2i(-1, 0),
            new Vector2i(0, -1)};
    }
    public static class InNearstIterator2i extends NearstIterator2i {
        public final double maxSquaredDistance;
        InNearstIterator2i(@NotNull Vector2i center, double maxDistance) {
            super(center);
            maxSquaredDistance = maxDistance * maxDistance;
        }
        @Override public boolean hasNext() {return getNextDistance() <= maxSquaredDistance;}
    }
}
