package lpctools.tools.slightXRay;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import fi.dy.masa.malilib.render.MaLiLibPipelines;
import fi.dy.masa.malilib.render.RenderContext;
import fi.dy.masa.malilib.util.data.Color4f;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lpctools.LPCTools;
import lpctools.compact.derived.ShapeList;
import lpctools.generic.GenericUtils;
import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.Registry;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.ConfigListOptionListConfigEx;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.*;
import lpctools.lpcfymasaapi.implementations.ILPCConfigBase;
import lpctools.lpcfymasaapi.implementations.ILPCConfigList;
import lpctools.lpcfymasaapi.implementations.ILPCValueChangeCallback;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.RangeLimitConfig;
import lpctools.mixin.client.SpriteContentsMixin;
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
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.*;

import java.awt.*;
import java.lang.Math;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;
import static lpctools.tools.ToolUtils.*;
import static lpctools.util.AlgorithmUtils.*;
import static lpctools.util.BlockUtils.*;
import static lpctools.util.DataUtils.*;
import static lpctools.util.MathUtils.*;

public class SlightXRay implements ILPCValueChangeCallback, WorldRenderEvents.End, ClientChunkEvents.Load, ClientChunkEvents.Unload, ClientWorldEvents.AfterClientWorldChange, Registry.ClientWorldChunkSetBlockState {
    //以下几个变量使用synchronized(markedBlocks)同步
    //标记的需要显示的区块
    static final @NotNull HashMap<BlockPos, MutableInt> markedBlocks = new HashMap<>();
    //加载过或正要加载的区块
    static final @NotNull HashSet<ChunkPos> loadedOrLoadingChunks = new HashSet<>();
    //以上几个变量使用synchronized(markedBlocks)同步
    static final @NotNull ImmutableList<Block> defaultXRayBlocks = ImmutableList.of(
        Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE,
        Blocks.DEEPSLATE_COAL_ORE, Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE,
        Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN, Blocks.ENDER_CHEST, Blocks.REINFORCED_DEEPSLATE,
        Blocks.BUDDING_AMETHYST, Blocks.CALCITE,
        Blocks.ANCIENT_DEBRIS
    );
    static final @NotNull HashMap<Block, MutableInt> XRayBlocks;
    static final @NotNull ImmutableList<String> defaultXRayBlockIds = idListFromBlockList(defaultXRayBlocks);
    public static BooleanHotkeyConfig slightXRay;
    public static ConfigListOptionListConfigEx<ConfigListWithColorMethod> defaultColorMethod;
    public static ColorConfig defaultColor;
    public static StringListConfig XRayBlocksConfig;
    public static RangeLimitConfig displayRange;
    public static DoubleConfig saturationDelta;
    public static DoubleConfig brightnessDelta;
    public static IntegerConfig defaultAlpha;
    static {
        XRayBlocks = new HashMap<>();
        for(Block block : defaultXRayBlocks)
            XRayBlocks.put(block, new MutableInt(0));
    }

    public static void init(){
        slightXRay = addBooleanHotkeyConfig("slightXRay", false, null, new SlightXRay());
        setLPCToolsToggleText(slightXRay);
        defaultColorMethod = peekConfigList().addConfig(
            new ConfigListOptionListConfigEx<>(peekConfigList(), "defaultColorMethod", SlightXRay::refreshXRayBlocks){
                @Override public void setValueFromJsonElement(@NotNull JsonElement element) {
                    super.setValueFromJsonElement(element);
                    onValueChanged();
                }
            });
        ILPCConfigList byDefaultColor = defaultColorMethod.addList(
            new ConfigListWithColorBase(defaultColorMethod, "byDefaultColor", block->defaultColor.getIntegerValue()) {
        });
        defaultColor = addColorConfig(byDefaultColor, "defaultColor", new Color4f(127, 127, 255, 127), SlightXRay::refreshXRayBlocks);
        ILPCConfigList byTextureColor = defaultColorMethod.addList(
            new ConfigListWithColorBase(defaultColorMethod, "byTextureColor", SlightXRay::getColorByTextureColor)
        );
        defaultAlpha = addIntegerConfig(byTextureColor, "defaultAlpha", 127, 0, 255, SlightXRay::refreshXRayBlocks);
        saturationDelta = addDoubleConfig(byTextureColor, "saturationDelta", 1, -5, 5, SlightXRay::refreshXRayBlocks);
        brightnessDelta = addDoubleConfig(byTextureColor, "brightnessDelta", 1, -5, 5, SlightXRay::refreshXRayBlocks);
        XRayBlocksConfig = addStringListConfig("XRayBlocks", defaultXRayBlockIds, SlightXRay::refreshXRayBlocks);
        displayRange = addRangeLimitConfig(false);
    }
    
    private static int getColorByTextureColor(Block block) {
        try{
            BlockStateModel model = MinecraftClient.getInstance().getBlockRenderManager()
                .getModel(block.getDefaultState());
            Sprite particleSprite = model.particleSprite();
            float r = 0, g = 0, b = 0;
            float t = 0;
            for(NativeImage image : ((SpriteContentsMixin)particleSprite.getContents()).getMipmapLevelsImages()){
                for(int color : image.copyPixelsArgb()){
                    float k = (color >>> 24) / 255.0f;
                    r += ((color >>> 16) & 0xff) * k;
                    g += ((color >>> 8) & 0xff) * k;
                    b += (color & 0xff) * k;
                    t += k;
                }
            }
            if(t == 0) return 0x7f000000;
            int ri = Math.round(r / t);
            int gi = Math.round(g / t);
            int bi = Math.round(b / t);
            float[] hsb = Color.RGBtoHSB(ri, gi, bi, new float[3]);
            hsb[1] = (float) Math.tanh(atanh(hsb[1] * 2 - 1) + saturationDelta.getAsDouble()) * 0.5f + 0.5f;
            hsb[2] = (float) Math.tanh(atanh(hsb[2] * 2 - 1) + brightnessDelta.getAsDouble()) * 0.5f + 0.5f;
            return (Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]) & 0x00ffffff) | (defaultAlpha.getAsInt() << 24);
        }
        catch (Exception e){return 0x7f000000;}
    }
    
    public static double atanh(double x) {
        if (Math.abs(x) > 1) throw new IllegalArgumentException("atanh: input value out of bound [-1, 1]");
        return 0.5 * Math.log((1 + x) / (1 - x));
    }

    private static void addAllRenderRegionsIntoWork(){
        ClientWorld world = MinecraftClient.getInstance().world;
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if(world == null || player == null) return;
        //int distance = MinecraftClient.getInstance().options.getViewDistance().getValue();
        ChunkPos chunkPos = player.getChunkPos();
        for(Vector2i vec : iterateFromClosest(toVector2i(chunkPos))){
            Chunk chunk = world.getChunk(vec.x, vec.y, ChunkStatus.FULL, false);
            if(chunk == null) break;
            updateChunkInCompletableFuture(world, chunk, false);
        }
    }

    private static void refreshXRayBlocks(){
        if(XRayBlocksConfig == null) return;
        HashMap<Block, MutableInt> newBlocks = new HashMap<>();
        for(String str : XRayBlocksConfig){
            String[] splits = str.split(";");
            if(splits.length > 0 && splits.length < 3){
                Block block = getBlockFromId(splits[0], true);
                if(block == null) continue;
                Integer color = null;
                if(splits.length > 1) {
                    try {
                        color = Integer.parseUnsignedInt(splits[1], 16);
                    }catch (NumberFormatException e){
                        warnInvalidString(str);
                        continue;
                    }
                }
                if(color == null) color = defaultColorMethod.getCurrentUserdata().getColor(block);
                newBlocks.put(block, new MutableInt(color));
            }
            else warnInvalidString(str);
        }
        if(XRayBlocks.keySet().equals(newBlocks.keySet())) {
            for(Map.Entry<Block, MutableInt> block : newBlocks.entrySet())
                XRayBlocks.get(block.getKey()).setValue(block.getValue());
            return;
        }
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
            Registry.registerClientChunkLoadCallback(this);
            Registry.registerClientChunkUnloadCallback(this);
            Registry.registerClientWorldChangeCallback(this);
            Registry.registerClientWorldChunkSetBlockStateCallback(this);
            displayEnableMessage(slightXRay);
        }
        else {
            if(Registry.unregisterWorldRenderEndCallback(this))
                clearAll();
            Registry.unregisterClientChunkLoadCallback(this);
            Registry.unregisterClientChunkUnloadCallback(this);
            Registry.unregisterClientWorldChangeCallback(this);
            Registry.unregisterClientWorldChunkSetBlockStateCallback(this);
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
            for(Map.Entry<BlockPos, MutableInt> pos : markedBlocks.entrySet()){
                if(shapes.testPos(pos.getKey())){
                    paintMethods.vertexBlock(matrix, buffer, pos.getKey(), pos.getValue().intValue(), shapes);
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
    @Override public void onChunkLoad(ClientWorld world, WorldChunk worldChunk) {
        updateChunkInCompletableFuture(world, worldChunk, false);
        ChunkPos pos = worldChunk.getPos();
        Chunk chunk;
        chunk = world.getChunk(pos.x - 1, pos.z, ChunkStatus.FULL, false);
        if(chunk != null) updateChunkInCompletableFuture(world, chunk, false);
        chunk = world.getChunk(pos.x, pos.z - 1, ChunkStatus.FULL, false);
        if(chunk != null) updateChunkInCompletableFuture(world, chunk, false);
        chunk = world.getChunk(pos.x + 1, pos.z, ChunkStatus.FULL, false);
        if(chunk != null) updateChunkInCompletableFuture(world, chunk, false);
        chunk = world.getChunk(pos.x, pos.z + 1, ChunkStatus.FULL, false);
        if(chunk != null) updateChunkInCompletableFuture(world, chunk, false);
    }
    @Override public void onChunkUnload(ClientWorld world, WorldChunk worldChunk) {
        updateChunkInCompletableFuture(world, worldChunk, true);
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
        CompletableFuture<?>[] futures = new CompletableFuture<?>[threads.size()];
        synchronized (threadTasks){
            int n = 0;
            for(CompletableFuture<?> future : threads.values()){
                future.cancel(false);
                futures[n++] = future;
            }
            threadTasks.clear();
        }
        CompletableFuture.allOf(futures);
        synchronized (markedBlocks){
            markedBlocks.clear();
            loadedOrLoadingChunks.clear();
        }
    }

    @Override public void onClientWorldChunkSetBlockState(WorldChunk chunk, BlockPos pos, BlockState lastState, BlockState newState){
        if(newState == null) newState = Blocks.AIR.getDefaultState();
        if(isFluid(newState.getBlock())) return;
        if(doShowAround(newState)){
            for(BlockPos pos1 : iterateInManhattanDistance(pos, 2))
                testPos(chunk.getWorld(), pos1);
        }
        else testPos(chunk.getWorld(), pos);
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
                    markedBlocks.put(pos.toImmutable(), XRayBlocks.get(state.getBlock()));
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
            public MutableInt color = null;
            void set(BlockState state){
                doShowAround = doShowAround(state);
                Block block = state.getBlock();
                color = XRayBlocks.get(block);
                if(state.getBlock() == Blocks.VOID_AIR)
                    doShowAround = false;
            }
            @SuppressWarnings("SameParameterValue")
            void set(boolean doShowAround, MutableInt color){
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

    private static void updateChunk(XRayNecessaryState states, ThreadTask task){
        HashMap<BlockPos, MutableInt> result = new HashMap<>();
        Chunk chunk = task.chunk;
        World world = task.world;
        ThreadTask.NearbyChunks nearbyChunks = task.nearbyChunks;
        ChunkPos chunkPos = chunk.getPos();
        int cx = chunkPos.x, cz = chunkPos.z;
        if(nearbyChunks == null){
            //表示当前操作是卸载区块
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
                entry.getValue().set(doShowAround, null);
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
        Chunk nxChunk = nearbyChunks.nx;
        Chunk pxChunk = nearbyChunks.px;
        Chunk nzChunk = nearbyChunks.nz;
        Chunk pzChunk = nearbyChunks.pz;
        mutableBlockPos.setX(15);
        for(int y = minY; y < topY; ++y){
            mutableBlockPos.setY(y);
            for(int z = 0; z < 16; ++z){
                mutableBlockPos.setZ(z);
                states.data[1][y - minY + 2][z + 2].set(nxChunk.getBlockState(mutableBlockPos));
            }
        }
        mutableBlockPos.setX(0);
        for(int y = minY; y < topY; ++y){
            mutableBlockPos.setY(y);
            for(int z = 0; z < 16; ++z){
                mutableBlockPos.setZ(z);
                states.data[18][y - minY + 2][z + 2].set(pxChunk.getBlockState(mutableBlockPos));
            }
        }
        mutableBlockPos.setZ(15);
        for(int y = minY; y < topY; ++y){
            mutableBlockPos.setY(y);
            for(int x = 0; x < 16; ++x){
                mutableBlockPos.setX(x);
                states.data[x + 2][y - minY + 2][1].set(nzChunk.getBlockState(mutableBlockPos));
            }
        }
        mutableBlockPos.setZ(0);
        for(int y = minY; y < topY; ++y){
            mutableBlockPos.setY(y);
            for(int x = 0; x < 16; ++x){
                mutableBlockPos.setX(x);
                states.data[x + 2][y - minY + 2][18].set(pzChunk.getBlockState(mutableBlockPos));
            }
        }
        BlockPos chunkStartPos = chunkPos.getStartPos().add(0, chunk.getBottomY(), 0);
        //检测并加入过关数据
        for(Map.Entry<BlockPos, XRayNecessaryState.Data> data : states.iterateIn(1)){
            if(!data.getValue().doShowAround) continue;
            for(Direction direction : Direction.values()){
                BlockPos pos = data.getKey().offset(direction);
                markData(result, pos, chunkStartPos, states.get(pos));
            }
        }
        synchronized (markedBlocks){
            markedBlocks.putAll(result);
        }
    }
    private static void markData(HashMap<BlockPos, MutableInt> result, BlockPos blockPos, BlockPos chunkStartPos, XRayNecessaryState.Data data){
        BlockPos pos = chunkStartPos.add(blockPos);
        if(data.color != null)
            result.put(pos.toImmutable(), data.color);
    }

    //此处几个数据使用synchronized(threadTasks)同步
    private static final HashSet<ThreadTask> threadTasks = new HashSet<>();
    private static final Int2ObjectOpenHashMap<CompletableFuture<?>> threads = new Int2ObjectOpenHashMap<>();
    private static int threadTaskId = 0;
    
    private record ThreadTask(@NotNull ClientWorld world, @NotNull Chunk chunk, @Nullable NearbyChunks nearbyChunks){
        @Override public int hashCode(){
            return chunk.getPos().hashCode();
        }
        @Override public boolean equals(Object o){
            if(o == this) return true;
            if(o instanceof ThreadTask task)
                return task.world.equals(world)
                    && task.chunk.equals(chunk)
                    && Objects.equals(task.nearbyChunks, nearbyChunks);
            return false;
        }
        public record NearbyChunks(@NotNull Chunk nx,@NotNull  Chunk nz,@NotNull  Chunk px,@NotNull  Chunk pz){}
    }

    //更新当前区块，如果区块更新过或者需要加载但是相邻有未加载的区块则不作操作
    private static void updateChunkInCompletableFuture(ClientWorld world, Chunk chunk, boolean unload){
        ChunkPos pos = chunk.getPos();
        ThreadTask.NearbyChunks nearbyChunks;
        synchronized (markedBlocks){
            if(loadedOrLoadingChunks.contains(pos) != unload) return;
            if(unload) {
                loadedOrLoadingChunks.remove(pos);
                nearbyChunks = null;
            }
            else {
                Chunk nx = world.getChunk(pos.x - 1, pos.z, ChunkStatus.FULL, false);
                Chunk nz = world.getChunk(pos.x, pos.z - 1, ChunkStatus.FULL, false);
                Chunk px = world.getChunk(pos.x + 1, pos.z, ChunkStatus.FULL, false);
                Chunk pz = world.getChunk(pos.x, pos.z + 1, ChunkStatus.FULL, false);
                if(hasNull(nx, nz, px, pz)) return;
                loadedOrLoadingChunks.add(pos);
                //noinspection DataFlowIssue
                nearbyChunks = new ThreadTask.NearbyChunks(nx, nz, px, pz);
            }
        }
        synchronized (threadTasks){
            threadTasks.add(new ThreadTask(world, chunk, nearbyChunks));
            int currentId = threadTaskId++;
            threads.put(currentId, GenericUtils.runAsync(()->threadTask(currentId)));
        }
    }
    private static void threadTask(int me){
        ThreadTask task = null;
        double minDistance = Double.MAX_VALUE;
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        Vec3i playerPos = player == null ? Vec3i.ZERO : player.getBlockPos();
        synchronized (threadTasks){
            threads.remove(me);
            for(ThreadTask task1 : threadTasks){
                double distance = task1.chunk.getPos().getCenterAtY(playerPos.getY()).getSquaredDistance(playerPos);
                if(distance < minDistance){
                    minDistance = distance;
                    task = task1;
                }
            }
            if(task == null) return;
            threadTasks.remove(task);
        }
        XRayNecessaryState stateBuffer = new XRayNecessaryState(task.world.getHeight());
        updateChunk(stateBuffer, task);
    }

    public interface DefaultColorMethod{
        int getColor(Block block);
    }
    public interface ConfigListWithColorMethod extends ILPCConfigList, DefaultColorMethod{}
    public static class ConfigListWithColorBase extends LPCConfigList implements ConfigListWithColorMethod{
        public ConfigListWithColorBase(ILPCConfigBase parent, String nameKey, @NotNull DefaultColorMethod defaultColorMethod) {
            super(parent, nameKey);
            this.defaultColorMethod = defaultColorMethod;
        }
        @Override public int getColor(Block block) {
            return defaultColorMethod.getColor(block);
        }
        @NotNull DefaultColorMethod defaultColorMethod;
    }
    private final SlightXRayPaintMethods paintMethods = new SlightXRayPaintMethods();
}
