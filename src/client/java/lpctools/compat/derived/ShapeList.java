package lpctools.compat.derived;

import lpctools.compat.interfaces.IMinihudShape;
import lpctools.compat.litematica.LitematicaMethods;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;

public class ShapeList extends ArrayList<IMinihudShape> {
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean testPos(boolean testLitematica, BlockPos pos){
        boolean b;
        if(!testLitematica || LitematicaMethods.getInstance() == null) b = false;
        else b = LitematicaMethods.getInstance().isInsideRenderRange(pos);
        for(IMinihudShape shape : this){
            switch (shape.shapeTestResult(pos)){
                case IMinihudShape.ShapeTestResult.SET_AS_TRUE -> b = true;
                case IMinihudShape.ShapeTestResult.SET_AS_FALSE -> b = false;
            }
        }
        return b;
    }
}
