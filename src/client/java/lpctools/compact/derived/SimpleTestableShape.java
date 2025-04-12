package lpctools.compact.derived;

import lpctools.compact.interfaces.ITestableShape;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import static lpctools.compact.interfaces.ITestableShape.ShapeTestResult.*;

public class SimpleTestableShape implements ITestableShape {
    public interface ShapeTester{ boolean isInsideShape(BlockPos pos);}
    @NotNull public ShapeTester tester;
    TestType testType;
    public SimpleTestableShape(@NotNull ShapeTester tester, TestType testType){
        this.tester = tester;
        this.testType = testType;
    }
    @Override public @NotNull ShapeTestResult shapeTestResult(BlockPos pos) {
        return testType.combineResult(tester.isInsideShape(pos));
    }
    @Nullable public static TestType testTestType(String name, String namePrefix){
        for(TestType testType : TestType.values())
            if(name.startsWith(testType.getPrefix() + namePrefix))
                return testType;
        return null;
    }
    public static class TestType {
        public final boolean invert, cropping;
        ShapeTestResult combineResult(boolean isInsideShape){
            return (isInsideShape == invert) ?
                    (cropping ? DEFAULT_TRUE : DEFAULT_FALSE): (cropping ? SET_AS_FALSE : SET_AS_TRUE);
        }
        public static Iterable<TestType> values(){return values;}
        public String getPrefix(){
            if(cropping) return invert ? "-~" : "-";
            else return invert ? "+~" : "+";
        }
        @SuppressWarnings("unused")
        public static TestType of(boolean invert, boolean cropping){
            return values.get((invert ? 2 : 0) + (cropping ? 1 : 0));
        }
        private TestType(boolean invert, boolean cropping){
            this.invert = invert;
            this.cropping = cropping;
        }
        private static TestType newOf(boolean invert, boolean cropping){
            return new TestType(invert, cropping);
        }
        private static final ArrayList<TestType> values = initValues();
        private static ArrayList<TestType> initValues(){
            ArrayList<TestType> list = new ArrayList<>();
            list.add(newOf(false, false));
            list.add(newOf(false, true));
            list.add(newOf(true, false));
            list.add(newOf(true, true));
            return list;
        }
    }
}
