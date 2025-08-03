package lpctools.compact.litematica;

import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import fi.dy.masa.litematica.selection.AreaSelection;
import fi.dy.masa.malilib.util.LayerRange;
import lpctools.compact.derived.SimpleTestableShape;
import lpctools.compact.interfaces.ITestableShape;
import lpctools.util.data.Box3i;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

import static lpctools.util.DataUtils.toVector3i;

public class LitematicaMethods {
    public void addSchematicShapes(Collection<ITestableShape> list, String namePrefix){
        for(SchematicPlacement placement : DataManager.getSchematicPlacementManager().getAllSchematicsPlacements()){
            SimpleTestableShape.TestType testType = SimpleTestableShape.testTestType(placement.getName(), namePrefix);
            if(testType == null) continue;
            for(fi.dy.masa.litematica.selection.Box box : placement.getSchematic().getAreas().values()){
                Box box1 = toMinecraftBox(box);
                if(box1 == null) continue;
                Box finalBox = box1.offset(placement.getOrigin());
                list.add(ITestableShape.byTester(new SimpleTestableShape.InsideBox(finalBox),testType));
            }
        }
    }
    public void addRenderRangeShape(Collection<ITestableShape> list, SimpleTestableShape.TestType testType){
        LayerRange currentRange = DataManager.getRenderLayerRange();
        list.add(ITestableShape.byTester(
            new LayerRangeTester(currentRange.getAxis(), currentRange.getLayerMin(), currentRange.getLayerMax())
            , testType));
    }
    public @Nullable Box3i getSelectionBox(){
        AreaSelection selection = DataManager.getSelectionManager().getCurrentSelection();
        if(selection == null) return null;
        fi.dy.masa.litematica.selection.Box box = selection.getSelectedSubRegionBox();
        if(box == null) return null;
        return toBox3i(box);
    }
    private record LayerRangeTester(Direction.Axis axis, int layerMin, int layerMax) implements SimpleTestableShape.ShapeTester {
        @Override public boolean isInsideShape(BlockPos pos) {
            int component = axis.choose(pos.getX(), pos.getY(), pos.getZ());
            return component >= layerMin && component <= layerMax;
        }
        @Override public boolean equals(Object object){
            if(object instanceof LayerRangeTester tester)
                return axis.equals(tester.axis) && layerMin == tester.layerMin && layerMax == tester.layerMax;
            else return false;
        }
    }
    //因为投影里用的Box不带这个方法，所以需要自己实现
    @Nullable private static Box toMinecraftBox(fi.dy.masa.litematica.selection.Box box){
        BlockPos pos1 = box.getPos1(), pos2 = box.getPos2();
        if(pos1 == null || pos2 == null) return null;
        return new Box(pos1.toCenterPos(), pos2.toCenterPos()).expand(0.5);
    }
    @Nullable private static Box3i toBox3i(fi.dy.masa.litematica.selection.Box box){
        BlockPos pos1 = box.getPos1(), pos2 = box.getPos2();
        if(pos1 == null || pos2 == null) return null;
        return new Box3i(toVector3i(pos1), toVector3i(pos2));
    }
}
