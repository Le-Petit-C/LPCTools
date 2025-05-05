package lpctools.tools.slightXRay;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import lpctools.compact.derived.ShapeList;
import lpctools.lpcfymasaapi.Registry;
import lpctools.lpcfymasaapi.configbutton.ILPCValueChangeCallback;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.RangeLimitConfig;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.ThirdListConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.BooleanHotkeyConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.ColorConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.StringListConfig;
import lpctools.tools.ToolConfigs;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.NotNull;
import org.joml.*;

import java.util.*;

import static lpctools.util.AlgorithmUtils.*;
import static lpctools.util.DataUtils.*;
import static lpctools.util.MathUtils.*;

public class SlightXRay implements ILPCValueChangeCallback, WorldRenderEvents.End, ClientChunkEvents.Load, ClientChunkEvents.Unload, Registry.AfterClientWorldChange {
    //markedBlocks放在多线程里用，记得要同步
    static final @NotNull HashSet<BlockPos> markedBlocks = new HashSet<>();
    static final @NotNull ImmutableList<Block> defaultXRayBlocks = ImmutableList.of(
            Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE,
            Blocks.DEEPSLATE_COAL_ORE, Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE,
            Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN, Blocks.ENDER_CHEST, Blocks.REINFORCED_DEEPSLATE,
            Blocks.BUDDING_AMETHYST, Blocks.CALCITE
    );
    static final @NotNull HashSet<Block> XRayBlocks = new HashSet<>(defaultXRayBlocks);
    static final @NotNull ImmutableList<String> defaultXRayBlockIds = idListFromBlockList(defaultXRayBlocks);
    public static BooleanHotkeyConfig slightXRay;
    public static ColorConfig displayColor;
    public static StringListConfig XRayBlocksConfig;
    public static RangeLimitConfig displayRange;

    public static void init(ThirdListConfig SXConfig){
        slightXRay = SXConfig.addBooleanHotkeyConfig("slightXRay", false, null, new SlightXRay());
        slightXRay.getKeybind().setCallback((action, key) -> {
            slightXRay.toggleBooleanValue();
            ToolConfigs.displayToggleMessage(slightXRay);
            return true;
        });
        displayColor = SXConfig.addColorConfig("displayColor", 0x7F3F7FFF);
        XRayBlocksConfig = SXConfig.addStringListConfig("XRayBlocks", defaultXRayBlockIds, SlightXRay::refreshXRayBlocks);
        displayRange = SXConfig.addRangeLimitConfig(false, "SX");
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
        HashSet<Block> newBlocks = blockSetFromIds(XRayBlocksConfig.getStrings());
        if(XRayBlocks.equals(newBlocks)) return;
        XRayBlocks.clear();
        XRayBlocks.addAll(newBlocks);
        if(slightXRay.getAsBoolean()){
            clearAll();
            addAllRenderRegionsIntoWork();
        }
    }

    @Override public void onValueChanged() {
        if(slightXRay.getAsBoolean()){
            if(Registry.registerWorldRenderEndCallback(this))
                addAllRenderRegionsIntoWork();
            Registry.registerClientChunkLoadCallbacks(this);
            Registry.registerClientChunkUnloadCallbacks(this);
            Registry.registerClientWorldChangeCallbacks(this);
            ToolConfigs.displayEnableMessage(slightXRay);
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
            Registry.unregisterClientWorldChangeCallbacks(this);
            ToolConfigs.displayDisableMessage(slightXRay);
        }
    }

    @Override public void onEnd(WorldRenderContext context) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        Matrix4d matrix = worldToCameraMatrix4d(context.camera());
        int color = displayColor.getAsInt();
        ShapeList shapes = displayRange.buildShapeList();
        boolean bufferUsed = false;
        synchronized (markedBlocks){
            if(markedBlocks.isEmpty()) return;
            for(BlockPos pos : markedBlocks){
                if(shapes.testPos(pos)){
                    vertexBlock(matrix, buffer, pos, color, shapes);
                    bufferUsed = true;
                }
            }
        }
        if(!bufferUsed) return;
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }
    @Override public void onChunkLoad(ClientWorld clientWorld, WorldChunk worldChunk) {
        updateChunkInAnotherThread(clientWorld, worldChunk, false);
    }
    @Override public void onChunkUnload(ClientWorld clientWorld, WorldChunk worldChunk) {
        updateChunkInAnotherThread(clientWorld, worldChunk, true);
    }

    private static boolean doShowAround(BlockState state){
        return !state.isOpaque() || state.isTransparent(MinecraftClient.getInstance().world, BlockPos.ORIGIN);
    }

    private static boolean isXRayTarget(BlockState state){
        return XRayBlocks.contains(state.getBlock());
    }

    @Override public void afterWorldChange(MinecraftClient client, ClientWorld world) {
        clearAll();
    }

    private static void clearAll(){
        synchronized (threadTasks){
            threadTasks.clear();
            if(loadingThreadCount != 0) {
                try {threadTasks.wait();} catch (InterruptedException ignored) {}
            }
        }
        synchronized (markedBlocks){
            markedBlocks.clear();
        }
    }

    private enum XRayNecessaryState{
        F_F(false, false),
        F_T(false, true),
        T_F(true, false),
        T_T(true, true);
        public final boolean doShowAround, isXRayTarget;
        public static XRayNecessaryState of(BlockState state){
            if(state.getBlock() != Blocks.VOID_AIR)
                return values()[(isXRayTarget(state) ? 1 : 0) + (doShowAround(state) ? 2 : 0)];
            else return null;
        }
        XRayNecessaryState(boolean doShowAround, boolean isXRayTarget){
            this.doShowAround = doShowAround;
            this.isXRayTarget = isXRayTarget;
        }
    }

    public static void setBlockStateTest(World world, BlockPos pos, BlockState lastState, BlockState currentState){
        if(lastState == null) lastState = Blocks.AIR.getDefaultState();
        if(currentState == null) currentState = Blocks.AIR.getDefaultState();
        if(doShowAround(lastState) && doShowAround(currentState)) return;
        if(doShowAround(currentState)){
            for(BlockPos pos1 : iterateInManhattanDistance(pos, 2))
                testPos(world, pos1, world.getBlockState(pos1));
        }
        else testPos(world, pos, currentState);
    }

    private static void testPos(World world, BlockPos pos, BlockState state){
        if(!isXRayTarget(state)){
            synchronized (markedBlocks){
                markedBlocks.remove(pos);
            }
            return;
        }
        for(BlockPos pos1 : iterateInManhattanDistance(pos, 2)) {
            if(doShowAround(world.getBlockState(pos1))){
                synchronized (markedBlocks){
                    markedBlocks.add(pos.mutableCopy());
                }
                return;
            }
        }
        synchronized (markedBlocks){
            markedBlocks.remove(pos);
        }
    }

    //states用于存放预处理后的数据，向外拓展了一格处理相邻区块的内容，再向外拓展了一格防止越界
    private static XRayNecessaryState[][][] allocateStateBuffer(XRayNecessaryState[][][] lastBuffer, World world){
        if(lastBuffer != null && lastBuffer[0].length == world.getHeight()) return lastBuffer;
        return new XRayNecessaryState[20][world.getHeight() + 4][20];
    }
    private static void updateChunk(XRayNecessaryState[][][] states, ClientWorld world, WorldChunk chunk, boolean unload){
        ChunkPos chunkPos = chunk.getPos();
        int cx = chunkPos.x, cz = chunkPos.z;
        if(unload){
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

        //初始化states数据
        for(XRayNecessaryState[][] states1 : states){
            for(int y = 0; y < states1.length; ++y){
                XRayNecessaryState[] states2 = states1[y];
                if(world.isOutOfHeightLimit(y))
                    Arrays.fill(states2, XRayNecessaryState.of(Blocks.AIR.getDefaultState()));
                else Arrays.fill(states2, null);
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
                    if(states[x][y][z] == null || !states[x][y][z].doShowAround) continue;
                    if(states[x - 1][y][z] != null) markPos(x - 3, y + minY - 2, z - 2, chunkPos, states[x - 1][y][z].isXRayTarget);
                    if(states[x + 1][y][z] != null) markPos(x - 1, y + minY - 2, z - 2, chunkPos, states[x + 1][y][z].isXRayTarget);
                    if(states[x][y - 1][z] != null) markPos(x - 2, y + minY - 3, z - 2, chunkPos, states[x][y - 1][z].isXRayTarget);
                    if(states[x][y + 1][z] != null) markPos(x - 2, y + minY - 1, z - 2, chunkPos, states[x][y + 1][z].isXRayTarget);
                    if(states[x][y][z - 1] != null) markPos(x - 2, y + minY - 2, z - 3, chunkPos, states[x][y][z - 1].isXRayTarget);
                    if(states[x][y][z + 1] != null) markPos(x - 2, y + minY - 2, z - 1, chunkPos, states[x][y][z + 1].isXRayTarget);
                }
            }
        }
    }
    private static void markPos(int x, int y, int z, ChunkPos chunkPos, boolean status){
        BlockPos pos = chunkPos.getStartPos().add(x, y, z);
        if(status){
            synchronized (markedBlocks){
                markedBlocks.add(pos.mutableCopy());
            }
        }
    }

    private static final HashSet<ThreadTask> threadTasks = new HashSet<>();
    private static final int maxLoadingThreadCount = 4;
    private static final int threadTaskCountLimit = 4;
    private static int loadingThreadCount = 0;
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
            if(loadingThreadCount < maxLoadingThreadCount &&
                    loadingThreadCount * threadTaskCountLimit < threadTasks.size()){
                new Thread(SlightXRay::ThreadFunc).start();
                ++loadingThreadCount;
            }
        }
    }
    private static void ThreadFunc(){
        XRayNecessaryState[][][] stateBuffer = null;
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
                    --loadingThreadCount;
                    break;
                }
                threadTasks.remove(task);
            }
            stateBuffer = allocateStateBuffer(stateBuffer, task.world);
            updateChunk(stateBuffer, task.world, task.chunk, task.unload);
        }
        synchronized (threadTasks){
            if(loadingThreadCount == 0)
                threadTasks.notifyAll();
        }
    }

    private static class VertexVectorBuffer{
        public final Vector4d center = new Vector4d();
        public final Vector4d buf = new Vector4d();
        public final Vector3f nnn = new Vector3f();
        public final Vector3f nnp = new Vector3f();
        public final Vector3f npn = new Vector3f();
        public final Vector3f npp = new Vector3f();
        public final Vector3f pnn = new Vector3f();
        public final Vector3f pnp = new Vector3f();
        public final Vector3f ppn = new Vector3f();
        public final Vector3f ppp = new Vector3f();
        public final BlockPos.Mutable mutablePos = new BlockPos.Mutable();
    }

    private final VertexVectorBuffer vertexVectorBuffer = new VertexVectorBuffer();

    @SuppressWarnings("SameParameterValue")
    private void vertexBlock(Matrix4d matrix, BufferBuilder buffer, BlockPos pos, int color, ShapeList shapes){
        VertexVectorBuffer vBuf = vertexVectorBuffer;
        Vector4d center = vBuf.center.set(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1);
        Vector4d buf = vBuf.buf;
        Vector3f nnn = vecTrans(center.add(-0.5, -0.5, -0.5, 0, buf).mul(matrix), vBuf.nnn);
        Vector3f nnp = vecTrans(center.add(-0.5, -0.5, 0.5, 0, buf).mul(matrix), vBuf.nnp);
        Vector3f npn = vecTrans(center.add(-0.5, 0.5, -0.5, 0, buf).mul(matrix), vBuf.npn);
        Vector3f npp = vecTrans(center.add(-0.5, 0.5, 0.5, 0, buf).mul(matrix), vBuf.npp);
        Vector3f pnn = vecTrans(center.add(0.5, -0.5, -0.5, 0, buf).mul(matrix), vBuf.pnn);
        Vector3f pnp = vecTrans(center.add(0.5, -0.5, 0.5, 0, buf).mul(matrix), vBuf.pnp);
        Vector3f ppn = vecTrans(center.add(0.5, 0.5, -0.5, 0, buf).mul(matrix), vBuf.ppn);
        Vector3f ppp = vecTrans(center.add(0.5, 0.5, 0.5, 0, buf).mul(matrix), vBuf.ppp);
        BlockPos.Mutable mutablePos = vBuf.mutablePos.set(pos);
        mutablePos.setX(pos.getX() - 1);
        if(!shapes.testPos(mutablePos) || !markedBlocks.contains(mutablePos)){
            buffer.vertex(nnn).color(color);
            buffer.vertex(nnp).color(color);
            buffer.vertex(npp).color(color);
            buffer.vertex(npn).color(color);
        }
        mutablePos.setX(pos.getX() + 1);
        if(!shapes.testPos(mutablePos) || !markedBlocks.contains(mutablePos)){
            buffer.vertex(pnn).color(color);
            buffer.vertex(ppn).color(color);
            buffer.vertex(ppp).color(color);
            buffer.vertex(pnp).color(color);
        }
        mutablePos.setX(pos.getX());
        mutablePos.setY(pos.getY() - 1);
        if(!shapes.testPos(mutablePos) || !markedBlocks.contains(mutablePos)){
            buffer.vertex(nnn).color(color);
            buffer.vertex(pnn).color(color);
            buffer.vertex(pnp).color(color);
            buffer.vertex(nnp).color(color);
        }
        mutablePos.setY(pos.getY() + 1);
        if(!shapes.testPos(mutablePos) || !markedBlocks.contains(mutablePos)){
            buffer.vertex(npn).color(color);
            buffer.vertex(npp).color(color);
            buffer.vertex(ppp).color(color);
            buffer.vertex(ppn).color(color);
        }
        mutablePos.setY(pos.getY());
        mutablePos.setZ(pos.getZ() - 1);
        if(!shapes.testPos(mutablePos) || !markedBlocks.contains(mutablePos)){
            buffer.vertex(nnn).color(color);
            buffer.vertex(npn).color(color);
            buffer.vertex(ppn).color(color);
            buffer.vertex(pnn).color(color);
        }
        mutablePos.setZ(pos.getZ() + 1);
        if(!shapes.testPos(mutablePos) || !markedBlocks.contains(mutablePos)){
            buffer.vertex(nnp).color(color);
            buffer.vertex(pnp).color(color);
            buffer.vertex(ppp).color(color);
            buffer.vertex(npp).color(color);
        }
    }
}
