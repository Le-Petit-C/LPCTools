package lpctools.tools.tilingTool;

import com.google.common.collect.ImmutableSet;
import lpctools.compact.derived.ShapeList;
import lpctools.util.AlgorithmUtils;
import lpctools.util.DataUtils;
import lpctools.util.data.Box3i;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;

public class TilingToolData {
    static @Nullable TilingToolExecutor executor;
    static @Nullable StoredData storedData;
    static @NotNull ShapeList shapeList = ShapeList.emptyList();
    public static final HashMap<Block, ArrayList<ImmutableSet<Block>>> vagueBlocks = new HashMap<>();
    public static void refresh(Box3i box){storedData = StoredData.create(box);}
    public record StoredData(BlockPos startPos, Vec3i cuboidSize, Block[][][] storedBlocks){
        public static StoredData create(Box3i box){
            ClientLevel world = Minecraft.getInstance().level;
            if(world == null) return null;
            box = box.ensureMinMax(new Box3i());
            BlockPos startPos = DataUtils.toBlockPos(box.pos1);
            Vec3i cuboidSize = DataUtils.toBlockPos(box.pos2.sub(box.pos1).add(1, 1, 1));
            Block[][][] storedBlocks = new Block[cuboidSize.getZ()][cuboidSize.getY()][cuboidSize.getX()];
            for(BlockPos pos : AlgorithmUtils.iterateInBox(startPos, startPos.offset(cuboidSize).offset(-1, -1, -1))){
                storedBlocks[pos.getZ() - startPos.getZ()][pos.getY() - startPos.getY()][pos.getX() - startPos.getX()]
                    = world.getBlockState(pos).getBlock();
            }
            return new StoredData(startPos, cuboidSize, storedBlocks);
        }
    }
}
