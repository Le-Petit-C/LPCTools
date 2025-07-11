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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class TilingToolData {
    static @Nullable TilingToolExecutor executor;
    static @Nullable StoredData storedData;
    public static final HashMap<Block, ArrayList<HashSet<Block>>> vagueBlocks = new HashMap<>();
    public static void refresh(Box3i box){storedData = StoredData.create(box);}
    public record StoredData(BlockPos startPos, Vec3i cuboidSize, Block[][][] storedBlocks){
        public static StoredData create(Box3i box){
            ClientWorld world = MinecraftClient.getInstance().world;
            if(world == null) return null;
            box = box.ensureMinMax(new Box3i());
            BlockPos startPos = DataUtils.toBlockPos(box.pos1);
            Vec3i cuboidSize = DataUtils.toBlockPos(box.pos2.sub(box.pos1).add(1, 1, 1));
            Block[][][] storedBlocks = new Block[cuboidSize.getZ()][cuboidSize.getY()][cuboidSize.getX()];
            for(BlockPos pos : AlgorithmUtils.iterateInBox(startPos, startPos.add(cuboidSize).add(-1, -1, -1))){
                storedBlocks[pos.getZ() - startPos.getZ()][pos.getY() - startPos.getY()][pos.getX() - startPos.getX()]
                    = world.getBlockState(pos).getBlock();
            }
            return new StoredData(startPos, cuboidSize, storedBlocks);
        }
    }
}
