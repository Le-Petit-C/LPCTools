package lpctools.compact.minihud;

import fi.dy.masa.minihud.renderer.shapes.*;
import lpctools.compact.derived.SimpleTestableShape;
import lpctools.compact.interfaces.ITestableShape;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
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
    private record ShapeBoxTester(AABB box) implements SimpleTestableShape.ShapeTester {
        @Override public boolean isInsideShape(int x, int y, int z) {
            return box.contains(x + 0.5, y + 0.5, z + 0.5);
        }
        @Override public boolean equals(Object object){
            if(object instanceof ShapeBoxTester(AABB box1))
                return box.equals(box1);
            else return false;
        }
        ShapeBoxTester(ShapeBox shapeBox){this(shapeBox.getBox());}
    }
    private record ShapeSphereTester(Vec3 effectiveCenter, double squaredRadius) implements SimpleTestableShape.ShapeTester {
        @Override public boolean isInsideShape(int x, int y, int z) {
            return effectiveCenter.subtract(x + 0.5, y + 0.5, z + 0.5).lengthSqr() <= squaredRadius;
        }
        @Override public boolean equals(Object object){
            if(object instanceof ShapeSphereTester(Vec3 center, double radius))
                return effectiveCenter.equals(center) && squaredRadius == radius;
            else return false;
        }
        ShapeSphereTester(ShapeSphereBlocky sphere){
            this(sphere.getEffectiveCenter(), sphere.getSquaredRadius());
        }
    }
    private record ShapeCircleTester(Vec3 effectiveCenter, double squaredRadius, int height) implements SimpleTestableShape.ShapeTester {
        @Override public boolean isInsideShape(int x, int y, int z) {
            if(effectiveCenter.y() > y + 0.5) return false;
            if(effectiveCenter.y() + height < y + 0.5) return false;
            return effectiveCenter.distanceToSqr(x + 0.5, effectiveCenter.y(), z + 0.5) <= squaredRadius;
        }
        @Override public boolean equals(Object object){
            if(object instanceof ShapeCircleTester(Vec3 center, double radius, int height1))
                return effectiveCenter.equals(center) && squaredRadius == radius && height == height1;
            else return false;
        }
        ShapeCircleTester(ShapeCircle circle){
            this(circle.getEffectiveCenter(), circle.getSquaredRadius(), circle.getHeight());
        }
    }
}
