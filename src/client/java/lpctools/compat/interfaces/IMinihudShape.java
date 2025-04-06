package lpctools.compat.interfaces;

import net.minecraft.util.math.BlockPos;

public interface IMinihudShape {
    enum ShapeTestResult{
        NO_OPERATION,
        SET_AS_TRUE,
        SET_AS_FALSE
    }
    ShapeTestResult shapeTestResult(BlockPos pos);
}
