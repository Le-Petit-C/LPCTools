package lpctools.compat.minihud;

import lpctools.compat.interfaces.IMinihudShape;
import lpctools.tools.liquidcleaner.LiquidCleaner;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;

public class ShapeList extends ArrayList<IMinihudShape> {
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean testPos(BlockPos pos){
        boolean b = !LiquidCleaner.limitCleaningRange.getAsBoolean();
        for(IMinihudShape shape : this){
            switch (shape.shapeTestResult(pos)){
                case IMinihudShape.ShapeTestResult.SET_AS_TRUE -> b = true;
                case IMinihudShape.ShapeTestResult.SET_AS_FALSE -> b = false;
            }
        }
        return b;
    }
}
