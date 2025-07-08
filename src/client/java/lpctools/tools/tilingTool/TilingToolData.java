package lpctools.tools.tilingTool;

import lpctools.util.AlgorithmUtils;
import lpctools.util.DataUtils;
import lpctools.util.data.Box3i;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

public class TilingToolData {
    static @Nullable TilingToolExecutor executor;
    static @Nullable StoredData storedData;
    public static void refresh(Box3i box){storedData = StoredData.create(box);}
    public record StoredData(BlockPos startPos, Vec3i cuboidSize, Block[][][] storedBlocks){
        public static StoredData create(Box3i box){
            ClientWorld world = MinecraftClient.getInstance().world;
            if(world == null) return null;
            BlockPos startPos = DataUtils.toBlockPos(box.pos1);
            Vec3i cuboidSize = DataUtils.toBlockPos(box.pos2.sub(box.pos1, new Vector3i()));
            Block[][][] storedBlocks = new Block[cuboidSize.getZ()][cuboidSize.getY()][cuboidSize.getX()];
            for(BlockPos pos : AlgorithmUtils.iterateInBox(startPos, startPos.add(cuboidSize))){
                storedBlocks[pos.getZ() - startPos.getZ()][pos.getY() - startPos.getY()][pos.getX() - startPos.getX()]
                    = world.getBlockState(pos).getBlock();
            }
            return new StoredData(startPos, cuboidSize, storedBlocks);
        }
    }
}
