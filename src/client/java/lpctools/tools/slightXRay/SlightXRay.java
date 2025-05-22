package lpctools.tools.slightXRay;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.render.MaLiLibPipelines;
import fi.dy.masa.malilib.render.RenderContext;
import fi.dy.masa.malilib.util.data.Color4f;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lpctools.LPCTools;
import lpctools.compact.derived.ShapeList;
import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.Registry;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.ConfigListOptionListConfigEx;
import lpctools.lpcfymasaapi.implementations.ILPCConfigBase;
import lpctools.lpcfymasaapi.implementations.ILPCValueChangeCallback;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.RangeLimitConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.BooleanHotkeyConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.ColorConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.StringListConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
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
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.NotNull;
import org.joml.*;

import java.util.*;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;
import static lpctools.tools.ToolUtils.*;
import static lpctools.util.AlgorithmUtils.*;
import static lpctools.util.DataUtils.*;
import static lpctools.util.MathUtils.*;

public class SlightXRay implements ILPCValueChangeCallback, WorldRenderEvents.End, ClientChunkEvents.Load, ClientChunkEvents.Unload, ClientWorldEvents.AfterClientWorldChange {
    //markedBlocks放在多线程里用，记得要同步
    static final @NotNull Object2IntOpenHashMap<BlockPos> markedBlocks = new Object2IntOpenHashMap<>();
    static final @NotNull ImmutableList<Block> defaultXRayBlocks = ImmutableList.of(
            Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE,
            Blocks.DEEPSLATE_COAL_ORE, Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE,
            Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN, Blocks.ENDER_CHEST, Blocks.REINFORCED_DEEPSLATE,
            Blocks.BUDDING_AMETHYST, Blocks.CALCITE
    );
    static final @NotNull Object2IntOpenHashMap<Block> XRayBlocks = new Object2IntOpenHashMap<>(defaultXRayBlocks.toArray(new Block[0]), new int[defaultXRayBlocks.size()]);
    static final @NotNull ImmutableList<String> defaultXRayBlockIds = idListFromBlockList(defaultXRayBlocks);
    public static BooleanHotkeyConfig slightXRay;
    public static ConfigListOptionListConfigEx<ConfigListWithColorMethod> defaultColorMethod;
    public static ColorConfig displayColor;
    public static StringListConfig XRayBlocksConfig;
    public static RangeLimitConfig displayRange;

    public static void init(){
        slightXRay = addBooleanHotkeyConfig("slightXRay", false, null, new SlightXRay());
        setLPCToolsToggleText(slightXRay);
        defaultColorMethod = addConfigListOptionListConfigEx("defaultColor");
        defaultColorMethod.addOption("displayColor", new ConfigListWithColorMethod(defaultColorMethod, "displayColor"));
        displayColor = addColorConfig("displayColor", Color4f.fromColor(0x7F3F7FFF));
        XRayBlocksConfig = addStringListConfig("XRayBlocks", defaultXRayBlockIds, SlightXRay::refreshXRayBlocks);
        displayRange = addRangeLimitConfig(false);
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
        Object2IntOpenHashMap<Block> newBlocks = new Object2IntOpenHashMap<>();
        for(String str : XRayBlocksConfig){
            String[] splits = str.split(";");
            if(splits.length > 0 && splits.length < 3){
                Block block = getBlockFromId(splits[0], true);
                if(block == null) continue;
                Integer color = null;
                /*if(splits.length > 1) {
                    try {
                        color = Integer.parseUnsignedInt(splits[1], 16);
                    }catch (NumberFormatException e){
                        warnInvalidString(str);
                        continue;
                    }
                }
                if(color == null) color = displayColor.getColor().intValue;*/
                color = block.getDefaultMapColor().color | (displayColor.getIntegerValue() & 0xff000000);
                newBlocks.addTo(block, color);
            }
            else warnInvalidString(str);
        }
        if(XRayBlocks.equals(newBlocks)) return;
        XRayBlocks.clear();
        XRayBlocks.putAll(newBlocks);
        if(slightXRay.getAsBoolean()){
            clearAll();
            addAllRenderRegionsIntoWork();
        }
    }
    private static void warnInvalidString(String str){
        notifyPlayer(String.format("§eWarning: Invalid string \"%s\"", str), false);
    }

    @Override public void onValueChanged() {
        if(slightXRay.getAsBoolean()){
            if(Registry.registerWorldRenderEndCallback(this))
                addAllRenderRegionsIntoWork();
            Registry.registerClientChunkLoadCallbacks(this);
            Registry.registerClientChunkUnloadCallbacks(this);
            Registry.registerClientWorldChangeCallbacks(this);
            displayEnableMessage(slightXRay);
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
            displayDisableMessage(slightXRay);
        }
    }

    @Override public void onEnd(WorldRenderContext context) {
        RenderContext ctx = new RenderContext(MaLiLibPipelines.POSITION_COLOR_MASA_NO_DEPTH);
        BufferBuilder buffer = ctx.getBuilder();
        Matrix4d matrix = worldToCameraMatrix4d(context.camera());
        ShapeList shapes = displayRange.buildShapeList();
        boolean bufferUsed = false;
        synchronized (markedBlocks){
            for(Object2IntMap.Entry<BlockPos> pos : markedBlocks.object2IntEntrySet()){
                if(shapes.testPos(pos.getKey())){
                    vertexBlock(matrix, buffer, pos.getKey(), pos.getIntValue(), shapes);
                    bufferUsed = true;
                }
            }
        }
        if(!bufferUsed) return;
        try {
            BuiltBuffer meshData = buffer.endNullable();
            if (meshData != null) {
                ctx.draw(meshData, false, true);
                meshData.close();
            }
            ctx.close();
        } catch (Exception err) {
            LPCTools.LOGGER.error("lpctools.tools.slightXRay.slightXRay.onLast(): Draw Exception; {}", err.getMessage());
        }
    }
    @Override public void onChunkLoad(ClientWorld clientWorld, WorldChunk worldChunk) {
        updateChunkInAnotherThread(clientWorld, worldChunk, false);
    }
    @Override public void onChunkUnload(ClientWorld clientWorld, WorldChunk worldChunk) {
        updateChunkInAnotherThread(clientWorld, worldChunk, true);
    }

    private static boolean doShowAround(BlockState state){
        return !state.isOpaque() || state.isTransparent();
    }

    private static boolean isXRayTarget(BlockState state){
        return XRayBlocks.containsKey(state.getBlock());
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

    public static void setBlockStateTest(World world, BlockPos pos, BlockState lastState, BlockState currentState){
        if(lastState == null) lastState = Blocks.AIR.getDefaultState();
        if(currentState == null) currentState = Blocks.AIR.getDefaultState();
        if(doShowAround(lastState) && doShowAround(currentState)) return;
        if(doShowAround(currentState)){
            for(BlockPos pos1 : iterateInManhattanDistance(pos, 2))
                testPos(world, pos1);
        }
        else testPos(world, pos);
    }

    private static void testPos(World world, BlockPos pos){
        BlockState state = world.getBlockState(pos);
        if(!isXRayTarget(state)){
            synchronized (markedBlocks){
                markedBlocks.keySet().remove(pos);
            }
            return;
        }
        for(BlockPos pos1 : iterateInManhattanDistance(pos, 2)) {
            if(doShowAround(world.getBlockState(pos1))){
                synchronized (markedBlocks){
                    markedBlocks.put(pos.toImmutable(), XRayBlocks.getInt(state.getBlock()));
                }
                return;
            }
        }
        synchronized (markedBlocks){
            markedBlocks.keySet().remove(pos);
        }
    }
    
    //用于存放预处理后的数据，向外拓展了一格处理相邻区块的内容，再向外拓展了一格防止越界
    static class XRayNecessaryState{
        public static class Data{
            public boolean doShowAround = false;
            public int color = 0;
            void set(BlockState state){
                doShowAround = doShowAround(state);
                Block block = state.getBlock();
                if(XRayBlocks.containsKey(block))
                    color = XRayBlocks.getInt(block);
                else color = 0;
                if(state.getBlock() == Blocks.VOID_AIR)
                    doShowAround = false;
            }
            @SuppressWarnings("SameParameterValue")
            void set(boolean doShowAround, int color){
                this.doShowAround = doShowAround;
                this.color = color;
            }
        }
        public final int worldHeight;
        public final Data[][][] data;
        XRayNecessaryState(int worldHeight){
            data = new Data[20][worldHeight + 4][20];
            this.worldHeight = worldHeight;
            for (Data[][] data1 : data)
                for (Data[] data2 : data1)
                    for (int z = 0; z < data2.length; ++z)
                        data2[z] = new Data();
        }
        Data get(BlockPos pos){
            return get(pos.getX(), pos.getY(), pos.getZ());
        }
        Data get(int x, int y, int z){
            return data[x + 2][y + 2][z + 2];
        }
        public static class MutableEntry<T, U> implements Map.Entry<T, U>{
            T key; U value;
            @Override public T getKey() {
                return key;
            }
            @Override public U getValue() {
                return value;
            }
            @Override public U setValue(U value) {
                U old = this.value;
                this.value = value;
                return old;
            }
            MutableEntry(T key, U value){
                this.key = key;
                this.value = value;
            }
        }
        //在区块范围拓展若干格的范围内遍历，坐标是相对于区块xyz最小点的坐标
        Iterable<Map.Entry<BlockPos, Data>> iterateIn(int expand){
            return new Iterable<>() {
                @Override public @NotNull Iterator<Map.Entry<BlockPos, Data>> iterator() {
                    return new Iterator<>() {
                        final BlockPos.Mutable pos = new BlockPos.Mutable(expand + 15, worldHeight + expand - 1, -expand - 1);
                        final Map.Entry<BlockPos, Data> currentData
                            = new MutableEntry<>(pos, new Data());
                        @Override public boolean hasNext() {
                            return pos.getZ() < expand + 15
                                || pos.getY() < expand + worldHeight - 1
                                || pos.getX() < expand + 15;
                        }
                        @Override public Map.Entry<BlockPos, Data> next() {
                            pos.setX(pos.getX() + 1);
                            if(pos.getX() >= expand + 16){
                                pos.setX(-expand);
                                pos.setY(pos.getY() + 1);
                                if(pos.getY() >= expand + worldHeight){
                                    pos.setY(-expand);
                                    pos.setZ(pos.getZ() + 1);
                                }
                            }
                            currentData.setValue(get(pos));
                            return currentData;
                        }
                    };
                }
            };
        }
    }

    private static void updateChunk(XRayNecessaryState states, ClientWorld world, WorldChunk chunk, boolean unload){
        ChunkPos chunkPos = chunk.getPos();
        int cx = chunkPos.x, cz = chunkPos.z;
        if(unload){
            synchronized (markedBlocks){
                Iterator<BlockPos> posIterator = markedBlocks.keySet().iterator();
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
        try {
            //初始化states数据
            for(Map.Entry<BlockPos, XRayNecessaryState.Data> entry : states.iterateIn(2)){
                int y = entry.getKey().getY();
                boolean doShowAround = y < 2 || y >= states.worldHeight + 2;
                entry.getValue().set(doShowAround, 0);
            }
        }catch (Exception e){
            notifyPlayer(e.getMessage(), false);
            throw new RuntimeException(e);
        }
        //加载本区块中数据
        BlockPos.Mutable mutableBlockPos = new BlockPos.Mutable();
        for(int x = 0; x < 16; ++x){
            mutableBlockPos.setX(x);
            XRayNecessaryState.Data[][] states1 = states.data[x + 2];
            for(int y = minY; y < topY; ++y){
                mutableBlockPos.setY(y);
                XRayNecessaryState.Data[] states2 = states1[y - minY + 2];
                for(int z = 0; z < 16; ++z){
                    mutableBlockPos.setZ(z);
                    states2[z + 2].set(chunk.getBlockState(mutableBlockPos));
                }
            }
        }
        //加载相邻区块中数据
        world.isChunkLoaded(cx, cz);
        WorldChunk nxChunk = world.getChunk(cx - 1, cz);
        WorldChunk pxChunk = world.getChunk(cx + 1, cz);
        WorldChunk nzChunk = world.getChunk(cx, cz - 1);
        WorldChunk pzChunk = world.getChunk(cx, cz + 1);
        if(nxChunk != null){
            mutableBlockPos.setX(15);
            for(int y = minY; y < topY; ++y){
                mutableBlockPos.setY(y);
                for(int z = 0; z < 16; ++z){
                    mutableBlockPos.setZ(z);
                    states.data[1][y - minY + 2][z + 2].set(nxChunk.getBlockState(mutableBlockPos));
                }
            }
        }
        if(pxChunk != null){
            mutableBlockPos.setX(0);
            for(int y = minY; y < topY; ++y){
                mutableBlockPos.setY(y);
                for(int z = 0; z < 16; ++z){
                    mutableBlockPos.setZ(z);
                    states.data[18][y - minY + 2][z + 2].set(pxChunk.getBlockState(mutableBlockPos));
                }
            }
        }
        if(nzChunk != null){
            mutableBlockPos.setZ(15);
            for(int y = minY; y < topY; ++y){
                mutableBlockPos.setY(y);
                for(int x = 0; x < 16; ++x){
                    mutableBlockPos.setX(x);
                    states.data[x + 2][y - minY + 2][1].set(nzChunk.getBlockState(mutableBlockPos));
                }
            }
        }
        if(pzChunk != null){
            mutableBlockPos.setZ(0);
            for(int y = minY; y < topY; ++y){
                mutableBlockPos.setY(y);
                for(int x = 0; x < 16; ++x){
                    mutableBlockPos.setX(x);
                    states.data[x + 2][y - minY + 2][18].set(pzChunk.getBlockState(mutableBlockPos));
                }
            }
        }
        BlockPos chunkStartPos = chunkPos.getStartPos().add(0, chunk.getBottomY(), 0);
        //检测并加入过关数据
        for(Map.Entry<BlockPos, XRayNecessaryState.Data> data : states.iterateIn(1)){
            if(!data.getValue().doShowAround) continue;
            for(Direction direction : Direction.values()){
                BlockPos pos = data.getKey().offset(direction);
                markData(pos, chunkStartPos, states.get(pos));
            }
        }
    }
    private static void markData(BlockPos blockPos, BlockPos chunkStartPos, XRayNecessaryState.Data data){
        BlockPos pos = chunkStartPos.add(blockPos);
        if(data.color != 0){
            synchronized (markedBlocks){
                markedBlocks.put(pos.toImmutable(), data.color);
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
            XRayNecessaryState stateBuffer = new XRayNecessaryState(task.world.getHeight());
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
        Vector3f nnn = center.add(-0.5, -0.5, -0.5, 0, buf).mul(matrix).xyz(vBuf.nnn);
        Vector3f nnp = center.add(-0.5, -0.5, 0.5, 0, buf).mul(matrix).xyz(vBuf.nnp);
        Vector3f npn = center.add(-0.5, 0.5, -0.5, 0, buf).mul(matrix).xyz(vBuf.npn);
        Vector3f npp = center.add(-0.5, 0.5, 0.5, 0, buf).mul(matrix).xyz(vBuf.npp);
        Vector3f pnn = center.add(0.5, -0.5, -0.5, 0, buf).mul(matrix).xyz(vBuf.pnn);
        Vector3f pnp = center.add(0.5, -0.5, 0.5, 0, buf).mul(matrix).xyz(vBuf.pnp);
        Vector3f ppn = center.add(0.5, 0.5, -0.5, 0, buf).mul(matrix).xyz(vBuf.ppn);
        Vector3f ppp = center.add(0.5, 0.5, 0.5, 0, buf).mul(matrix).xyz(vBuf.ppp);
        BlockPos.Mutable mutablePos = vBuf.mutablePos.set(pos);
        mutablePos.setX(pos.getX() - 1);
        if(!shapes.testPos(mutablePos) || !markedBlocks.containsKey(mutablePos)){
            buffer.vertex(nnn).color(color);
            buffer.vertex(nnp).color(color);
            buffer.vertex(npp).color(color);
            buffer.vertex(npn).color(color);
        }
        mutablePos.setX(pos.getX() + 1);
        if(!shapes.testPos(mutablePos) || !markedBlocks.containsKey(mutablePos)){
            buffer.vertex(pnn).color(color);
            buffer.vertex(ppn).color(color);
            buffer.vertex(ppp).color(color);
            buffer.vertex(pnp).color(color);
        }
        mutablePos.setX(pos.getX());
        mutablePos.setY(pos.getY() - 1);
        if(!shapes.testPos(mutablePos) || !markedBlocks.containsKey(mutablePos)){
            buffer.vertex(nnn).color(color);
            buffer.vertex(pnn).color(color);
            buffer.vertex(pnp).color(color);
            buffer.vertex(nnp).color(color);
        }
        mutablePos.setY(pos.getY() + 1);
        if(!shapes.testPos(mutablePos) || !markedBlocks.containsKey(mutablePos)){
            buffer.vertex(npn).color(color);
            buffer.vertex(npp).color(color);
            buffer.vertex(ppp).color(color);
            buffer.vertex(ppn).color(color);
        }
        mutablePos.setY(pos.getY());
        mutablePos.setZ(pos.getZ() - 1);
        if(!shapes.testPos(mutablePos) || !markedBlocks.containsKey(mutablePos)){
            buffer.vertex(nnn).color(color);
            buffer.vertex(npn).color(color);
            buffer.vertex(ppn).color(color);
            buffer.vertex(pnn).color(color);
        }
        mutablePos.setZ(pos.getZ() + 1);
        if(!shapes.testPos(mutablePos) || !markedBlocks.containsKey(mutablePos)){
            buffer.vertex(nnp).color(color);
            buffer.vertex(pnp).color(color);
            buffer.vertex(ppp).color(color);
            buffer.vertex(npp).color(color);
        }
    }
    public interface DefaultColorMethod{
    
    }
    public static class ConfigListWithColorMethod extends LPCConfigList implements DefaultColorMethod{
        public ConfigListWithColorMethod(ILPCConfigBase parent, String nameKey) {super(parent, nameKey);}
        
    }
}
