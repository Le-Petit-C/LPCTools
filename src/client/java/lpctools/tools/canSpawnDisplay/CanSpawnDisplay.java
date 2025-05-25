package lpctools.tools.canSpawnDisplay;

import com.mojang.blaze3d.systems.RenderSystem;
import fi.dy.masa.malilib.util.SubChunkPos;
import fi.dy.masa.malilib.util.Color4f;
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
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
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

import java.lang.Math;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static lpctools.generic.GenericUtils.mayMobSpawnAt;
import static lpctools.lpcfymasaapi.LPCConfigStatics.*;
import static lpctools.tools.ToolUtils.*;
import static lpctools.util.AlgorithmUtils.*;

@SuppressWarnings("deprecation")
public class CanSpawnDisplay implements WorldRenderEvents.Last, WorldRenderEvents.DebugRender, Registry.ClientWorldChunkLightUpdated, ClientChunkEvents.Unload, Registry.ClientWorldChunkSetBlockState, Registry.AfterClientWorldChange, ClientTickEvents.StartTick, GenericRegistry.SpawnConditionChanged {
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
    private final HashMap<SubChunkPos, HashSet<BlockPos>> canSpawnPoses = new HashMap<>();
    private static void putCanSpawnPoses(HashMap<SubChunkPos, HashSet<BlockPos>> map, BlockPos pos){
        map.computeIfAbsent(new SubChunkPos(pos), k->new HashSet<>()).add(pos.toImmutable());
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
            World world = entry.getValue();
            boolean maySpawn = mayMobSpawnAt(world, world.getLightingProvider(), pos);
            boolean maySpawnUp = mayMobSpawnAt(world, world.getLightingProvider(), pos.up());
            synchronized (canSpawnPoses){
                if(maySpawn) putCanSpawnPoses(canSpawnPoses, pos.down());
                else removeCanSpawnPoses(canSpawnPoses, pos.down());
                if(maySpawnUp) putCanSpawnPoses(canSpawnPoses, pos);
                else removeCanSpawnPoses(canSpawnPoses, pos);
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
        boolean b = true;
        for(Vector2i direction : directions){
            if(world.getChunk(pos.x + direction.x, pos.z + direction.y, ChunkStatus.FULL, false) == null) {
                b = false;
                break;
            }
        }
        Chunk chunk = world.getChunk(pos.x, pos.z, ChunkStatus.FULL, false);
        if(b && chunk != null) synchronized (tasks){
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
                if(newDistance < minDistance) chunk = task;
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
            for (int y = chunk.getBottomY() >> 4; y < yCeiling; ++y)
                canSpawnPoses.remove(new SubChunkPos(x, y, z));
        }
        if(!task.load) return;
        BlockPos regionStartPos = chunk.getPos().getStartPos().add(0, chunk.getBottomY(), 0);
        Iterable<BlockPos> iterableBox = iterateInBox(regionStartPos, regionStartPos.add(15, chunk.getHeight() - 1, 15));
        HashMap<SubChunkPos, HashSet<BlockPos>> result = new HashMap<>();
        for(BlockPos pos : iterableBox)
            if(mayMobSpawnAt(chunk, task.light, pos.up())) putCanSpawnPoses(result, pos);
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
        String getNameKey();
        BufferBuilder getShader(Tessellator tessellator, boolean xray);
        void vertex(BufferBuilder buffer, BlockPos pos, Vector3d cameraPos, int color, boolean xray);
    }
    private static final RenderMethod[] renderMethods = {
        new RenderMethod() {
            final Vector3d nn = new Vector3d();
            final Vector3d np = new Vector3d();
            final Vector3d pp = new Vector3d();
            final Vector3d pn = new Vector3d();
            @Override public String getNameKey() {
                return "minihudStyle";
            }
            @Override public BufferBuilder getShader(Tessellator tessellator, boolean xray) {
                return tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
            }
            @Override public void vertex(BufferBuilder buffer, BlockPos pos, Vector3d cp, int color, boolean xray) {
                double yOffset = xray ? 1 : 1.005;
                nn.set(pos.getX() + 0.1, pos.getY() + yOffset, pos.getZ() + 0.1).sub(cp);
                pn.set(pos.getX() + 0.9, pos.getY() + yOffset, pos.getZ() + 0.1).sub(cp);
                pp.set(pos.getX() + 0.9, pos.getY() + yOffset, pos.getZ() + 0.9).sub(cp);
                np.set(pos.getX() + 0.1, pos.getY() + yOffset, pos.getZ() + 0.9).sub(cp);
                buffer.vertex((float) nn.x, (float) nn.y, (float) nn.z).color(color);
                buffer.vertex((float) pn.x, (float) pn.y, (float) pn.z).color(color);
                buffer.vertex((float) pn.x, (float) pn.y, (float) pn.z).color(color);
                buffer.vertex((float) pp.x, (float) pp.y, (float) pp.z).color(color);
                buffer.vertex((float) pp.x, (float) pp.y, (float) pp.z).color(color);
                buffer.vertex((float) np.x, (float) np.y, (float) np.z).color(color);
                buffer.vertex((float) np.x, (float) np.y, (float) np.z).color(color);
                buffer.vertex((float) nn.x, (float) nn.y, (float) nn.z).color(color);
            }
        },
        new RenderMethod() {
            final Vector3d nn = new Vector3d();
            final Vector3d np = new Vector3d();
            final Vector3d pp = new Vector3d();
            final Vector3d pn = new Vector3d();
            @Override public String getNameKey() {
                return "fullSurface";
            }
            @Override public BufferBuilder getShader(Tessellator tessellator, boolean xray) {
                return tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            }
            @Override public void vertex(BufferBuilder buffer, BlockPos pos, Vector3d cp, int color, boolean xray) {
                double yOffset = xray ? 1 : 1.005;
                boolean direction = pos.getY() + yOffset < cp.y;
                nn.set(pos.getX(), pos.getY() + yOffset, pos.getZ()).sub(cp);
                np.set(pos.getX(), pos.getY() + yOffset, pos.getZ() + 1).sub(cp);
                pp.set(pos.getX() + 1, pos.getY() + yOffset, pos.getZ() + 1).sub(cp);
                pn.set(pos.getX() + 1, pos.getY() + yOffset, pos.getZ()).sub(cp);
                buffer.vertex((float) nn.x, (float) nn.y, (float) nn.z).color(color);
                if(direction) buffer.vertex((float) np.x, (float) np.y, (float) np.z).color(color);
                else buffer.vertex((float) pn.x, (float) pn.y, (float) pn.z).color(color);
                buffer.vertex((float) pp.x, (float) pp.y, (float) pp.z).color(color);
                if(direction) buffer.vertex((float) pn.x, (float) pn.y, (float) pn.z).color(color);
                else buffer.vertex((float) np.x, (float) np.y, (float) np.z).color(color);
            }
        },
        new RenderMethod() {
            final Vector3d nn = new Vector3d();
            final Vector3d np = new Vector3d();
            final Vector3d pp = new Vector3d();
            final Vector3d pn = new Vector3d();
            final Vector3d nn1 = new Vector3d();
            final Vector3d np1 = new Vector3d();
            final Vector3d pp1 = new Vector3d();
            final Vector3d pn1 = new Vector3d();
            @Override public String getNameKey() {
                return "lineCube";
            }
            @Override public BufferBuilder getShader(Tessellator tessellator, boolean xray) {
                return tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
            }
            @Override public void vertex(BufferBuilder buffer, BlockPos pos, Vector3d cp, int color, boolean xray) {
                nn.set(pos.getX() + 0.1, pos.getY() + 1.1, pos.getZ() + 0.1).sub(cp);
                pn.set(pos.getX() + 0.9, pos.getY() + 1.1, pos.getZ() + 0.1).sub(cp);
                pp.set(pos.getX() + 0.9, pos.getY() + 1.1, pos.getZ() + 0.9).sub(cp);
                np.set(pos.getX() + 0.1, pos.getY() + 1.1, pos.getZ() + 0.9).sub(cp);
                nn1.set(pos.getX() + 0.1, pos.getY() + 1.9, pos.getZ() + 0.1).sub(cp);
                pn1.set(pos.getX() + 0.9, pos.getY() + 1.9, pos.getZ() + 0.1).sub(cp);
                pp1.set(pos.getX() + 0.9, pos.getY() + 1.9, pos.getZ() + 0.9).sub(cp);
                np1.set(pos.getX() + 0.1, pos.getY() + 1.9, pos.getZ() + 0.9).sub(cp);
                buffer.vertex((float) nn.x, (float) nn.y, (float) nn.z).color(color);
                buffer.vertex((float) pn.x, (float) pn.y, (float) pn.z).color(color);
                buffer.vertex((float) pn.x, (float) pn.y, (float) pn.z).color(color);
                buffer.vertex((float) pp.x, (float) pp.y, (float) pp.z).color(color);
                buffer.vertex((float) pp.x, (float) pp.y, (float) pp.z).color(color);
                buffer.vertex((float) np.x, (float) np.y, (float) np.z).color(color);
                buffer.vertex((float) np.x, (float) np.y, (float) np.z).color(color);
                buffer.vertex((float) nn.x, (float) nn.y, (float) nn.z).color(color);
                buffer.vertex((float) nn1.x, (float) nn1.y, (float) nn1.z).color(color);
                buffer.vertex((float) pn1.x, (float) pn1.y, (float) pn1.z).color(color);
                buffer.vertex((float) pn1.x, (float) pn1.y, (float) pn1.z).color(color);
                buffer.vertex((float) pp1.x, (float) pp1.y, (float) pp1.z).color(color);
                buffer.vertex((float) pp1.x, (float) pp1.y, (float) pp1.z).color(color);
                buffer.vertex((float) np1.x, (float) np1.y, (float) np1.z).color(color);
                buffer.vertex((float) np1.x, (float) np1.y, (float) np1.z).color(color);
                buffer.vertex((float) nn1.x, (float) nn1.y, (float) nn1.z).color(color);
                buffer.vertex((float) nn.x, (float) nn.y, (float) nn.z).color(color);
                buffer.vertex((float) nn1.x, (float) nn1.y, (float) nn1.z).color(color);
                buffer.vertex((float) pn.x, (float) pn.y, (float) pn.z).color(color);
                buffer.vertex((float) pn1.x, (float) pn1.y, (float) pn1.z).color(color);
                buffer.vertex((float) pp.x, (float) pp.y, (float) pp.z).color(color);
                buffer.vertex((float) pp1.x, (float) pp1.y, (float) pp1.z).color(color);
                buffer.vertex((float) np.x, (float) np.y, (float) np.z).color(color);
                buffer.vertex((float) np1.x, (float) np1.y, (float) np1.z).color(color);
            }
        }
    };
    
    public void render(WorldRenderContext context) {
        RenderMethod method = renderMethod.get();
        boolean xray = renderXRays.getAsBoolean();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = method.getShader(tessellator, xray);
        Vec3d cameraPos = context.camera().getPos();
        Vector3d cp = new Vector3d(cameraPos.x, cameraPos.y, cameraPos.z);
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
        for (int ind : index) method.vertex(buffer, list.get(ind), cp, color, xray);
        if(xray) RenderSystem.disableDepthTest();
        else RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }
    @Override public void onLast(WorldRenderContext context) {
        if(renderXRays.getAsBoolean()) render(context);
    }
    @Override public void beforeDebugRender(WorldRenderContext context) {
        if(!renderXRays.getAsBoolean()) render(context);
    }
}
