package lpctools.compact.litematica;

import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.materials.MaterialListBase;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import fi.dy.masa.litematica.selection.AreaSelection;
import fi.dy.masa.malilib.util.position.LayerRange;
import lpctools.compact.derived.SimpleTestableShape;
import lpctools.compact.interfaces.ITestableShape;
import lpctools.util.data.Box3i;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

import static lpctools.util.DataUtils.toVector3i;

public class LitematicaMethods {
    public void addSchematicShapes(Collection<ITestableShape> list, String namePrefix){
        for(SchematicPlacement placement : DataManager.getSchematicPlacementManager().getAllSchematicsPlacements()){
            SimpleTestableShape.TestType testType = SimpleTestableShape.testTestType(placement.getName(), namePrefix);
            if(testType == null) continue;
            for(fi.dy.masa.litematica.selection.Box box : placement.getSchematic().getAreas().values()){
                AABB box1 = toMinecraftBox(box);
                if(box1 == null) continue;
                AABB finalBox = box1.move(placement.getOrigin());
                list.add(ITestableShape.byTester(new SimpleTestableShape.InsideBox(finalBox),testType));
            }
        }
    }
    public void addRenderRangeShape(Collection<ITestableShape> list, SimpleTestableShape.TestType testType){
        LayerRange currentRange = DataManager.getRenderLayerRange();
        list.add(ITestableShape.byTester(
            new LayerRangeTester(Direction.Axis.valueOf(currentRange.getAxis().name()), currentRange.getLayerRangeMin(), currentRange.getLayerRangeMax())
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
        @Override public boolean isInsideShape(int x, int y, int z) {
            int component = axis.choose(x, y, z);
            return component >= layerMin && component <= layerMax;
        }
        @Override public boolean equals(Object object){
            if(object instanceof LayerRangeTester(Direction.Axis axis1, int min, int max))
                return axis.equals(axis1) && layerMin == min && layerMax == max;
            else return false;
        }
    }
    //因为投影里用的Box不带这个方法，所以需要自己实现
    @Nullable private static AABB toMinecraftBox(fi.dy.masa.litematica.selection.Box box){
        BlockPos pos1 = box.getPos1(), pos2 = box.getPos2();
        if(pos1 == null || pos2 == null) return null;
        return new AABB(net.minecraft.world.phys.Vec3.atCenterOf(pos1), net.minecraft.world.phys.Vec3.atCenterOf(pos2)).inflate(0.5);
    }
    @Nullable private static Box3i toBox3i(fi.dy.masa.litematica.selection.Box box){
        BlockPos pos1 = box.getPos1(), pos2 = box.getPos2();
        if(pos1 == null || pos2 == null) return null;
        return new Box3i(toVector3i(pos1), toVector3i(pos2));
    }
    public @Nullable MaterialListBase getMaterialList(){
        return DataManager.getMaterialList();
    }
}
