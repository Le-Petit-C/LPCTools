package lpctools.compact.minihud;

import fi.dy.masa.minihud.renderer.shapes.*;
import lpctools.compact.derived.SimpleTestableShape;
import lpctools.compact.interfaces.ITestableShape;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Collection;

public class MiniHUDMethods{
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
    private boolean shapeSphereTest(ShapeSphereBlocky sphere, BlockPos pos){
        return sphere.getEffectiveCenter().subtract(pos.toCenterPos()).lengthSquared() <= sphere.getSquaredRadius();
    }
    private boolean shapeCircleTest(ShapeCircle circle, BlockPos pos){
        Vec3d center = circle.getEffectiveCenter();
        Vec3d blockCenter = pos.toCenterPos();
        if(center.getY() > blockCenter.getY()) return false;
        if(center.getY() + circle.getHeight() < blockCenter.getY()) return false;
        return center.squaredDistanceTo(blockCenter.getX(), center.getY(), blockCenter.getZ()) <= circle.getSquaredRadius();
    }
}
