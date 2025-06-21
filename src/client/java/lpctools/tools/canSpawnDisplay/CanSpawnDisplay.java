package lpctools.tools.canSpawnDisplay;

import fi.dy.masa.malilib.util.SubChunkPos;
import fi.dy.masa.malilib.util.Color4f;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import lpctools.LPCTools;
import lpctools.compact.derived.ShapeList;
import lpctools.generic.GenericRegistry;
import lpctools.generic.GenericUtils;
import lpctools.lpcfymasaapi.Registries;
import lpctools.lpcfymasaapi.Registry;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.ArrayOptionListConfig;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.RangeLimitConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.BooleanConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.BooleanHotkeyConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.ColorConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.DoubleConfig;
import lpctools.lpcfymasaapi.gl.*;
import lpctools.lpcfymasaapi.gl.furtherWarpped.RenderBuffer;
import lpctools.shader.ShaderPrograms;
import lpctools.util.MathUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.*;
import org.lwjgl.system.MemoryUtil;

import java.lang.Math;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static lpctools.generic.GenericUtils.mayMobSpawnAt;
import static lpctools.lpcfymasaapi.LPCConfigStatics.*;
import static lpctools.tools.ToolUtils.*;
import static lpctools.util.AlgorithmUtils.*;

public class CanSpawnDisplay implements WorldRenderEvents.Last, WorldRenderEvents.DebugRender, WorldRenderEvents.Start, Registries.ClientWorldChunkLightUpdated, ClientChunkEvents.Unload, Registries.ClientWorldChunkSetBlockState, ClientWorldEvents.AfterClientWorldChange, ClientTickEvents.StartTick, GenericRegistry.SpawnConditionChanged, Registries.ScreenChangeCallback {
    public static BooleanHotkeyConfig canSpawnDisplay;
    public static ColorConfig displayColor;
    public static RangeLimitConfig rangeLimit;
    public static DoubleConfig renderDistance;
    public static ArrayOptionListConfig<IRenderMethod> renderMethod;
    public static BooleanConfig renderXRays;
    public static void init(){
        canSpawnDisplay = addBooleanHotkeyConfig("canSpawnDisplay", false, null,
            ()->instance.onValueChanged(canSpawnDisplay.getAsBoolean()));
        setLPCToolsToggleText(canSpawnDisplay);
        //noinspection deprecation
        displayColor = addColorConfig("displayColor", Color4f.fromColor(0x7fffffff));
        rangeLimit = addRangeLimitConfig(false);
        renderDistance = addDoubleConfig("renderDistance", 4, 1.5, 64);
        renderMethod = addArrayOptionListConfig("renderMethod");
        for(IRenderMethod method : renderMethods)
            renderMethod.addOption(renderMethod.getFullTranslationKey() + '.' + method.getNameKey(), method);
        renderXRays = addBooleanConfig("renderXRays", true);
    }
    private static final CanSpawnDisplay instance = new CanSpawnDisplay();
    public static CanSpawnDisplay getInstance(){return instance;}
    public void onValueChanged(boolean newValue){
        if(newValue) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if(dataInstance == null && mc.world != null && mc.player != null)
                dataInstance = new DataInstance(mc.world, mc.player.getPos());
            if(buffer == null){
                buffer = allocateBuffer();
                addAllIntoWork();
            }
            Registries.WORLD_RENDER_LAST.register(this);
            Registry.registerWorldRenderBeforeDebugRenderCallback(this);
            Registries.WORLD_RENDER_START.register(this);
            Registries.CLIENT_CHUNK_LIGHT_LOAD.register(this);
            Registry.registerClientChunkUnloadCallback(this);
            Registries.CLIENT_WORLD_CHUNK_SET_BLOCK_STATE.register(this);
            Registry.registerClientWorldChangeCallback(this);
            Registries.START_CLIENT_TICK.register(this);
            Registries.ON_SCREEN_CHANGED.register(this);
            GenericRegistry.SPAWN_CONDITION_CHANGED.register(this);
        }
        else{
            if(dataInstance != null){
                dataInstance.close();
                dataInstance = null;
            }
            if(buffer != null){
                clearAll();
                buffer.close();
                buffer = null;
            }
            Registries.WORLD_RENDER_LAST.unregister(this);
            Registry.unregisterWorldRenderBeforeDebugRenderCallback(this);
            Registries.WORLD_RENDER_START.unregister(this);
            Registries.CLIENT_CHUNK_LIGHT_LOAD.unregister(this);
            Registry.unregisterClientChunkUnloadCallback(this);
            Registries.CLIENT_WORLD_CHUNK_SET_BLOCK_STATE.unregister(this);
            Registry.unregisterClientWorldChangeCallback(this);
            Registries.START_CLIENT_TICK.unregister(this);
            Registries.ON_SCREEN_CHANGED.unregister(this);
            GenericRegistry.SPAWN_CONDITION_CHANGED.unregister(this);
        }
    }
    private static final Function<SubChunkPos, HashSet<BlockPos>> putCanSpawnPosesComputeFunction = k->new HashSet<>();
    private final HashMap<SubChunkPos, HashSet<BlockPos>> canSpawnPoses = new HashMap<>();
    private static void putCanSpawnPoses(HashMap<SubChunkPos, HashSet<BlockPos>> map, BlockPos pos){
        map.computeIfAbsent(new SubChunkPos(pos), putCanSpawnPosesComputeFunction).add(pos.toImmutable());
    }
    private static void removeCanSpawnPoses(HashMap<SubChunkPos, HashSet<BlockPos>> map, BlockPos pos){
        HashSet<BlockPos> set = map.get(new SubChunkPos(pos));
        if(set != null) set.remove(pos);
    }
    
    @Override public void onClientWorldChunkLightUpdated(WorldChunk chunk) {
        testAndAddChunkIntoWork(chunk.getWorld(), chunk.getPos());
        for(Vector2i direction : directions)
            testAndAddChunkIntoWork(chunk.getWorld(), toChunkPos(toVector2i(chunk.getPos()).add(direction)));
    }
    
    private final HashMap<BlockPos, World> needRefreshPoses = new HashMap<>();
    private static final int needRefreshPosesSizeLimit = 65536;
    @Override public void onClientWorldChunkSetBlockState(WorldChunk chunk, BlockPos pos, BlockState lastState, BlockState newState) {
        if(needRefreshPoses.size() >= needRefreshPosesSizeLimit) return;
        for(BlockPos pos1 : iterateInManhattanDistance(pos, 14)){
            if(!needRefreshPoses.containsKey(pos1))
                needRefreshPoses.put(pos1.toImmutable(), chunk.getWorld());
        }
    }
    @Override public void afterWorldChange(MinecraftClient minecraftClient, ClientWorld clientWorld) {
        clearAll();
    }
    @Override public void onStartTick(MinecraftClient mc) {
        if(needRefreshPoses.size() >= needRefreshPosesSizeLimit){
            needRefreshPoses.clear();
            clearAll();
            addAllIntoWork();
            return;
        }
        for(Map.Entry<BlockPos, World> entry : needRefreshPoses.entrySet()){
            BlockPos pos = entry.getKey();
            BlockPos up = pos.up();
            World world = entry.getValue();
            boolean maySpawn = mayMobSpawnAt(world, world.getLightingProvider(), pos);
            boolean maySpawnUp = mayMobSpawnAt(world, world.getLightingProvider(), pos.up());
            synchronized (canSpawnPoses){
                if(maySpawn) putCanSpawnPoses(canSpawnPoses, pos);
                else removeCanSpawnPoses(canSpawnPoses, pos);
                if(maySpawnUp) putCanSpawnPoses(canSpawnPoses, up);
                else removeCanSpawnPoses(canSpawnPoses, up);
            }
        }
        needRefreshPoses.clear();
    }
    @Override public void onSpawnConditionChanged() {
        clearAll();
        addAllIntoWork();
    }
    
    @Override
    public void onScreenChanged(Screen newScreen) {
        ShapeList list = rangeLimit.buildShapeList();
        if(list.equals(shapeList)) return;
        clearAll();
        shapeList = list;
        addAllIntoWork();
    }
    
    private record ThreadTask(@NotNull Chunk chunk, @Nullable LightingProvider light, boolean load){}
    private final Int2ObjectOpenHashMap<CompletableFuture<?>> tasks = new Int2ObjectOpenHashMap<>();
    private final HashSet<ThreadTask> chunkTasks = new HashSet<>();
    private int taskIndex = 0;
    private void clearAll(){
        CompletableFuture<?>[] futures;
        synchronized (tasks){
            futures = new CompletableFuture<?>[tasks.size()];
            int index = 0;
            for (CompletableFuture<?> future : tasks.values()){
                future.cancel(false);
                futures[index++] = future;
            }
            tasks.clear();
            chunkTasks.clear();
            taskIndex = 0;
        }
        for(CompletableFuture<?> future : futures){
            try{future.join();
            } catch (CancellationException ignored){}
        }
        synchronized (canSpawnPoses){
            canSpawnPoses.clear();
        }
    }
    private void addAllIntoWork(){
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        ClientWorld world = mc.world;
        if(player == null || world == null) return;
        for(Vector2i pos : iterateFromClosest(toVector2i(player.getChunkPos()))){
            if(!testAndAddChunkIntoWork(world, toChunkPos(pos))) break;
        }
    }
    private static final Vector2i[] directions = {
        new Vector2i(1, 0),
        new Vector2i(0, 1),
        new Vector2i(-1, 0),
        new Vector2i(0, -1),};
    private boolean testAndAddChunkIntoWork(World world, ChunkPos pos){
        Chunk chunk = world.getChunk(pos.x, pos.z, ChunkStatus.FULL, false);
        if(chunk != null) synchronized (tasks){
            chunkTasks.add(new ThreadTask(chunk, world.getLightingProvider(), true));
            int task = taskIndex++;
            tasks.put(task, GenericUtils.runAsync(()->prepareAndUpdateChunk(task)));
        }
        return chunk != null;
    }
    private void prepareAndUpdateChunk(int taskIndex){
        ThreadTask chunk = null;
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        ChunkPos center;
        if(player == null) center = ChunkPos.ORIGIN;
        else center = player.getChunkPos();
        int minDistance = Integer.MAX_VALUE;
        synchronized (tasks){
            tasks.remove(taskIndex);
            for (ThreadTask task : chunkTasks){
                int newDistance = task.chunk.getPos().getSquaredDistance(center);
                if(newDistance < minDistance) {
                    chunk = task;
                    minDistance = newDistance;
                }
            }
            chunkTasks.remove(chunk);
        }
        if(chunk != null) updateChunk(chunk);
    }
    private void updateChunk(ThreadTask task){
        Chunk chunk = task.chunk;
        int yCeiling = (chunk.getBottomY() + chunk.getHeight()) >> 4;
        int x = chunk.getPos().x, z = chunk.getPos().z;
        synchronized (canSpawnPoses) {
            for (int y = chunk.getBottomY() >> 4; y <= yCeiling; ++y)
                canSpawnPoses.remove(new SubChunkPos(x, y, z));
        }
        if(!task.load) return;
        BlockPos regionStartPos = chunk.getPos().getStartPos().add(0, chunk.getBottomY() + 1, 0);
        Iterable<BlockPos> iterableBox = iterateInBox(regionStartPos, regionStartPos.add(15, chunk.getHeight() - 1, 15));
        HashMap<SubChunkPos, HashSet<BlockPos>> result = new HashMap<>();
        for(BlockPos pos : iterableBox)
            if(mayMobSpawnAt(chunk, task.light, pos)) putCanSpawnPoses(result, pos);
        synchronized (canSpawnPoses){
            canSpawnPoses.putAll(result);
        }
    }
    @Override public void onChunkUnload(ClientWorld world, WorldChunk chunk) {
        synchronized (tasks){
            chunkTasks.add(new ThreadTask(chunk, world.getLightingProvider(), false));
            int task = taskIndex++;
            tasks.put(task, GenericUtils.runAsync(()->prepareAndUpdateChunk(task)));
        }
    }
    
    private static final IRenderMethod[] renderMethods = {
        new MinihudStyleRenderMethod(),
        new FullSurfaceRenderMethod(),
        new LineCubeRenderMethod()
    };
    
    CompletableFuture<CompletableFuture<?>> renderPrepareTask = null;
    IRenderMethod method = null;
    ByteBuffer indexBuffer = null;
    ByteBuffer vertexBuffer = null;
    Vec3d camPos = null;
    int thisRenderIndexCount;
    int lastBlockCount = 0;
    
    private static DataInstance dataInstance;
    private static final Function<CanSpawnDisplay, CompletableFuture<Void>> renderPrepareSupplier = instance->{
        CompletableFuture<MutableInt> finalTask = CompletableFuture.completedFuture(new MutableInt(0));
        int lastBlockCount = instance.lastBlockCount;
        MutableInt thisBlockCount = new MutableInt(0);
        IRenderMethod method = instance.method;
        int sizePerVertex = method.getVertexBufferSizePerVertex();
        int vertexPerBlock = method.getVertexCountPerBlock();
        int indexPerBlock = method.getIndexCountPerBlock();
        ByteBuffer indexBuffer = instance.indexBuffer =MemoryUtil.memAlloc(lastBlockCount * indexPerBlock * 4);
        ByteBuffer vertexBuffer = instance.vertexBuffer = MemoryUtil.memAlloc(lastBlockCount * vertexPerBlock * sizePerVertex);
        boolean xray = renderXRays.getAsBoolean();
        Vec3d cameraPos = instance.camPos;
        double squaredRenderBlockDistance = renderDistance.getAsDouble() * renderDistance.getAsDouble() * 256;
        Vec3d cameraDiv16Pos = new Vec3d(cameraPos.x / 16, cameraPos.y / 16, cameraPos.z / 16);
        for(Vec3i vec : iterateFromClosestInDistance(cameraDiv16Pos, renderDistance.getAsDouble() + 0.866025403784439)) {
            SubChunkPos chunkPos = new SubChunkPos(vec.getX(), vec.getY(), vec.getZ());
            ArrayList<BlockPos> posSet = dataInstance.canSpawnPoses.get(chunkPos);
            if(posSet == null) continue;
            CompletableFuture<ArrayList<BlockPos>> chunkTask = CompletableFuture.supplyAsync(
                ()->{
                    ArrayList<BlockPos> list = new ArrayList<>();
                    DoubleArrayList distance = new DoubleArrayList();
                    IntArrayList indexes = new IntArrayList();
                    for(BlockPos pos : posSet){
                        double d = pos.getSquaredDistance(cameraPos);
                        if(d > squaredRenderBlockDistance) continue;
                        if(!instance.shapeList.testPos(pos)) continue;
                        list.add(pos);
                        distance.add(d);
                        indexes.add(indexes.size());
                    }
                    indexes.sort((o1, o2) -> (int) Math.signum(distance.getDouble(o2) - distance.getDouble(o1)));
                    for(int n = 0; n < indexes.size(); ++n){
                        while(indexes.getInt(n) != n){
                            int thisIndex = indexes.getInt(n);
                            int thatIndex = indexes.getInt(thisIndex);
                            BlockPos thisIndexedBlockPos = list.get(thisIndex);
                            BlockPos thatIndexedBlockPos = list.get(thatIndex);
                            indexes.set(n, thatIndex);
                            indexes.set(thisIndex, thisIndex);
                            list.set(thisIndex, thatIndexedBlockPos);
                            list.set(thatIndex, thisIndexedBlockPos);
                        }
                    }
                    return list;
                }
            );
            finalTask = finalTask.thenCombine(chunkTask, (index, list)->{
                int blockCount = list.size();
                int usedBufferCount = thisBlockCount.getAndAdd(blockCount);
                int operatingIndex = index.getValue();
                for(BlockPos pos : list){
                    if(usedBufferCount >= lastBlockCount) break;
                    method.vertex(indexBuffer, vertexBuffer, pos, operatingIndex, xray);
                    ++usedBufferCount;operatingIndex += vertexPerBlock;
                }
                index.setValue(operatingIndex);
                return index;
            });
        }
        return finalTask.thenApply(index->{
            indexBuffer.flip();
            vertexBuffer.flip();
            instance.lastBlockCount = thisBlockCount.getValue();
            instance.thisRenderIndexCount = index.getValue() / vertexPerBlock * indexPerBlock;
            return null;
        });
    };
    
    public void renderPrepare(WorldRenderContext context){
        if(renderPrepareTask != null || indexBuffer != null || vertexBuffer != null){
            LPCTools.LOGGER.warn("CanSpawnDisplay: Last render not cleared");
            if(renderPrepareTask != null) renderPrepareTask.join();
            if(indexBuffer != null) MemoryUtil.memFree(indexBuffer);
            if(vertexBuffer != null) MemoryUtil.memFree(vertexBuffer);
            renderPrepareTask = null;
            indexBuffer = vertexBuffer = null;
        }
        method = renderMethod.get();
        camPos = context.camera().getPos();
        renderPrepareTask = CompletableFuture.supplyAsync(()->renderPrepareSupplier.apply(this));
    }
    
    public void render(WorldRenderContext context) {
        renderPrepareTask.join().join();renderPrepareTask = null;
        if (thisRenderIndexCount == 0) {
            MemoryUtil.memFree(indexBuffer);indexBuffer = null;
            MemoryUtil.memFree(vertexBuffer);vertexBuffer = null;
            return;
        }
        int count = indexBuffer.remaining() / 4;
        buffer.dataIndex(indexBuffer);
        buffer.dataVertex(vertexBuffer);
        MemoryUtil.memFree(indexBuffer);indexBuffer = null;
        MemoryUtil.memFree(vertexBuffer);vertexBuffer = null;
        Matrix4f finalMatrix = MathUtils.inverseOffsetMatrix4f(camPos.toVector3f());
        context.positionMatrix().mul(finalMatrix, finalMatrix);
        context.projectionMatrix().mul(finalMatrix, finalMatrix);
        buffer.program.setFinalMatrix(finalMatrix);
        buffer.program.setColor32(displayColor.getIntegerValue());
        try(MaskLayer layer = new MaskLayer()){
            layer.enableBlend().disableCullFace().enableDepthTest(!renderXRays.getAsBoolean());
            buffer.renderWithIndexes(method.getDrawMode(), count);
        }
    }
    @Override public void onLast(WorldRenderContext context) {
        if(renderXRays.getAsBoolean()) render(context);
    }
    @Override public void beforeDebugRender(WorldRenderContext context) {
        if(!renderXRays.getAsBoolean()) render(context);
    }
    @Override public void onStart(WorldRenderContext context) {
        renderPrepare(context);
    }
    private @NotNull ShapeList shapeList = ShapeList.emptyList();
    private RenderBuffer<ShaderPrograms.PositionStaticColorProgram> buffer = null;
    private RenderBuffer<ShaderPrograms.PositionStaticColorProgram> allocateBuffer(){
        return new RenderBuffer<>(Constants.BufferMode.DYNAMIC_DRAW, ShaderPrograms.POSITION_STATIC_COLOR_PROGRAM);
    }
}
