package lpctools.tools.singletool;

import fi.dy.masa.malilib.render.MaLiLibPipelines;
import fi.dy.masa.malilib.render.RenderContext;
import lpctools.LPCTools;
import lpctools.lpcfymasaapi.Registry;
import lpctools.lpcfymasaapi.configbutton.IValueRefreshCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.*;

import static lpctools.util.MathUtils.*;

public class SlightXRay implements IValueRefreshCallback, WorldRenderEvents.End {
    //markedBlocks放在多线程里用，记得要同步
    @NotNull final HashSet<BlockPos> markedBlocks = new HashSet<>();
    @NotNull static final HashSet<Block> XRayBlocks = initHashset();
    private static HashSet<Block> initHashset(){
        HashSet<Block> blocks = new HashSet<>();
        blocks.add(Blocks.DIAMOND_ORE);
        blocks.add(Blocks.DEEPSLATE_DIAMOND_ORE);
        return blocks;
    }

    @Override public void valueRefreshCallback() {
        if(SingleTool.slightXRay.getAsBoolean()){
            if(Registry.registerWorldRenderEndCallback(this)){
                ClientWorld world = MinecraftClient.getInstance().world;
                ClientPlayerEntity player = MinecraftClient.getInstance().player;
                if(world == null || player == null) return;
                int distance = MinecraftClient.getInstance().options.getViewDistance().getValue();
                ChunkPos chunkPos = player.getChunkPos();
                for(int x = chunkPos.x - distance; x <= chunkPos.x + distance; ++x){
                    for(int z = chunkPos.z - distance; z <= chunkPos.z + distance; ++z){
                        if(world.isChunkLoaded(x, z))
                            newAThreadToLoadChunk(world, new ChunkPos(x, z), player);
                    }
                }
            }
        }
        else {
            Registry.unregisterWorldRenderEndCallback(this);
            synchronized (markedBlocks){
                markedBlocks.clear();
            }
            synchronized (threadTasks){
                threadTasks.clear();
            }
        }
    }

    @Override public void onEnd(WorldRenderContext context) {
        RenderContext ctx = new RenderContext(MaLiLibPipelines.POSITION_COLOR_MASA_NO_DEPTH);
        BufferBuilder buffer = ctx.getBuilder();
        Matrix4f matrix = worldToCameraMatrix(context.camera());
        synchronized (markedBlocks){
            for(BlockPos pos : markedBlocks)
                vertexBlock(matrix, buffer, pos, 0x7F3F7FFF);
        }
        try {
            BuiltBuffer meshData = buffer.endNullable();
            if (meshData != null) {
                ctx.draw(meshData, false, true);
                meshData.close();
            }
            ctx.close();
        } catch (Exception err) {
            LPCTools.LOGGER.error("lpctools.tools.singletool.SlightXRay.onLast(): Draw Exception; {}", err.getMessage());
        }
    }

    private enum XRayNecessaryState{
        F_F(false, false),
        F_T(false, true),
        T_F(true, false),
        T_T(true, true);
        public final boolean doShowAround, isXRayTarget;
        public static XRayNecessaryState of(BlockState state){
            return values()[(XRayBlocks.contains(state.getBlock()) ? 1 : 0)
                    + (!state.isOpaque() || state.isTransparent() ? 2 : 0)];
        }
        XRayNecessaryState(boolean doShowAround, boolean isXRayTarget){
            this.doShowAround = doShowAround;
            this.isXRayTarget = isXRayTarget;
        }
    }

    private void loadChunk(ClientWorld world, ChunkPos chunkPos){
        //TODO:优化算法
        int cx = chunkPos.x, cz = chunkPos.z;
        if(!world.isChunkLoaded(cx, cz)) return;
        WorldChunk chunk = world.getChunk(cx, cz);
        int minY = world.getBottomY();
        int numY = world.getHeight();
        int topY = minY + numY;

        //states用于存放预处理后的数据，并向外拓展了两格防止越界
        XRayNecessaryState[][][] states = new XRayNecessaryState[18][numY + 2][18];
        //初始化states数据
        for(XRayNecessaryState[][] states1 : states){
            for(XRayNecessaryState[] states2 : states1){
                Arrays.fill(states2, XRayNecessaryState.F_F);
            }
        }
        //加载本区块中数据
        BlockPos.Mutable mutableBlockPos = new BlockPos.Mutable();
        for(int x = 0; x < 16; ++x){
            mutableBlockPos.setX(x);
            XRayNecessaryState[][] states1 = states[x + 1];
            for(int y = minY; y < topY; ++y){
                mutableBlockPos.setY(y);
                XRayNecessaryState[] states2 = states1[y - minY + 1];
                for(int z = 0; z < 16; ++z){
                    mutableBlockPos.setZ(z);
                    states2[z + 1] = XRayNecessaryState.of(chunk.getBlockState(mutableBlockPos));
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
                    states[0][y - minY + 1][z + 1] = XRayNecessaryState.of(nxChunk.getBlockState(mutableBlockPos));
                }
            }
        }
        if(pxChunk != null){
            mutableBlockPos.setX(16);
            for(int y = minY; y < topY; ++y){
                mutableBlockPos.setY(y);
                for(int z = 0; z < 16; ++z){
                    mutableBlockPos.setZ(z);
                    states[17][y - minY + 1][z + 1] = XRayNecessaryState.of(pxChunk.getBlockState(mutableBlockPos));
                }
            }
        }
        if(nzChunk != null){
            mutableBlockPos.setZ(-1);
            for(int y = minY; y < topY; ++y){
                mutableBlockPos.setY(y);
                for(int x = 0; x < 16; ++x){
                    mutableBlockPos.setX(x);
                    states[x + 1][y - minY + 1][0] = XRayNecessaryState.of(nzChunk.getBlockState(mutableBlockPos));
                }
            }
        }
        if(pzChunk != null){
            mutableBlockPos.setZ(16);
            for(int y = minY; y < topY; ++y){
                mutableBlockPos.setY(y);
                for(int x = 0; x < 16; ++x){
                    mutableBlockPos.setX(x);
                    states[x + 1][y - minY + 1][17] = XRayNecessaryState.of(pzChunk.getBlockState(mutableBlockPos));
                }
            }
        }
        //检测并加入过关数据
        for(int x = 1; x <= 16; ++x){
            for(int y = 1; y <= numY; ++y){
                for(int z = 1; z <= 16; ++z){
                    if(!states[x][y][z].doShowAround) continue;
                    if(states[x - 1][y][z].isXRayTarget) markPos(x - 2, y + minY - 1, z - 1, chunkPos);
                    if(states[x + 1][y][z].isXRayTarget) markPos(x, y + minY - 1, z - 1, chunkPos);
                    if(states[x][y - 1][z].isXRayTarget) markPos(x - 1, y + minY - 2, z - 1, chunkPos);
                    if(states[x][y + 1][z].isXRayTarget) markPos(x - 1, y + minY, z - 1, chunkPos);
                    if(states[x][y][z - 1].isXRayTarget) markPos(x - 1, y + minY - 1, z - 2, chunkPos);
                    if(states[x][y][z + 1].isXRayTarget) markPos(x - 1, y + minY - 1, z, chunkPos);
                }
            }
        }
    }

    private static final PriorityQueue<ThreadTask> threadTasks = new PriorityQueue<>(new ThreadTask.Comparator());
    private static Thread thread;
    private record ThreadTask(double distance, ClientWorld world, ChunkPos chunkPos){
        public static class Comparator implements java.util.Comparator<ThreadTask>{
            @Override public int compare(ThreadTask o1, ThreadTask o2) {
                return (int)(o1.distance - o2.distance);
            }
        }
        ThreadTask(ClientWorld world, ChunkPos pos, ClientPlayerEntity player){
            this(pos.getCenterAtY((int)player.getPos().getY()).getSquaredDistance(player.getPos()),
                    world, pos);
        }
    }

    private void newAThreadToLoadChunk(ClientWorld world, ChunkPos chunkPos, ClientPlayerEntity player){
        synchronized (threadTasks){
            threadTasks.add(new ThreadTask(world, chunkPos, player));
            if(thread == null){
                thread = new Thread(this::ThreadFunc);
                thread.start();
            }
        }
    }
    private void ThreadFunc(){
        while(true){
            ThreadTask task;
            synchronized (threadTasks){
                if(threadTasks.isEmpty()){
                    thread = null;
                    break;
                }
                task = threadTasks.remove();
            }
            loadChunk(task.world, task.chunkPos);
        }
    }

    private void markPos(int x, int y, int z, ChunkPos chunkPos){
        synchronized (markedBlocks){
            markedBlocks.add(chunkPos.getStartPos().add(x, y, z));
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
