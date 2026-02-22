package lpctools.compact.derived;

import lpctools.compact.interfaces.ITestableShape;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import static lpctools.compact.interfaces.ITestableShape.ShapeTestResult.*;

public class SimpleTestableShape implements ITestableShape {
    public interface ShapeTester{ boolean isInsideShape(int x, int y, int z);}
    public final @NotNull ShapeTester tester;
    public final TestType testType;
    public SimpleTestableShape(@NotNull ShapeTester tester, TestType testType){
        this.tester = tester;
        this.testType = testType;
    }
    @Override public @NotNull ShapeTestResult shapeTestResult(int x, int y, int z) {
        return testType.combineResult(tester.isInsideShape(x, y, z));
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
    public record InsideBox(Box box) implements ShapeTester{
        @Override public boolean isInsideShape(int x, int y, int z) {return box.contains(x + 0.5, y + 0.5, z + 0.5);}
        @Override public boolean equals(Object object){
            if(object instanceof InsideBox(Box box1))
                return box.equals(box1);
            else return false;
        }
    }
    @Override public boolean equals(Object object){
        if(object instanceof SimpleTestableShape shape)
            return tester.equals(shape.tester) && testType.equals(shape.testType);
        else return false;
    }
}
