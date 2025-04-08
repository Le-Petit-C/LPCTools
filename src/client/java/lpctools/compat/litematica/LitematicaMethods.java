package lpctools.compat.litematica;

import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import fi.dy.masa.malilib.util.LayerRange;
import lpctools.compat.derived.SimpleTestableShape;
import lpctools.compat.interfaces.ITestableShape;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class LitematicaMethods {
    @Nullable public static LitematicaMethods getInstance(){
        if(isLoaded) return instance;
        isLoaded = true;
        return instance = createInstance();
    }
    private static boolean isLoaded = false;
    @Nullable private static LitematicaMethods instance;
    @Nullable private static LitematicaMethods createInstance(){
        if(FabricLoader.getInstance().isModLoaded("litematica")) return new LitematicaMethods();
        else return null;
    }
    public void addSchematicShapes(Collection<ITestableShape> list, String namePrefix){
        for(SchematicPlacement placement : DataManager.getSchematicPlacementManager().getAllSchematicsPlacements()){
            SimpleTestableShape.TestType testType = SimpleTestableShape.testTestType(placement.getName(), namePrefix);
            if(testType == null) continue;
            for(fi.dy.masa.litematica.selection.Box box : placement.getSchematic().getAreas().values()){
                Box box1 = toMinecraftBox(box);
                if(box1 == null) continue;
                Box finalBox = box1.offset(placement.getOrigin());
                list.add(ITestableShape.byTester(pos -> finalBox.contains(pos.toCenterPos()),testType));
            }
        }
    }
    public void addRenderRangeShape(Collection<ITestableShape> list, SimpleTestableShape.TestType testType){
        list.add(ITestableShape.byTester(pos -> {
            LayerRange range = DataManager.getRenderLayerRange();
            int component = range.getAxis().choose(pos.getX(), pos.getY(), pos.getZ());
            return component >= range.getLayerMin() && component <= range.getLayerMax();
            }, testType));
    }
    //因为投影里用的Box不带这个方法，所以需要自己实现
    @Nullable private static Box toMinecraftBox(fi.dy.masa.litematica.selection.Box box){
        BlockPos pos1 = box.getPos1(), pos2 = box.getPos2();
        if(pos1 == null || pos2 == null) return null;
        return new Box(pos1.toCenterPos(), pos2.toCenterPos()).expand(0.5);
    }
}
