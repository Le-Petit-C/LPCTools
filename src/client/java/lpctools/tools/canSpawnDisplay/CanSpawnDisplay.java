package lpctools.tools.canSpawnDisplay;

import com.mojang.blaze3d.buffers.BufferType;
import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.vertex.VertexFormat;
import fi.dy.masa.malilib.render.MaLiLibPipelines;
import fi.dy.masa.malilib.util.SubChunkPos;
import fi.dy.masa.malilib.util.data.Color4f;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import lpctools.compact.derived.ShapeList;
import lpctools.generic.GenericRegistry;
import lpctools.generic.GenericUtils;
import lpctools.lpcfymasaapi.Registry;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.ArrayOptionListConfig;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.RangeLimitConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.BooleanConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.BooleanHotkeyConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.ColorConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.DoubleConfig;
import lpctools.render.LPCExtraPipelines;
import lpctools.util.MathUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.*;
import org.lwjgl.system.MemoryUtil;

import java.lang.Math;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static lpctools.generic.GenericUtils.mayMobSpawnAt;
import static lpctools.lpcfymasaapi.LPCConfigStatics.*;
import static lpctools.tools.ToolUtils.*;
import static lpctools.util.AlgorithmUtils.*;

public class CanSpawnDisplay implements WorldRenderEvents.Last, WorldRenderEvents.DebugRender, Registry.ClientWorldChunkLightUpdated, ClientChunkEvents.Unload, Registry.ClientWorldChunkSetBlockState, ClientWorldEvents.AfterClientWorldChange, ClientTickEvents.StartTick, GenericRegistry.SpawnConditionChanged {
    public static BooleanHotkeyConfig canSpawnDisplay;
    public static ColorConfig displayColor;
    public static RangeLimitConfig rangeLimit;
    public static DoubleConfig renderDistance;
    public static ArrayOptionListConfig<RenderMethod> renderMethod;
    public static BooleanConfig renderXRays;
    public static void init(){
        canSpawnDisplay = addBooleanHotkeyConfig("canSpawnDisplay", false, null,
            ()->instance.onValueChanged(canSpawnDisplay.getAsBoolean()));
        setLPCToolsToggleText(canSpawnDisplay);
        displayColor = addColorConfig("displayColor", Color4f.fromColor(0x7fffffff));
        rangeLimit = addRangeLimitConfig(false);
        renderDistance = addDoubleConfig("renderDistance", 4, 1.5, Double.MAX_VALUE);
        renderMethod = addArrayOptionListConfig("renderMethod");
        for(RenderMethod method : renderMethods)
            renderMethod.addOption(renderMethod.getFullTranslationKey() + '.' + method.getNameKey(), method);
        renderXRays = addBooleanConfig("renderXRays", true);
    }
    private static final CanSpawnDisplay instance = new CanSpawnDisplay();
    public static CanSpawnDisplay getInstance(){return instance;}
    public void onValueChanged(boolean newValue){
        if(newValue) {
            if(Registry.registerWorldRenderLastCallback(this))
                addAllIntoWork();
            Registry.registerWorldRenderBeforeDebugRenderCallback(this);
            Registry.registerClientWorldChunkLightUpdatedCallback(this);
            Registry.registerClientChunkUnloadCallback(this);
            Registry.registerClientWorldChunkSetBlockStateCallback(this);
            Registry.registerClientWorldChangeCallback(this);
            Registry.registerStartClientTickCallback(this);
            GenericRegistry.SPAWN_CONDITION_CHANGED.register(this);
        }
        else{
            if(Registry.unregisterWorldRenderLastCallback(this))
                clearAll();
            Registry.unregisterWorldBeforeDebugRenderCallback(this);
            Registry.unregisterClientWorldChunkLightUpdatedCallback(this);
            Registry.unregisterClientChunkUnloadCallback(this);
            Registry.unregisterClientWorldChunkSetBlockStateCallback(this);
            Registry.unregisterClientWorldChangeCallback(this);
            Registry.unregisterStartClientTickCallback(this);
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
        CompletableFuture.allOf(futures);
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
    
    public interface RenderMethod{
        int getVertexBufferSizePerBlock();
        int getIndexBufferSizePerBlockByInt();
        String getNameKey();
        RenderPipeline getShader(boolean xray);
        void vertex(ByteBuffer indexBuffer, ByteBuffer vertexBuffer, BlockPos pos, int color, boolean xray);
    }
    private static final RenderMethod[] renderMethods = {
        new RenderMethod() {
            @Override public int getVertexBufferSizePerBlock(){return 64;}
            @Override public int getIndexBufferSizePerBlockByInt(){return 8;}
            @Override public String getNameKey() {
                return "minihudStyle";
            }
            @Override public RenderPipeline getShader(boolean xray) {
                if(xray) return MaLiLibPipelines.DEBUG_LINES_TRANSLUCENT_NO_DEPTH_NO_CULL;
                else return MaLiLibPipelines.DEBUG_LINES_TRANSLUCENT_NO_CULL;
            }
            @Override public void vertex(ByteBuffer indexBuffer, ByteBuffer vertexBuffer, BlockPos pos, int color, boolean xray) {
                double yOffset = xray ? 0 : 0.005;
                float y = (float) (pos.getY() + yOffset);
                float minX = (float) (pos.getX() + 0.1), maxX = (float) (pos.getX() + 0.9);
                float minZ = (float) (pos.getZ() + 0.1), maxZ = (float) (pos.getZ() + 0.9);
                int position = vertexBuffer.position() >>> 4;
                indexBuffer.putInt(position).putInt(position + 1);
                indexBuffer.putInt(position + 1).putInt(position + 2);
                indexBuffer.putInt(position + 2).putInt(position + 3);
                indexBuffer.putInt(position + 3).putInt(position);
                vertexBuffer.putFloat(minX).putFloat(y).putFloat(minZ).putInt(color);
                vertexBuffer.putFloat(maxX).putFloat(y).putFloat(minZ).putInt(color);
                vertexBuffer.putFloat(maxX).putFloat(y).putFloat(maxZ).putInt(color);
                vertexBuffer.putFloat(minX).putFloat(y).putFloat(maxZ).putInt(color);
            }
        },
        new RenderMethod() {
            @Override public int getVertexBufferSizePerBlock(){return 64;}
            @Override public int getIndexBufferSizePerBlockByInt(){return 6;}
            @Override public String getNameKey() {
                return "fullSurface";
            }
            @Override public RenderPipeline getShader(boolean xray) {
                if(xray) return MaLiLibPipelines.POSITION_COLOR_MASA_NO_DEPTH_NO_CULL;
                else return LPCExtraPipelines.POSITION_COLOR_MASA_NO_CULL;
            }
            @Override public void vertex(ByteBuffer indexBuffer, ByteBuffer vertexBuffer, BlockPos pos, int color, boolean xray) {
                double yOffset = xray ? 0 : 0.005;
                float y = (float) (pos.getY() + yOffset);
                float minX = pos.getX(), maxX = pos.getX() + 1;
                float minZ = pos.getZ(), maxZ = pos.getZ() + 1;
                int position = vertexBuffer.position() >>> 4;
                indexBuffer.putInt(position).putInt(position + 1).putInt(position + 2);
                indexBuffer.putInt(position).putInt(position + 2).putInt(position + 3);
                vertexBuffer.putFloat(minX).putFloat(y).putFloat(minZ).putInt(color);
                vertexBuffer.putFloat(maxX).putFloat(y).putFloat(minZ).putInt(color);
                vertexBuffer.putFloat(maxX).putFloat(y).putFloat(maxZ).putInt(color);
                vertexBuffer.putFloat(minX).putFloat(y).putFloat(maxZ).putInt(color);
            }
        },
        new RenderMethod() {
            @Override public int getVertexBufferSizePerBlock(){return 128;}
            @Override public int getIndexBufferSizePerBlockByInt(){return 24;}
            @Override public String getNameKey() {
                return "lineCube";
            }
            @Override public RenderPipeline getShader(boolean xray) {
                if(xray) return MaLiLibPipelines.DEBUG_LINES_TRANSLUCENT_NO_DEPTH_NO_CULL;
                else return MaLiLibPipelines.DEBUG_LINES_TRANSLUCENT;
            }
            @Override public void vertex(ByteBuffer indexBuffer, ByteBuffer vertexBuffer, BlockPos pos, int color, boolean xray) {
                float minX = (float) (pos.getX() + 0.1), maxX = (float) (pos.getX() + 0.9);
                float minY = (float) (pos.getY() + 0.1), maxY = (float) (pos.getY() + 0.9);
                float minZ = (float) (pos.getZ() + 0.1), maxZ = (float) (pos.getZ() + 0.9);
                int position = vertexBuffer.position() >>> 4;
                indexBuffer.putInt(position).putInt(position + 1);
                indexBuffer.putInt(position + 2).putInt(position + 3);
                indexBuffer.putInt(position + 4).putInt(position + 5);
                indexBuffer.putInt(position + 6).putInt(position + 7);
                indexBuffer.putInt(position).putInt(position + 2);
                indexBuffer.putInt(position + 1).putInt(position + 3);
                indexBuffer.putInt(position + 4).putInt(position + 6);
                indexBuffer.putInt(position + 5).putInt(position + 7);
                indexBuffer.putInt(position).putInt(position + 4);
                indexBuffer.putInt(position + 1).putInt(position + 5);
                indexBuffer.putInt(position + 2).putInt(position + 6);
                indexBuffer.putInt(position + 3).putInt(position + 7);
                vertexBuffer.putFloat(minX).putFloat(minY).putFloat(minZ).putInt(color);
                vertexBuffer.putFloat(maxX).putFloat(minY).putFloat(minZ).putInt(color);
                vertexBuffer.putFloat(minX).putFloat(maxY).putFloat(minZ).putInt(color);
                vertexBuffer.putFloat(maxX).putFloat(maxY).putFloat(minZ).putInt(color);
                vertexBuffer.putFloat(minX).putFloat(minY).putFloat(maxZ).putInt(color);
                vertexBuffer.putFloat(maxX).putFloat(minY).putFloat(maxZ).putInt(color);
                vertexBuffer.putFloat(minX).putFloat(maxY).putFloat(maxZ).putInt(color);
                vertexBuffer.putFloat(maxX).putFloat(maxY).putFloat(maxZ).putInt(color);
            }
        }
    };
    
    public void render(WorldRenderContext context) {
        RenderMethod method = renderMethod.get();
        boolean xray = renderXRays.getAsBoolean();
        Vec3d cameraPos = context.camera().getPos();
        int color = displayColor.getIntegerValue();
        ShapeList shapeList = rangeLimit.buildShapeList();
        double squaredRenderBlockDistance = renderDistance.getAsDouble() * renderDistance.getAsDouble() * 256;
        ArrayList<BlockPos> list = new ArrayList<>();
        DoubleArrayList distance = new DoubleArrayList();
        IntArrayList index = new IntArrayList();
        Vec3d cameraDiv16Pos = new Vec3d(cameraPos.x / 16, cameraPos.y / 16, cameraPos.z / 16);
        synchronized (canSpawnPoses){
            if(canSpawnPoses.isEmpty()) return;
            for(Vec3i vec : iterateFromClosestInDistance(cameraDiv16Pos, renderDistance.getAsDouble() + 0.866025403784439)){
                SubChunkPos chunkPos = new SubChunkPos(vec.getX(), vec.getY(), vec.getZ());
                HashSet<BlockPos> posSet = canSpawnPoses.get(chunkPos);
                if(posSet == null) continue;
                for(BlockPos pos : posSet){
                    double d = pos.getSquaredDistance(cameraPos);
                    if(d > squaredRenderBlockDistance) continue;
                    if(!shapeList.testPos(pos)) continue;
                    list.add(pos);
                    distance.add(d);
                    index.add(index.size());
                }
            }
        }
        if(index.isEmpty()) return;
        index.sort((o1, o2) -> (int) Math.signum(distance.getDouble(o2) - distance.getDouble(o1)));
        int blockCount = index.size();
        int indexCount = blockCount * method.getIndexBufferSizePerBlockByInt();
        ByteBuffer indexBuffer = MemoryUtil.memAlloc(indexCount * 4);
        ByteBuffer vertexBuffer = MemoryUtil.memAlloc(blockCount * method.getVertexBufferSizePerBlock());
        for (int ind : index) method.vertex(indexBuffer, vertexBuffer, list.get(ind), color, xray);
        indexBuffer.flip();
        vertexBuffer.flip();
        GpuBuffer gpuIndexBuffer = RenderSystem.getDevice()
            .createBuffer(null, BufferType.INDICES, BufferUsage.STATIC_WRITE, indexBuffer);
        GpuBuffer gpuVertexBuffer = RenderSystem.getDevice()
            .createBuffer(null, BufferType.VERTICES, BufferUsage.STATIC_WRITE, vertexBuffer);
        MemoryUtil.memFree(indexBuffer);
        MemoryUtil.memFree(vertexBuffer);
        Framebuffer framebuffer = MinecraftClient.getInstance().getFramebuffer();
        Matrix4fStack stack = RenderSystem.getModelViewStack();
        stack.pushMatrix();
        stack.mul(MathUtils.inverseOffsetMatrix4f(cameraPos.toVector3f()));
        GpuTexture gpuTexture;
        GpuTexture gpuTexture2;
        gpuTexture = framebuffer.getColorAttachment();
        gpuTexture2 = framebuffer.getDepthAttachment();
        try (RenderPass renderPass = RenderSystem.getDevice()
            .createCommandEncoder()
            .createRenderPass(gpuTexture, OptionalInt.empty(), gpuTexture2, OptionalDouble.empty())) {
            renderPass.setPipeline(method.getShader(xray));
            renderPass.setIndexBuffer(gpuIndexBuffer, VertexFormat.IndexType.INT);
            renderPass.setVertexBuffer(0, gpuVertexBuffer);
            renderPass.drawIndexed(0, indexCount);
        }
        stack.popMatrix();
        gpuIndexBuffer.close();
        gpuVertexBuffer.close();
    }
    @Override public void onLast(WorldRenderContext context) {
        if(renderXRays.getAsBoolean()) render(context);
    }
    @Override public void beforeDebugRender(WorldRenderContext context) {
        if(!renderXRays.getAsBoolean()) render(context);
    }
}
