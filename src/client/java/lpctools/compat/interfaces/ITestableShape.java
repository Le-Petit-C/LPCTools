package lpctools.compat.interfaces;

import lpctools.compat.derived.SimpleTestableShape;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;

public interface ITestableShape {
    boolean defaultBoolean = true;
    enum ShapeTestResult implements BooleanSupplier {
        NO_OPERATION(()->defaultBoolean, 0),//初始状态，无操作
        DEFAULT_TRUE(()->true, 1),          //仅能覆盖NO_OPERATION的true，形状设为“裁剪”但是检测点不在形状内时应当返回此值
        DEFAULT_FALSE(()->false, 1),        //仅能覆盖NO_OPERATION的false，形状设为“添加”但是检测点不在形状内时应当返回此值
        SET_AS_TRUE(()->true, 2),           //将值设为true，形状设为“添加”且检测点在形状内时应当返回此值
        SET_AS_FALSE(()->false, 2);         //将值设为false，形状设为“裁剪”且检测点在形状内时应当返回此值
        @Override public boolean getAsBoolean() {return supplier.getAsBoolean();}
        public ShapeTestResult apply(ShapeTestResult value) {
            if(value.level >= 2 || value.level > level) return value;
            else return this;
        }
        private final BooleanSupplier supplier;
        private final int level;
        ShapeTestResult(BooleanSupplier supplier, int level){
            this.supplier = supplier;
            this.level = level;
        }
    }
    @NotNull ShapeTestResult shapeTestResult(BlockPos pos);
    static ShapeTestResult testShapes(Iterable<ITestableShape> shapes, BlockPos pos){
        ShapeTestResult result = ShapeTestResult.NO_OPERATION;
        for(ITestableShape shape : shapes)
            result = result.apply(shape.shapeTestResult(pos));
        return result;
    }
    static SimpleTestableShape byTester(SimpleTestableShape.ShapeTester tester, SimpleTestableShape.TestType testType){
        return new SimpleTestableShape(tester, testType);
    }
}
