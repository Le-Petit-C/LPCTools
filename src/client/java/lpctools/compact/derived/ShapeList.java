package lpctools.compact.derived;

import lpctools.compact.CompactMain;
import lpctools.compact.interfaces.ITestableShape;
import lpctools.util.Packed;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;

//不是动态连接litematica和minihud内容的，在它们的显示区域删添时不会自动更新，所以每个需要此表的工具每游戏刻的检测都会新建此表
public class ShapeList extends ArrayList<ITestableShape> {
    public ShapeList(SimpleTestableShape.TestType litematicaRenderRangeTestType, String namePrefix){
        if(CompactMain.getLitematicaInstance() != null){
            CompactMain.getLitematicaInstance().addSchematicShapes(this, namePrefix);
            if(litematicaRenderRangeTestType != null)
                CompactMain.getLitematicaInstance().addRenderRangeShape(this, litematicaRenderRangeTestType);
        }
        if(CompactMain.getMinihudInstance() != null)
            CompactMain.getMinihudInstance().addShapes(this, namePrefix);
    }
    public boolean testPos(int x, int y, int z) {
        return ITestableShape.testShapes(this, x, y, z).getAsBoolean();
    }
    public boolean testPos(BlockPos pos){
        return testPos(pos.getX(), pos.getY(), pos.getZ());
    }
    public boolean testPos(long packedBlockPos) {
        return testPos(Packed.BlockPos.unpackX(packedBlockPos), Packed.BlockPos.unpackY(packedBlockPos), Packed.BlockPos.unpackZ(packedBlockPos));
    }
    public static ShapeList emptyList(){
        return new ShapeList();
    }
    private ShapeList(){}
}
