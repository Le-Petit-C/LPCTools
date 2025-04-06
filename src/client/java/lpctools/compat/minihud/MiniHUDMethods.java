package lpctools.compat.minihud;

import fi.dy.masa.minihud.renderer.shapes.*;
import lpctools.compat.interfaces.IMinihudShape.ShapeTestResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import static lpctools.compat.interfaces.IMinihudShape.ShapeTestResult.*;

public class MiniHUDMethods{
    public MiniHUDMethods(){}
    public ShapeList getShapes(String namePrefix) {
        ShapeList list = new ShapeList();
        for(ShapeBase shape : ShapeManager.INSTANCE.getAllShapes()){
            String name = shape.getDisplayName();
            boolean invert;
            boolean cropping;
            if(name.startsWith(namePrefix + "+")){
                invert = false;
                cropping = false;
            }
            else if(name.startsWith(namePrefix + "-")){
                invert = false;
                cropping = true;
            }
            else if(name.startsWith(namePrefix + "+~")){
                invert = true;
                cropping = false;
            }
            else if(name.startsWith(namePrefix + "-~")){
                invert = true;
                cropping = true;
            }
            else continue;
            if(shape instanceof ShapeBox box)
                list.add((pos)->combineResult(shapeBoxTest(box, pos), invert, cropping));
            else if(shape instanceof ShapeCircleBase circleBase){
                if(circleBase instanceof ShapeSphereBlocky sphere){
                    list.add((pos)->combineResult(shapeSphereTest(sphere, pos), invert, cropping));
                }
                else if (circleBase instanceof ShapeCircle circle) {
                    list.add((pos)->combineResult(shapeCircleTest(circle, pos), invert, cropping));
                }
            }
        }
        return list;
    }
    private static ShapeTestResult combineResult(boolean isInsideShape, boolean invert, boolean cropping){
        return (isInsideShape == invert) ? NO_OPERATION : (cropping ? SET_AS_FALSE : SET_AS_TRUE);
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
}
