package lpctools.util;

import com.google.common.collect.ImmutableList;
import lpctools.util.javaex.NamedFunction;
import lpctools.util.javaex.NamedObject2BooleanFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector2i;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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
    //从近到远遍历已加载的区块
    public static Iterable<Chunk> iterateLoadedChunksFromClosest(World world, Vector2d center){
        return new Iterable<>() {
            @Override public @NotNull Iterator<Chunk> iterator() {
                return new Iterator<>() {
                    private final Vector2d compareCenter = center.mul(1.0 / 16, new Vector2d());
                    private double getSquaredDistance(ChunkPos pos){
                        double dx = (pos.x - compareCenter.x);
                        double dy = (pos.z - compareCenter.y);
                        return dx * dx + dy * dy;
                    }
                    private final HashSet<ChunkPos> remainingPoses = new HashSet<>();
                    private final PriorityQueue<Chunk> remainingChunks = initRemainingChunks();
                    private PriorityQueue<Chunk> initRemainingChunks(){
                        PriorityQueue<Chunk> ret = new PriorityQueue<>(Comparator.comparingDouble(v->getSquaredDistance(v.getPos())));
                        ChunkPos chunkPos = new ChunkPos((int)Math.floor(compareCenter.x + 0.5), (int)Math.floor(compareCenter.y + 0.5));
                        Chunk chunk;
                        chunk = world.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.FULL, false);
                        if(chunk != null) {ret.add(chunk);remainingPoses.add(chunk.getPos());}
                        chunk = world.getChunk(chunkPos.x + 1, chunkPos.z, ChunkStatus.FULL, false);
                        if(chunk != null) {ret.add(chunk);remainingPoses.add(chunk.getPos());}
                        chunk = world.getChunk(chunkPos.x, chunkPos.z + 1, ChunkStatus.FULL, false);
                        if(chunk != null) {ret.add(chunk);remainingPoses.add(chunk.getPos());}
                        chunk = world.getChunk(chunkPos.x + 1, chunkPos.z + 1, ChunkStatus.FULL, false);
                        if(chunk != null) {ret.add(chunk);remainingPoses.add(chunk.getPos());}
                        return ret;
                    }
                    @Override public boolean hasNext() {
                        return !remainingChunks.isEmpty();
                    }
                    @Override public Chunk next() {
                        Chunk chunk = remainingChunks.remove();
                        ChunkPos pos = chunk.getPos();
                        remainingPoses.remove(pos);
                        double distanceSquared = getSquaredDistance(pos);
                        ChunkPos pos1;
                        pos1 = new ChunkPos(pos.x - 1, pos.z);
                        if(getSquaredDistance(pos1) > distanceSquared && world.getChunk(pos1.x, pos1.z, ChunkStatus.FULL, false) instanceof Chunk chunk1 && remainingPoses.add(chunk1.getPos()))
                            remainingChunks.add(chunk1);
                        pos1 = new ChunkPos(pos.x + 1, pos.z);
                        if(getSquaredDistance(pos1) > distanceSquared && world.getChunk(pos1.x, pos1.z, ChunkStatus.FULL, false) instanceof Chunk chunk1 && remainingPoses.add(chunk1.getPos()))
                            remainingChunks.add(chunk1);
                        pos1 = new ChunkPos(pos.x, pos.z - 1);
                        if(getSquaredDistance(pos1) > distanceSquared && world.getChunk(pos1.x, pos1.z, ChunkStatus.FULL, false) instanceof Chunk chunk1 && remainingPoses.add(chunk1.getPos()))
                            remainingChunks.add(chunk1);
                        pos1 = new ChunkPos(pos.x, pos.z + 1);
                        if(getSquaredDistance(pos1) > distanceSquared && world.getChunk(pos1.x, pos1.z, ChunkStatus.FULL, false) instanceof Chunk chunk1 && remainingPoses.add(chunk1.getPos()))
                            remainingChunks.add(chunk1);
                        return chunk;
                    }
                };
            }
        };
    }
    public static Iterable<Chunk> iterateLoadedChunksFromClosest(World world, Vec3d center){
        return iterateLoadedChunksFromClosest(world, new Vector2d(center.x, center.z));
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
    //快速从ArrayList中移除内容，但是不保序
    public static <T> void fastRemove(ArrayList<T> source, NamedObject2BooleanFunction<T> shouldRemove){
        for(int a = 0; a < source.size(); ++a){
            while (shouldRemove.booleanApply(source.get(a))){
                Collections.swap(source, a, source.size() - 1);
                source.removeLast();
                if(a >= source.size()) break;
            }
        }
    }
    //软取消任务并阻塞等待剩余任务完成
    public static void cancelTask(@Nullable CompletableFuture<?> task){
        if(task == null) return;
        task.cancel(false);
        try{task.join();
        }catch (Exception ignored){}
    }
    //软取消元素关联的所有任务，清空原始集合并阻塞等待剩余任务完成
    public static <T> void cancelTasks(Collection<T> collection, NamedFunction<T, @Nullable CompletableFuture<?>> futureGetter){
        CompletableFuture<?>[] futures = new CompletableFuture<?>[collection.size()];
        int a = 0;
        for(T futureT : collection){
            CompletableFuture<?> future = futureGetter.apply(futureT);
            if(future == null) continue;
            future.cancel(false);
            futures[a++] = future;
        }
        collection.clear();
        try{CompletableFuture.allOf(futures).join();
        }catch (Exception ignored){}
    }
    public static <T> void cancelTasks(Collection<@Nullable CompletableFuture<T>> collection){
        CompletableFuture<?>[] futures = new CompletableFuture<?>[collection.size()];
        int a = 0;
        for(CompletableFuture<?> future : collection){
            if(future == null) continue;
            future.cancel(false);
            futures[a++] = future;
        }
        collection.clear();
        try{CompletableFuture.allOf(futures).join();
        }catch (Exception ignored){}
    }
    //处理并移除所有已完成的任务
    public static <T> void consumeCompletedTasks(Collection<CompletableFuture<T>> tasks, Consumer<T> consumer){
        ArrayList<CompletableFuture<T>> completedTasks = new ArrayList<>();
        for(CompletableFuture<T> task : tasks){
            if(!task.isDone()) continue;
            completedTasks.add(task);
            consumer.accept(task.join());
        }
        completedTasks.forEach(tasks::remove);
    }
    public static <T> void consumeCompletedTasks(ArrayList<CompletableFuture<T>> tasks, Consumer<T> consumer){
        HashSet<CompletableFuture<T>> completedTasks = new HashSet<>();
        for(CompletableFuture<T> task : tasks){
            if(!task.isDone()) continue;
            completedTasks.add(task);
            consumer.accept(task.join());
        }
        fastRemove(tasks, completedTasks::contains);
    }
    public static <T, U> void consumeCompletedTasks(Map<T, CompletableFuture<U>> tasks, BiConsumer<T, U> consumer){
        ArrayList<T> completedTasks = new ArrayList<>();
        tasks.forEach((t, task)->{
            if(!task.isDone()) return;
            completedTasks.add(t);
            consumer.accept(t, task.join());
        });
        completedTasks.forEach(tasks::remove);
    }
    public static <T extends AutoCloseable> void closeNoExcept(@NotNull Collection<T> closeableCollection){
        closeableCollection.forEach(AlgorithmUtils::closeNoExcept);
        closeableCollection.clear();
    }
    public static void closeNoExcept(@Nullable AutoCloseable closeable){
        if(closeable != null) try{closeable.close();}catch(Exception ignored){}
    }
    //转换集合内元素
    public static <T, U, V extends Collection<? super U>> @NotNull V convert(@NotNull V collection, @Nullable Iterable<T> values, NamedFunction<? super T, U> converter){
        if(values != null) values.forEach(value->collection.add(converter.apply(value)));
        return collection;
    }
    public static <T, U> @NotNull ImmutableList<U> convertToImmutableList(@Nullable Iterable<T> values, NamedFunction<? super T, U> converter){
        return ImmutableList.copyOf(convert(new ArrayList<>(), values, converter));
    }
    public static <T, U> @NotNull HashSet<U> convertToHashSet(@Nullable Iterable<T> values, NamedFunction<T, U> converter){
        return convert(new HashSet<>(), values, converter);
    }
}
