package lpctools.compat.derived;

import lpctools.compat.interfaces.ITestableShape;
import lpctools.compat.litematica.LitematicaMethods;
import lpctools.compat.minihud.MiniHUDMethods;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;

//不是动态连接litematica和minihud内容的，在它们的显示区域删添时不会自动更新，所以每个需要此表的工具每游戏刻的检测都会新建此表
public class ShapeList extends ArrayList<ITestableShape> {
    public ShapeList(SimpleTestableShape.TestType litematicaRenderRangeTestType, String namePrefix){
        if(LitematicaMethods.getInstance() != null){
            LitematicaMethods.getInstance().addSchematicShapes(this, namePrefix);
            if(litematicaRenderRangeTestType != null)
                LitematicaMethods.getInstance().addRenderRangeShape(this, litematicaRenderRangeTestType);
        }
        if(MiniHUDMethods.getInstance() != null)
            MiniHUDMethods.getInstance().addShapes(this, namePrefix);
    }
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean testPos(BlockPos pos){
        return ITestableShape.testShapes(this, pos).getAsBoolean();
    }
    public static ShapeList emptyList(){
        return new ShapeList();
    }
    private ShapeList(){}
}
