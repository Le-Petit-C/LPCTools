package lpctools.tools.singletool;

import com.mojang.blaze3d.buffers.BufferUsage;
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
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.HashSet;

import static lpctools.util.MathUtils.inverseOffsetMatrix4f;

public class SlightXRay implements IValueRefreshCallback, WorldRenderEvents.Last {
    @NotNull final HashSet<BlockPos> markedBlocks = new HashSet<>();
    @NotNull final HashSet<Block> XRayBlocks = initHashset();
    private static HashSet<Block> initHashset(){
        HashSet<Block> blocks = new HashSet<>();
        blocks.add(Blocks.DIAMOND_ORE);
        blocks.add(Blocks.DEEPSLATE_DIAMOND_ORE);
        return blocks;
    }

    @Override public void valueRefreshCallback() {
        if(SingleTool.slightXRay.getAsBoolean()){
            if(Registry.registerWorldRenderLastCallback(this)){
                ClientWorld world = MinecraftClient.getInstance().world;
                ClientPlayerEntity player = MinecraftClient.getInstance().player;
                if(world == null || player == null) return;
                int distance = MinecraftClient.getInstance().options.getViewDistance().getValue();
                ChunkPos chunkPos = player.getChunkPos();
                for(int x = chunkPos.x - distance; x <= chunkPos.x + distance; ++x){
                    for(int z = chunkPos.z - distance; z <= chunkPos.z + distance; ++z){
                        if(world.isChunkLoaded(x, z))
                            loadChunk(world, new ChunkPos(x, z));
                    }
                }
            }
        }
        else {
            Registry.unregisterWorldRenderLastCallback(this);
            markedBlocks.clear();
        }
    }

    @Override public void onLast(WorldRenderContext context) {
        RenderContext ctx = new RenderContext(RenderPipelines.DEBUG_QUADS, BufferUsage.STATIC_WRITE);
        BufferBuilder buffer = ctx.getBuilder();
        Vector3f cam = context.camera().getPos().toVector3f();
        Matrix4f matrix = inverseOffsetMatrix4f(cam);
        for(BlockPos pos : markedBlocks)
            vertexBlock(matrix, buffer, pos, 0x3F3F7FFF);
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

    private void loadChunk(ClientWorld world, ChunkPos chunkPos){
        //TODO:优化算法
        for(BlockPos pos : BlockPos.iterate(
                chunkPos.getStartX(), world.getBottomY() - 1, chunkPos.getStartZ(),
                chunkPos.getEndX(), world.getBottomY() + world.getHeight() + 1, chunkPos.getEndZ()
        )){
            BlockState state = world.getBlockState(pos);
            if(!state.isOpaque() || state.isTransparent()){
                for(Direction direction : Direction.values()){
                    BlockPos testPos = pos.offset(direction);
                    if(XRayBlocks.contains(world.getBlockState(testPos).getBlock()))
                        markedBlocks.add(testPos);
                }
            }
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void vertexBlock(Matrix4f matrix, BufferBuilder buffer, BlockPos pos, int color){
        //TODO:高精度处理
        Vector3f posf = pos.toCenterPos().toVector3f();
        if(!markedBlocks.contains(pos.down())){
            buffer.vertex(matrix, posf.x - 0.5f, posf.y - 0.5f, posf.z - 0.5f).color(color);
            buffer.vertex(matrix, posf.x + 0.5f, posf.y - 0.5f, posf.z - 0.5f).color(color);
            buffer.vertex(matrix, posf.x + 0.5f, posf.y - 0.5f, posf.z + 0.5f).color(color);
            buffer.vertex(matrix, posf.x - 0.5f, posf.y - 0.5f, posf.z + 0.5f).color(color);
        }
        if(!markedBlocks.contains(pos.up())){
            buffer.vertex(matrix, posf.x - 0.5f, posf.y + 0.5f, posf.z - 0.5f).color(color);
            buffer.vertex(matrix, posf.x + 0.5f, posf.y + 0.5f, posf.z - 0.5f).color(color);
            buffer.vertex(matrix, posf.x + 0.5f, posf.y + 0.5f, posf.z + 0.5f).color(color);
            buffer.vertex(matrix, posf.x - 0.5f, posf.y + 0.5f, posf.z + 0.5f).color(color);
        }
    }
}
