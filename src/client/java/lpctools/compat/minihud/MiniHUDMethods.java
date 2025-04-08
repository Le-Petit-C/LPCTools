package lpctools.compat.minihud;

import fi.dy.masa.minihud.renderer.shapes.*;
import lpctools.compat.derived.SimpleTestableShape;
import lpctools.compat.interfaces.ITestableShape;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class MiniHUDMethods{
    public static MiniHUDMethods getInstance(){
        if(isLoaded) return instance;
        isLoaded = true;
        return instance = createInstance();
    }
    public void addShapes(Collection<ITestableShape> list, String namePrefix) {
        for(ShapeBase shape : ShapeManager.INSTANCE.getAllShapes()){
            String name = shape.getDisplayName();
            SimpleTestableShape.TestType testType = SimpleTestableShape.testTestType(name, namePrefix);
            if(testType == null) continue;
            if(shape instanceof ShapeCircleBase circleBase){
                if(circleBase instanceof ShapeSphereBlocky sphere)
                    list.add(ITestableShape.byTester((pos)->shapeSphereTest(sphere, pos), testType));
                else if (circleBase instanceof ShapeCircle circle)
                    list.add(ITestableShape.byTester((pos)->shapeCircleTest(circle, pos), testType));
            }
            else if(shape instanceof ShapeBox box)
                list.add(ITestableShape.byTester((pos)->shapeBoxTest(box, pos), testType));
        }
    }
    private boolean shapeBoxTest(ShapeBox box, BlockPos pos){
        return box.getBox().contains(pos.toCenterPos());
    }
    private boolean shapeSphereTest(ShapeCircleBase sphere, BlockPos pos){
        return sphere.getEffectiveCenter().subtract(pos.toCenterPos()).lengthSquared() <= sphere.getSquaredRadius();
    }
    private boolean shapeCircleTest(ShapeCircle circle, BlockPos pos){
        Vec3d center = circle.getEffectiveCenter();
        Vec3d blockCenter = pos.toCenterPos();
        if(center.getY() > blockCenter.getY()) return false;
        if(center.getY() + circle.getHeight() < blockCenter.getY()) return false;
        return center.squaredDistanceTo(blockCenter.getX(), center.getY(), blockCenter.getZ()) <= circle.getSquaredRadius();
    }
    private static boolean isLoaded = false;
    @Nullable private static MiniHUDMethods instance;
    @Nullable private static MiniHUDMethods createInstance(){
        if(FabricLoader.getInstance().isModLoaded("minihud")) return new MiniHUDMethods();
        else return null;
    }
}
