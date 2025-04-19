package lpctools.tools.SlightXRay;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import lpctools.lpcfymasaapi.Registry;
import lpctools.lpcfymasaapi.configbutton.IValueRefreshCallback;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.ThirdListConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.BooleanHotkeyConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.ColorConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.StringListConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.*;

import static lpctools.util.AlgorithmUtils.*;
import static lpctools.util.DataUtils.*;
import static lpctools.util.MathUtils.*;

public class SlightXRay implements IValueRefreshCallback, WorldRenderEvents.End, ClientChunkEvents.Load, ClientChunkEvents.Unload {
    //markedBlocks放在多线程里用，记得要同步
    static final @NotNull HashSet<BlockPos> markedBlocks = new HashSet<>();
    static final @NotNull HashSet<Block> XRayBlocks = initHashset();
    static final @NotNull ImmutableList<Block> defaultXRayBlocks = ImmutableList.of(Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE);
    static final @NotNull ImmutableList<String> defaultXRayBlockIds = idListFromBlockList(defaultXRayBlocks);
    public static BooleanHotkeyConfig slightXRay;
    public static ColorConfig displayColor;
    public static StringListConfig XRayBlocksConfig;

    public static void init(ThirdListConfig STConfig){
        slightXRay = STConfig.addBooleanHotkeyConfig("slightXRay", false, null, new SlightXRay());
        displayColor = STConfig.addColorConfig("displayColor", 0x7F3F7FFF);
        XRayBlocksConfig = STConfig.addStringListConfig("XRayBlocks", defaultXRayBlockIds, SlightXRay::refreshXRayBlocks);
    }

    private static void addAllRenderRegionsIntoWork(){
        ClientWorld world = MinecraftClient.getInstance().world;
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if(world == null || player == null) return;
        int distance = MinecraftClient.getInstance().options.getViewDistance().getValue();
        ChunkPos chunkPos = player.getChunkPos();
        for(int x = chunkPos.x - distance; x <= chunkPos.x + distance; ++x){
            for(int z = chunkPos.z - distance; z <= chunkPos.z + distance; ++z){
                if(world.isChunkLoaded(x, z)){
                    WorldChunk chunk = world.getChunk(x, z);
                    ChunkPos compPos = chunk.getPos();
                    if(compPos.x != x || compPos.z != z) continue;
                    updateChunkInAnotherThread(world, chunk, false);
                }
            }
        }
    }

    private static void refreshXRayBlocks(){
        XRayBlocks.clear();
        for(String str : XRayBlocksConfig.getStrings()){
            if(str.isEmpty() || str.isBlank()) continue;
            XRayBlocks.add(Registries.BLOCK.get(Identifier.of(str)));
        }
        if(slightXRay.getAsBoolean())
            addAllRenderRegionsIntoWork();
    }

    private static HashSet<Block> initHashset(){
        HashSet<Block> blocks = new HashSet<>();
        blocks.add(Blocks.DIAMOND_ORE);
        blocks.add(Blocks.DEEPSLATE_DIAMOND_ORE);
        return blocks;
    }

    @Override public void valueRefreshCallback() {
        if(slightXRay.getAsBoolean()){
            if(Registry.registerWorldRenderEndCallback(this))
                addAllRenderRegionsIntoWork();
            Registry.registerClientChunkLoadCallbacks(this);
            Registry.registerClientChunkUnloadCallbacks(this);
        }
        else {
            if(Registry.unregisterWorldRenderEndCallback(this)){
                synchronized (markedBlocks){
                    markedBlocks.clear();
                }
                synchronized (threadTasks){
                    threadTasks.clear();
                }
            }
            Registry.unregisterClientChunkLoadCallbacks(this);
            Registry.unregisterClientChunkUnloadCallbacks(this);
        }
    }

    @Override public void onEnd(WorldRenderContext context) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        Matrix4f matrix = worldToCameraMatrix(context.camera());
        int color = displayColor.getAsInt();
        synchronized (markedBlocks){
            if(markedBlocks.isEmpty()) return;
            for(BlockPos pos : markedBlocks)
                vertexBlock(matrix, buffer, pos, color);
        }
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }
    @Override public void onChunkLoad(ClientWorld clientWorld, WorldChunk worldChunk) {
        updateChunkInAnotherThread(clientWorld, worldChunk, false);
    }
    @Override public void onChunkUnload(ClientWorld clientWorld, WorldChunk worldChunk) {
        updateChunkInAnotherThread(clientWorld, worldChunk, true);
    }

    private enum XRayNecessaryState{
        F_F(false, false),
        F_T(false, true),
        T_F(true, false),
        T_T(true, true);
        public final boolean doShowAround, isXRayTarget;
        public static XRayNecessaryState of(BlockState state){
            return values()[(XRayBlocks.contains(state.getBlock()) ? 1 : 0)
                    + ((!state.isOpaque() || state.isTransparent()) && !(state.getBlock() == Blocks.VOID_AIR) ? 2 : 0)];
        }
        XRayNecessaryState(boolean doShowAround, boolean isXRayTarget){
            this.doShowAround = doShowAround;
            this.isXRayTarget = isXRayTarget;
        }
    }

    public static void setBlockStateTest(World world, BlockPos pos, BlockState lastState, BlockState currentState){
        if(lastState == null || currentState == null) return;
        if(XRayNecessaryState.of(lastState).doShowAround == XRayNecessaryState.of(currentState).doShowAround)
            return;
        boolean hasNear = false;
        for(Direction direction : Direction.values()){
            if(XRayNecessaryState.of(world.getBlockState(pos.offset(direction))).doShowAround){
                hasNear = true;
                break;
            }
        }
        if(!hasNear) return;
        if(XRayNecessaryState.of(lastState).doShowAround) testPos(pos, currentState);
        else SlightXRay.markNears(world, pos);
    }

    private static void testPos(BlockPos pos, BlockState state){
        synchronized (markedBlocks){
            if(XRayNecessaryState.of(state).isXRayTarget)
                markedBlocks.add(pos.mutableCopy());
            else markedBlocks.remove(pos);
        }
    }

    private static void markNears(World world, BlockPos center){
        for(BlockPos pos : iterateInManhattanDistance(center, 2))
            testPos(pos, world.getBlockState(pos));
    }

    //TODO:跨纬度处理之类的
    private static void updateChunk(ClientWorld world, WorldChunk chunk, boolean unload){
        ChunkPos chunkPos = chunk.getPos();
        int cx = chunkPos.x, cz = chunkPos.z;
        if(unload || !world.isChunkLoaded(chunk.getPos().x, chunk.getPos().z)
        || MinecraftClient.getInstance().world == null){
            synchronized (markedBlocks){
                Iterator<BlockPos> posIterator = markedBlocks.iterator();
                while(posIterator.hasNext()){
                    BlockPos pos = posIterator.next();
                    int sx = pos.getX() - (cx << 4);
                    int sz = pos.getZ() - (cz << 4);
                    if(sx >= 0 && sz >= 0 && sx < 16 && sz < 16)
                        posIterator.remove();
                }
            }
            return;
        }
        int minY = world.getBottomY();
        int numY = world.getHeight();
        int topY = minY + numY;

        //states用于存放预处理后的数据，向外拓展了一格处理相邻区块的内容，再向外拓展了一格防止越界
        XRayNecessaryState[][][] states = new XRayNecessaryState[20][numY + 4][20];
        //初始化states数据
        for(XRayNecessaryState[][] states1 : states){
            for(int y = 0; y < states1.length; ++y){
                XRayNecessaryState[] states2 = states1[y];
                if(world.isOutOfHeightLimit(y))
                    Arrays.fill(states2, XRayNecessaryState.of(Blocks.AIR.getDefaultState()));
                else Arrays.fill(states2, XRayNecessaryState.F_F);
            }
        }
        //加载本区块中数据
        BlockPos.Mutable mutableBlockPos = new BlockPos.Mutable();
        for(int x = 0; x < 16; ++x){
            mutableBlockPos.setX(x);
            XRayNecessaryState[][] states1 = states[x + 2];
            for(int y = minY; y < topY; ++y){
                mutableBlockPos.setY(y);
                XRayNecessaryState[] states2 = states1[y - minY + 2];
                for(int z = 0; z < 16; ++z){
                    mutableBlockPos.setZ(z);
                    states2[z + 2] = XRayNecessaryState.of(chunk.getBlockState(mutableBlockPos));
                }
            }
        }
        //加载相邻区块中数据
        WorldChunk nxChunk = world.isChunkLoaded(cx - 1, cz) ? world.getChunk(cx - 1, cz) : null;
        WorldChunk pxChunk = world.isChunkLoaded(cx + 1, cz) ? world.getChunk(cx + 1, cz) : null;
        WorldChunk nzChunk = world.isChunkLoaded(cx, cz - 1) ? world.getChunk(cx, cz - 1) : null;
        WorldChunk pzChunk = world.isChunkLoaded(cx, cz + 1) ? world.getChunk(cx, cz + 1) : null;
        if(nxChunk != null){
            mutableBlockPos.setX(-1);
            for(int y = minY; y < topY; ++y){
                mutableBlockPos.setY(y);
                for(int z = 0; z < 16; ++z){
                    mutableBlockPos.setZ(z);
                    states[1][y - minY + 2][z + 2] = XRayNecessaryState.of(nxChunk.getBlockState(mutableBlockPos));
                }
            }
        }
        if(pxChunk != null){
            mutableBlockPos.setX(16);
            for(int y = minY; y < topY; ++y){
                mutableBlockPos.setY(y);
                for(int z = 0; z < 16; ++z){
                    mutableBlockPos.setZ(z);
                    states[18][y - minY + 2][z + 2] = XRayNecessaryState.of(pxChunk.getBlockState(mutableBlockPos));
                }
            }
        }
        if(nzChunk != null){
            mutableBlockPos.setZ(-1);
            for(int y = minY; y < topY; ++y){
                mutableBlockPos.setY(y);
                for(int x = 0; x < 16; ++x){
                    mutableBlockPos.setX(x);
                    states[x + 2][y - minY + 2][1] = XRayNecessaryState.of(nzChunk.getBlockState(mutableBlockPos));
                }
            }
        }
        if(pzChunk != null){
            mutableBlockPos.setZ(16);
            for(int y = minY; y < topY; ++y){
                mutableBlockPos.setY(y);
                for(int x = 0; x < 16; ++x){
                    mutableBlockPos.setX(x);
                    states[x + 2][y - minY + 2][18] = XRayNecessaryState.of(pzChunk.getBlockState(mutableBlockPos));
                }
            }
        }
        //检测并加入过关数据
        for(int x = 1; x <= 18; ++x){
            for(int y = 1; y <= numY + 2; ++y){
                for(int z = 1; z <= 18; ++z){
                    if(!states[x][y][z].doShowAround) continue;
                    markPos(x - 3, y + minY - 2, z - 2, chunkPos, states[x - 1][y][z].isXRayTarget);
                    markPos(x - 1, y + minY - 2, z - 2, chunkPos, states[x + 1][y][z].isXRayTarget);
                    markPos(x - 2, y + minY - 3, z - 2, chunkPos, states[x][y - 1][z].isXRayTarget);
                    markPos(x - 2, y + minY - 1, z - 2, chunkPos, states[x][y + 1][z].isXRayTarget);
                    markPos(x - 2, y + minY - 2, z - 3, chunkPos, states[x][y][z - 1].isXRayTarget);
                    markPos(x - 2, y + minY - 2, z - 1, chunkPos, states[x][y][z + 1].isXRayTarget);
                }
            }
        }
    }

    private static final HashSet<ThreadTask> threadTasks = new HashSet<>();
    private static Thread thread;
    private record ThreadTask(ClientWorld world, WorldChunk chunk, boolean unload){
        @Override public int hashCode(){
            return chunk.getPos().hashCode();
        }
        @Override public boolean equals(Object o){
            if(o instanceof ThreadTask task)
                return task.world == world && task.chunk == chunk && task.unload == unload;
            return false;
        }
    }

    private static void updateChunkInAnotherThread(ClientWorld world, WorldChunk chunk, boolean unload){
        synchronized (threadTasks){
            threadTasks.add(new ThreadTask(world, chunk, unload));
            if(thread == null){
                thread = new Thread(SlightXRay::ThreadFunc);
                thread.start();
            }
        }
    }
    private static void ThreadFunc(){
        while(true){
            ThreadTask task = null;
            double minDistance = Double.MAX_VALUE;
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            Vec3i playerPos = player == null ? Vec3i.ZERO : player.getBlockPos();
            synchronized (threadTasks){
                for(ThreadTask task1 : threadTasks){
                    double distance = task1.chunk.getPos().getCenterAtY(playerPos.getY()).getSquaredDistance(playerPos);
                    if(distance < minDistance){
                        minDistance = distance;
                        task = task1;
                    }
                }
                if(task == null){
                    thread = null;
                    break;
                }
                threadTasks.remove(task);
            }
            updateChunk(task.world, task.chunk, task.unload);
        }
    }

    private static void markPos(int x, int y, int z, ChunkPos chunkPos, boolean status){
        BlockPos pos = chunkPos.getStartPos().add(x, y, z);
        synchronized (markedBlocks){
            if(status) markedBlocks.add(pos);
            else markedBlocks.remove(pos);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void vertexBlock(Matrix4f matrix, BufferBuilder buffer, BlockPos pos, int color){
        //TODO:高精度处理
        Vector3f f_pos = pos.toCenterPos().toVector3f();
        if(!markedBlocks.contains(pos.west())){
            buffer.vertex(matrix, f_pos.x - 0.5f, f_pos.y - 0.5f, f_pos.z - 0.5f).color(color);
            buffer.vertex(matrix, f_pos.x - 0.5f, f_pos.y - 0.5f, f_pos.z + 0.5f).color(color);
            buffer.vertex(matrix, f_pos.x - 0.5f, f_pos.y + 0.5f, f_pos.z + 0.5f).color(color);
            buffer.vertex(matrix, f_pos.x - 0.5f, f_pos.y + 0.5f, f_pos.z - 0.5f).color(color);
        }
        if(!markedBlocks.contains(pos.east())){
            buffer.vertex(matrix, f_pos.x + 0.5f, f_pos.y - 0.5f, f_pos.z - 0.5f).color(color);
            buffer.vertex(matrix, f_pos.x + 0.5f, f_pos.y + 0.5f, f_pos.z - 0.5f).color(color);
            buffer.vertex(matrix, f_pos.x + 0.5f, f_pos.y + 0.5f, f_pos.z + 0.5f).color(color);
            buffer.vertex(matrix, f_pos.x + 0.5f, f_pos.y - 0.5f, f_pos.z + 0.5f).color(color);
        }
        if(!markedBlocks.contains(pos.down())){
            buffer.vertex(matrix, f_pos.x - 0.5f, f_pos.y - 0.5f, f_pos.z - 0.5f).color(color);
            buffer.vertex(matrix, f_pos.x + 0.5f, f_pos.y - 0.5f, f_pos.z - 0.5f).color(color);
            buffer.vertex(matrix, f_pos.x + 0.5f, f_pos.y - 0.5f, f_pos.z + 0.5f).color(color);
            buffer.vertex(matrix, f_pos.x - 0.5f, f_pos.y - 0.5f, f_pos.z + 0.5f).color(color);
        }
        if(!markedBlocks.contains(pos.up())){
            buffer.vertex(matrix, f_pos.x - 0.5f, f_pos.y + 0.5f, f_pos.z - 0.5f).color(color);
            buffer.vertex(matrix, f_pos.x - 0.5f, f_pos.y + 0.5f, f_pos.z + 0.5f).color(color);
            buffer.vertex(matrix, f_pos.x + 0.5f, f_pos.y + 0.5f, f_pos.z + 0.5f).color(color);
            buffer.vertex(matrix, f_pos.x + 0.5f, f_pos.y + 0.5f, f_pos.z - 0.5f).color(color);
        }
        if(!markedBlocks.contains(pos.north())){
            buffer.vertex(matrix, f_pos.x - 0.5f, f_pos.y - 0.5f, f_pos.z - 0.5f).color(color);
            buffer.vertex(matrix, f_pos.x - 0.5f, f_pos.y + 0.5f, f_pos.z - 0.5f).color(color);
            buffer.vertex(matrix, f_pos.x + 0.5f, f_pos.y + 0.5f, f_pos.z - 0.5f).color(color);
            buffer.vertex(matrix, f_pos.x + 0.5f, f_pos.y - 0.5f, f_pos.z - 0.5f).color(color);
        }
        if(!markedBlocks.contains(pos.south())){
            buffer.vertex(matrix, f_pos.x - 0.5f, f_pos.y - 0.5f, f_pos.z + 0.5f).color(color);
            buffer.vertex(matrix, f_pos.x + 0.5f, f_pos.y - 0.5f, f_pos.z + 0.5f).color(color);
            buffer.vertex(matrix, f_pos.x + 0.5f, f_pos.y + 0.5f, f_pos.z + 0.5f).color(color);
            buffer.vertex(matrix, f_pos.x - 0.5f, f_pos.y + 0.5f, f_pos.z + 0.5f).color(color);
        }
    }
}
