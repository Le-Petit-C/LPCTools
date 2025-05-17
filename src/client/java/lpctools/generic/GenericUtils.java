package lpctools.generic;

import net.minecraft.block.BlockState;
import net.minecraft.block.SideShapeType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.NotNull;

import static lpctools.generic.GenericConfigs.*;

public class GenericUtils {
    public static boolean mayMobSpawnAt(@NotNull BlockView world, BlockPos pos){
        BlockState block = world.getBlockState(pos);
        if(!block.getCollisionShape(world, pos).isEmpty()) return false;
        BlockState steppedBlock = world.getBlockState(pos.offset(Direction.DOWN));
        if(extraNoSpawnBlocks.contains(steppedBlock.getBlock())) return false;
        if(extraSpawnBlocks.contains(steppedBlock.getBlock())) return true;
        return steppedBlock.isSideSolid(world, pos, Direction.UP, SideShapeType.FULL);
    }
}
