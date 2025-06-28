package lpctools.compact.minihud;

import fi.dy.masa.minihud.renderer.shapes.*;
import lpctools.compact.derived.SimpleTestableShape;
import lpctools.compact.interfaces.ITestableShape;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
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
                    list.add(ITestableShape.byTester(new ShapeSphereTester(sphere), testType));
                else if (circleBase instanceof ShapeCircle circle)
                    list.add(ITestableShape.byTester(new ShapeCircleTester(circle), testType));
            }
            else if(shape instanceof ShapeBox box)
                list.add(ITestableShape.byTester(new ShapeBoxTester(box), testType));
        }
    }
    private record ShapeBoxTester(Box box) implements SimpleTestableShape.ShapeTester {
        @Override public boolean isInsideShape(BlockPos pos) {
            return box.contains(pos.toCenterPos());
        }
        @Override public boolean equals(Object object){
            if(object instanceof ShapeBoxTester tester)
                return box.equals(tester.box);
            else return false;
        }
        ShapeBoxTester(ShapeBox shapeBox){this(shapeBox.getBox());}
    }
    private record ShapeSphereTester(Vec3d effectiveCenter, double squaredRadius) implements SimpleTestableShape.ShapeTester {
        @Override public boolean isInsideShape(BlockPos pos) {
            return effectiveCenter.subtract(pos.toCenterPos()).lengthSquared() <= squaredRadius;
        }
        @Override public boolean equals(Object object){
            if(object instanceof ShapeSphereTester tester)
                return effectiveCenter.equals(tester.effectiveCenter) && squaredRadius == tester.squaredRadius;
            else return false;
        }
        ShapeSphereTester(ShapeSphereBlocky sphere){
            this(sphere.getEffectiveCenter(), sphere.getSquaredRadius());
        }
    }
    private record ShapeCircleTester(Vec3d effectiveCenter, double squaredRadius, int height) implements SimpleTestableShape.ShapeTester {
        @Override public boolean isInsideShape(BlockPos pos) {
            Vec3d blockCenter = pos.toCenterPos();
            if(effectiveCenter.getY() > blockCenter.getY()) return false;
            if(effectiveCenter.getY() + height < blockCenter.getY()) return false;
            return effectiveCenter.squaredDistanceTo(blockCenter.getX(), effectiveCenter.getY(), blockCenter.getZ()) <= squaredRadius;
        }
        @Override public boolean equals(Object object){
            if(object instanceof ShapeCircleTester tester)
                return effectiveCenter.equals(tester.effectiveCenter) && squaredRadius == tester.squaredRadius && height == tester.height;
            else return false;
        }
        ShapeCircleTester(ShapeCircle circle){
            this(circle.getEffectiveCenter(), circle.getSquaredRadius(), circle.getHeight());
        }
    }
}
