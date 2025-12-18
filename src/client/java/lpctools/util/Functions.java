package lpctools.util;

import com.google.common.collect.ImmutableMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class Functions {
	public static final EqualsSign EQUALS = new EqualsSign();
	public static final NEqualSign NEQUAL = new NEqualSign();
	public static final LessSign LESS = new LessSign();
	public static final GreaterSign GREATER = new GreaterSign();
	public static final LEqualSign LEQUAL = new LEqualSign();
	public static final GEqualSign GEQUAL = new GEqualSign();
	public static final AddSign ADD = new AddSign();
	public static final SubtractSign SUBTRACT = new SubtractSign();
	public static final MultiplySign MULTIPLY = new MultiplySign();
	public static final DivideSign DIVIDE = new DivideSign();
	public static final ModSign MOD = new ModSign();
	public static final AndSign AND = new AndSign();
	public static final OrSign OR = new OrSign();
	public static final XOrSign XOR = new XOrSign();
	public static final ShiftLeftSign SHIFT_LEFT = new ShiftLeftSign();
	public static final ShiftRightSign SHIFT_RIGHT = new ShiftRightSign();
	public static final CrossSign CROSS = new CrossSign();
	public static final DotSign DOT = new DotSign();
	public static final DistanceSquared DISTANCE_SQUARED = new DistanceSquared();
	public static final Distance DISTANCE = new Distance();
	public static final Pi PI = new Pi();
	public static final E E = new E();
	public static final PiOver2 PI_OVER_2 = new PiOver2();
	public static final Sqrt2 SQRT_2 = new Sqrt2();
	public static final Phi PHI = new Phi();
	public static final Euler EULER = new Euler();
	public static final NegativeFunction NEGATIVE = new NegativeFunction();
	public static final AbsFunction ABS = new AbsFunction();
	public static final SignFunction SIGN = new SignFunction();
	public static final SquareFunction SQUARE = new SquareFunction();
	public static final FactorialFunction FACTORIAL = new FactorialFunction();
	public static final Floor FLOOR = new Floor();
	public static final Ceil CEIL = new Ceil();
	public static final Round ROUND = new Round();
	public static final Trunc TRUNC = new Trunc();
	public static final SqrtFunction SQRT = new SqrtFunction();
	public static final ExpFunction EXP = new ExpFunction();
	public static final LnFunction LN = new LnFunction();
	public static final SinFunction SIN = new SinFunction();
	public static final CosFunction COS = new CosFunction();
	public static final TanFunction TAN = new TanFunction();
	public static final ArcsinFunction ARCSIN = new ArcsinFunction();
	public static final ArccosFunction ARCCOS = new ArccosFunction();
	public static final ArctanFunction ARCTAN = new ArctanFunction();
	public static final SinhFunction SINH = new SinhFunction();
	public static final CoshFunction COSH = new CoshFunction();
	public static final GcdFunction GCD = new GcdFunction();
	public static final LcdFunction LCD = new LcdFunction();
	public static final CombineFunction COMBINE = new CombineFunction();
	public static final ArrangeFunction ARRANGE = new ArrangeFunction();
	public static final PowFunction POW = new PowFunction();
	public static final ModPowFunction MOD_POW = new ModPowFunction();
	public static final ModInverseFunction MOD_INVERSE = new ModInverseFunction();
	public static final CoordinateX COORDINATE_X = new CoordinateX();
	public static final CoordinateY COORDINATE_Y = new CoordinateY();
	public static final CoordinateZ COORDINATE_Z = new CoordinateZ();
	public static final SquaredLength SQUARED_LENGTH = new SquaredLength();
	public static final Length LENGTH = new Length();
	
	public interface SignBase{
		String displayString();
		default String idString() {return displayString();}
	}
	
	//比较符号
	public static final FunctionsDefaultSignInfo<IntegerCompareSign> integerCompareSignInfo = new FunctionsDefaultSignInfo<>(IntegerCompareSign.class);
	public interface IntegerCompareSign extends SignBase { boolean compareIntegers(int i1, int i2);}
	
	public static final FunctionsDefaultSignInfo<DoubleCompareSign> doubleCompareSignInfo = new FunctionsDefaultSignInfo<>(DoubleCompareSign.class);
	public interface DoubleCompareSign extends SignBase { boolean compareDoubles(double f1, double f2);}
	
	public static final FunctionsDefaultSignInfo<ObjectCompareSign> objectCompareSignInfo = new FunctionsDefaultSignInfo<>(ObjectCompareSign.class);
	public interface ObjectCompareSign extends SignBase{ boolean compareObjects(Object o1, Object o2);}
	
	//计算符号
	public static final FunctionsDefaultSignInfo<IntegerCalculateSign> integerCalculateSignInfo = new FunctionsDefaultSignInfo<>(IntegerCalculateSign.class);
	public interface IntegerCalculateSign extends SignBase{ int calculateIntegers(int i1, int i2);}
	
	public static final FunctionsDefaultSignInfo<DoubleCalculateSign> doubleCalculateSignInfo = new FunctionsDefaultSignInfo<>(DoubleCalculateSign.class);
	public interface DoubleCalculateSign extends SignBase{ double calculateDoubles(double f1, double f2);}
	
	public static final FunctionsDefaultSignInfo<BlockPosCalculateSign> blockPosCalculateSignInfo = new FunctionsDefaultSignInfo<>(BlockPosCalculateSign.class);
	public interface BlockPosCalculateSign extends SignBase{ BlockPos calculateBlockPoses(BlockPos p1, BlockPos p2);}
	
	public static final FunctionsDefaultSignInfo<Vec3dCalculateSign> vec3dCalculateSignInfo = new FunctionsDefaultSignInfo<>(Vec3dCalculateSign.class);
	public interface Vec3dCalculateSign extends SignBase{ Vec3d calculateVec3ds(Vec3d v1, Vec3d v2);}
	
	//混合计算符号
	public static final FunctionsDefaultSignInfo<IntegerFromBlockPosesSign> intFromBlockPosesSignInfo = new FunctionsDefaultSignInfo<>(IntegerFromBlockPosesSign.class);
	public interface IntegerFromBlockPosesSign extends SignBase{ int intFromBlockPoses(BlockPos p1, BlockPos p2);}
	
	public static final FunctionsDefaultSignInfo<DoubleFromVec3dsSign> doubleFromVec3dsSignInfo = new FunctionsDefaultSignInfo<>(DoubleFromVec3dsSign.class);
	public interface DoubleFromVec3dsSign extends SignBase{ double doubleFromVec3ds(Vec3d v1, Vec3d v2);}
	
	//拓展函数
	public static final FunctionsDefaultSignInfo<DoubleConstant> doubleConstantInfo = new FunctionsDefaultSignInfo<>(DoubleConstant.class);
	public interface DoubleConstant extends SignBase{ double getDouble();}
	
	public static final FunctionsDefaultSignInfo<IntegerFunction> integerFunctionInfo = new FunctionsDefaultSignInfo<>(IntegerFunction.class);
	public interface IntegerFunction extends SignBase{ int applyInteger(int i);}
	
	public static final FunctionsDefaultSignInfo<DoubleFunction> doubleFunctionInfo = new FunctionsDefaultSignInfo<>(DoubleFunction.class);
	public interface DoubleFunction extends SignBase{ double applyDouble(double f);}
	
	public static final FunctionsDefaultSignInfo<IntegerBiFunction> integerBiFunctionInfo = new FunctionsDefaultSignInfo<>(IntegerBiFunction.class);
	public interface IntegerBiFunction extends SignBase{ int apply2Integers(int i1, int i2);}
	
	public static final FunctionsDefaultSignInfo<DoubleBiFunction> doubleBiFunctionInfo = new FunctionsDefaultSignInfo<>(DoubleBiFunction.class);
	public interface DoubleBiFunction extends SignBase{ double apply2Doubles(double f1, double f2);}
	
	public static final FunctionsDefaultSignInfo<IntegerTriFunction> integerTriFunctionInfo = new FunctionsDefaultSignInfo<>(IntegerTriFunction.class);
	public interface IntegerTriFunction extends SignBase{ int apply3Integers(int i1, int i2, int i3);}
	
	public static final FunctionsDefaultSignInfo<Double2IntFunction> double2IntFunctionInfo = new FunctionsDefaultSignInfo<>(Double2IntFunction.class);
	public interface Double2IntFunction extends SignBase{ int intFromDouble(double f);}
	
	public static final FunctionsDefaultSignInfo<Vec3d2BlockPosFunction> vec3d2BlockPosFunctionInfo = new FunctionsDefaultSignInfo<>(Vec3d2BlockPosFunction.class);
	public interface Vec3d2BlockPosFunction extends SignBase{ BlockPos blockPosFromVec3d(Vec3d v);}
	
	public static final FunctionsDefaultSignInfo<IntegerFromBlockPosFunction> integerFromBlockPosFunctionInfo = new FunctionsDefaultSignInfo<>(IntegerFromBlockPosFunction.class);
	public interface IntegerFromBlockPosFunction extends SignBase{ int integerFromBlockPos(BlockPos p);}
	
	public static final FunctionsDefaultSignInfo<DoubleFromVec3dFunc> doubleFromVec3dFuncInfo = new FunctionsDefaultSignInfo<>(DoubleFromVec3dFunc.class);
	public interface DoubleFromVec3dFunc extends SignBase{ double doubleFromVec3d(Vec3d v);}
	
	public static class EqualsSign implements IntegerCompareSign, DoubleCompareSign, ObjectCompareSign{
		private EqualsSign(){}
		@Override public String displayString() {return "==";}
		@Override public boolean compareDoubles(double f1, double f2) {return f1 == f2;}
		@Override public boolean compareIntegers(int i1, int i2) {return i1 == i2;}
		@Override public boolean compareObjects(Object o1, Object o2) {return Objects.equals(o1, o2);}
	}
	public static class NEqualSign implements IntegerCompareSign, DoubleCompareSign, ObjectCompareSign{
		private NEqualSign(){}
		@Override public String displayString() {return "!=";}
		@Override public boolean compareDoubles(double f1, double f2) {return f1 != f2;}
		@Override public boolean compareIntegers(int i1, int i2) {return i1 != i2;}
		@Override public boolean compareObjects(Object o1, Object o2) {return !Objects.equals(o1, o2);}
	}
	public static class LessSign implements IntegerCompareSign, DoubleCompareSign{
		private LessSign(){}
		@Override public String displayString() {return "<";}
		@Override public boolean compareDoubles(double f1, double f2) {return f1 < f2;}
		@Override public boolean compareIntegers(int i1, int i2) {return i1 < i2;}
	}
	public static class GreaterSign implements IntegerCompareSign, DoubleCompareSign{
		private GreaterSign(){}
		@Override public String displayString() {return ">";}
		@Override public boolean compareDoubles(double f1, double f2) {return f1 > f2;}
		@Override public boolean compareIntegers(int i1, int i2) {return i1 > i2;}
	}
	public static class LEqualSign implements IntegerCompareSign, DoubleCompareSign{
		private LEqualSign(){}
		@Override public String displayString() {return "<=";}
		@Override public boolean compareDoubles(double f1, double f2) {return f1 <= f2;}
		@Override public boolean compareIntegers(int i1, int i2) {return i1 <= i2;}
	}
	public static class GEqualSign implements IntegerCompareSign, DoubleCompareSign{
		private GEqualSign(){}
		@Override public String displayString() {return ">=";}
		@Override public boolean compareDoubles(double f1, double f2) {return f1 >= f2;}
		@Override public boolean compareIntegers(int i1, int i2) {return i1 >= i2;}
	}
	public static class AddSign implements IntegerCalculateSign, DoubleCalculateSign, BlockPosCalculateSign, Vec3dCalculateSign{
		private AddSign(){}
		@Override public String displayString() {return "+";}
		@Override public int calculateIntegers(int i1, int i2) {return i1 + i2;}
		@Override public double calculateDoubles(double f1, double f2) {return f1 + f2;}
		@Override public BlockPos calculateBlockPoses(BlockPos p1, BlockPos p2) {return p1.add(p2);}
		@Override public Vec3d calculateVec3ds(Vec3d v1, Vec3d v2) {return v1.add(v2);}
	}
	public static class SubtractSign implements IntegerCalculateSign, DoubleCalculateSign, BlockPosCalculateSign, Vec3dCalculateSign{
		private SubtractSign(){}
		@Override public String displayString() {return "-";}
		@Override public int calculateIntegers(int i1, int i2) {return i1 - i2;}
		@Override public double calculateDoubles(double f1, double f2) {return f1 - f2;}
		@Override public BlockPos calculateBlockPoses(BlockPos p1, BlockPos p2) {return p1.subtract(p2);}
		@Override public Vec3d calculateVec3ds(Vec3d v1, Vec3d v2) {return v1.subtract(v2);}
	}
	public static class MultiplySign implements IntegerCalculateSign, DoubleCalculateSign{
		private MultiplySign(){}
		@Override public String displayString() {return "*";}
		@Override public int calculateIntegers(int i1, int i2) {return i1 * i2;}
		@Override public double calculateDoubles(double f1, double f2) {return f1 * f2;}
	}
	public static class DivideSign implements IntegerCalculateSign, DoubleCalculateSign, DoubleFromVec3dsSign{
		private DivideSign(){}
		@Override public String displayString() {return "/";}
		@Override public int calculateIntegers(int i1, int i2) {return i1 / i2;}
		@Override public double calculateDoubles(double f1, double f2) {return f1 / f2;}
		@Override public double doubleFromVec3ds(Vec3d v1, Vec3d v2) {return v1.dotProduct(v2) / v2.lengthSquared();}
	}
	public static class ModSign implements IntegerCalculateSign, DoubleCalculateSign, Vec3dCalculateSign{
		private ModSign(){}
		@Override public String displayString() {return "%";}
		@Override public int calculateIntegers(int i1, int i2) {return i1 % i2;}
		@Override public double calculateDoubles(double f1, double f2) {return f1 % f2;}
		@Override public Vec3d calculateVec3ds(Vec3d v1, Vec3d v2) {
			double k = v1.dotProduct(v2) / v2.lengthSquared();
			return v1.subtract(v2.x * k, v2.y * k, v2.z * k);
		}
	}
	public static class AndSign implements IntegerCalculateSign{
		private AndSign(){}
		@Override public String displayString() {return "&";}
		@Override public int calculateIntegers(int i1, int i2) {return i1 & i2;}
	}
	public static class OrSign implements IntegerCalculateSign{
		private OrSign(){}
		@Override public String displayString() {return "|";}
		@Override public int calculateIntegers(int i1, int i2) {return i1 | i2;}
	}
	public static class XOrSign implements IntegerCalculateSign{
		private XOrSign(){}
		@Override public String displayString() {return "^";}
		@Override public int calculateIntegers(int i1, int i2) {return i1 ^ i2;}
	}
	public static class ShiftLeftSign implements IntegerCalculateSign{
		private ShiftLeftSign(){}
		@Override public String displayString() {return "<<";}
		@Override public int calculateIntegers(int i1, int i2) {return i1 << i2;}
	}
	public static class ShiftRightSign implements IntegerCalculateSign{
		private ShiftRightSign(){}
		@Override public String displayString() {return ">>";}
		@Override public int calculateIntegers(int i1, int i2) {return i1 >> i2;}
	}
	public static class CrossSign implements BlockPosCalculateSign, Vec3dCalculateSign{
		private CrossSign(){}
		@Override public String displayString() {return "×";}
		@Override public BlockPos calculateBlockPoses(BlockPos p1, BlockPos p2) {return p1.crossProduct(p2);}
		@Override public Vec3d calculateVec3ds(Vec3d v1, Vec3d v2) {return v1.crossProduct(v2);}
	}
	public static class DotSign implements DoubleFromVec3dsSign, IntegerFromBlockPosesSign {
		private DotSign(){}
		@Override public String displayString() {return "•";}
		@Override public double doubleFromVec3ds(Vec3d v1, Vec3d v2) {return v1.dotProduct(v2);}
		@Override public int intFromBlockPoses(BlockPos p1, BlockPos p2) {return p1.getX() * p2.getX() + p1.getY() * p2.getY() + p1.getZ() * p2.getZ();}
	}
	public static class DistanceSquared implements DoubleFromVec3dsSign, IntegerFromBlockPosesSign {
		private DistanceSquared(){}
		@Override public String displayString() {return "squaredDistanceTo";}
		@Override public double doubleFromVec3ds(Vec3d v1, Vec3d v2) {return v1.squaredDistanceTo(v2);}
		@Override public int intFromBlockPoses(BlockPos p1, BlockPos p2) {
			int x = p1.getX() - p2.getX();
			int y = p1.getY() - p2.getY();
			int z = p1.getZ() - p2.getZ();
			return x * x + y * y + z * z;
		}
	}
	public static class Distance implements DoubleFromVec3dsSign{
		private Distance(){}
		@Override public String displayString() {return "distanceTo";}
		@Override public double doubleFromVec3ds(Vec3d v1, Vec3d v2) {return v1.distanceTo(v2);}
	}
	public static class Pi implements DoubleConstant{//about 3.142
		private Pi(){}
		@Override public String displayString() {return "π";}
		@Override public double getDouble() {return Math.PI;}
	}
	public static class E implements DoubleConstant{//about 2.718
		private E(){}
		@Override public String displayString() {return "e";}
		@Override public double getDouble() {return Math.E;}
	}
	public static class PiOver2 implements DoubleConstant {//about 1.571
		private static final double res = Math.PI / 2;
		private PiOver2() {}
		@Override public String displayString() { return "π/2"; }
		@Override public double getDouble() { return res; }
	}
	public static class Sqrt2 implements DoubleConstant {//about 1.414
		private static final double res = Math.sqrt(2);
		private Sqrt2() {}
		@Override public String displayString() { return "√2"; }
		@Override public double getDouble() { return res; }
	}
	public static class Phi implements DoubleConstant{//about 0.618
		private static final double res = 2.0 / (Math.sqrt(5) + 1.0);
		private Phi(){}
		@Override public String displayString() {return "φ";}
		@Override public double getDouble() {return res;}//about 0.618
	}
	public static class Euler implements DoubleConstant{//about 0.577
		private Euler(){}
		@Override public String displayString() {return "γ";}
		@Override public double getDouble() {return 0.57721566490153286;}
	}
	public static class NegativeFunction implements IntegerFunction, DoubleFunction{
		private NegativeFunction(){}
		@Override public String displayString() {return "-";}
		@Override public int applyInteger(int i) {return -i;}
		@Override public double applyDouble(double f) {return -f;}
	}
	public static class AbsFunction implements IntegerFunction, DoubleFunction{
		private AbsFunction(){}
		@Override public String displayString() {return "abs";}
		@Override public int applyInteger(int i) {return Math.abs(i);}
		@Override public double applyDouble(double f) {return Math.abs(f);}
	}
	public static class SignFunction implements IntegerFunction, DoubleFunction{
		private SignFunction(){}
		@Override public String displayString() {return "sign";}
		@Override public int applyInteger(int i) {
			if(i == 0) return 0;
			else return i > 0 ? 1 : -1;
		}
		@Override public double applyDouble(double f) {return Math.signum(f);}
	}
	public static class SquareFunction implements IntegerFunction, DoubleFunction{
		private SquareFunction(){}
		@Override public String displayString() {return "square";}
		@Override public int applyInteger(int i) {return i * i;}
		@Override public double applyDouble(double f) {return f * f;}
	}
	public static class FactorialFunction implements IntegerFunction{
		private FactorialFunction(){}
		@Override public String displayString() {return "factorial";}
		@Override public int applyInteger(int i) {
			if(i < 0) return 1;
			int res = 1;
			while(i != 0) res *= i--;
			return res;
		}
	}
	public static class Floor implements Double2IntFunction, DoubleFunction, Vec3d2BlockPosFunction{
		private Floor(){}
		@Override public String displayString() {return "floor";}
		@Override public int intFromDouble(double f) {return (int)Math.floor(f);}
		@Override public double applyDouble(double f) {return Math.floor(f);}
		@Override public BlockPos blockPosFromVec3d(Vec3d v) {return BlockPos.ofFloored(v);}
	}
	public static class Ceil implements Double2IntFunction, DoubleFunction, Vec3d2BlockPosFunction{
		private Ceil(){}
		@Override public String displayString() {return "ceil";}
		@Override public int intFromDouble(double f) {return (int)Math.ceil(f);}
		@Override public double applyDouble(double f) {return Math.ceil(f);}
		@Override public BlockPos blockPosFromVec3d(Vec3d v) {return new BlockPos((int)Math.ceil(v.x), (int)Math.ceil(v.y), (int)Math.ceil(v.z));}
	}
	public static class Round implements Double2IntFunction, DoubleFunction, Vec3d2BlockPosFunction{
		private Round(){}
		@Override public String displayString() {return "round";}
		@Override public int intFromDouble(double f) {return (int)Math.round(f);}
		@Override public double applyDouble(double f) {return Math.round(f);}
		@Override public BlockPos blockPosFromVec3d(Vec3d v) {return new BlockPos((int)Math.round(v.x), (int)Math.round(v.y), (int)Math.round(v.z));}
	}
	public static class Trunc implements Double2IntFunction, DoubleFunction, Vec3d2BlockPosFunction{
		private Trunc(){}
		@Override public String displayString() {return "trunc";}
		@Override public int intFromDouble(double f) {return (int)f;}
		@Override public double applyDouble(double f) {return (double)(long)f;}
		@Override public BlockPos blockPosFromVec3d(Vec3d v) {return new BlockPos((int)v.x, (int)v.y, (int)v.z);}
	}
	public static class SqrtFunction implements DoubleFunction{
		private SqrtFunction(){}
		@Override public String displayString() {return "sqrt";}
		@Override public double applyDouble(double f) {return Math.sqrt(f);}
	}
	public static class ExpFunction implements DoubleFunction{
		private ExpFunction(){}
		@Override public String displayString() {return "exp";}
		@Override public double applyDouble(double f) {return Math.exp(f);}
	}
	public static class LnFunction implements DoubleFunction{
		private LnFunction(){}
		@Override public String displayString() {return "ln";}
		@Override public double applyDouble(double f) {return Math.log(f);}
	}
	public static class SinFunction implements DoubleFunction{
		private SinFunction(){}
		@Override public String displayString() {return "sin";}
		@Override public double applyDouble(double f) {return Math.sin(f);}
	}
	public static class CosFunction implements DoubleFunction{
		private CosFunction(){}
		@Override public String displayString() {return "cos";}
		@Override public double applyDouble(double f) {return Math.cos(f);}
	}
	public static class TanFunction implements DoubleFunction{
		private TanFunction(){}
		@Override public String displayString() {return "tan";}
		@Override public double applyDouble(double f) {return Math.tan(f);}
	}
	public static class ArcsinFunction implements DoubleFunction{
		private ArcsinFunction(){}
		@Override public String displayString() {return "arcsin";}
		@Override public double applyDouble(double f) {return Math.asin(f);}
	}
	public static class ArccosFunction implements DoubleFunction{
		private ArccosFunction(){}
		@Override public String displayString() {return "arccos";}
		@Override public double applyDouble(double f) {return Math.acos(f);}
	}
	public static class ArctanFunction implements DoubleFunction{
		private ArctanFunction(){}
		@Override public String displayString() {return "arctan";}
		@Override public double applyDouble(double f) {return Math.atan(f);}
	}
	public static class SinhFunction implements DoubleFunction{
		private SinhFunction(){}
		@Override public String displayString() {return "sinh";}
		@Override public double applyDouble(double f) {return Math.sinh(f);}
	}
	public static class CoshFunction implements DoubleFunction{
		private CoshFunction(){}
		@Override public String displayString() {return "cosh";}
		@Override public double applyDouble(double f) {return Math.cosh(f);}
	}
	public static class GcdFunction implements IntegerBiFunction{
		private GcdFunction(){}
		@Override public String displayString() {return "gcd";}
		@Override public int apply2Integers(int i1, int i2) {
			while(true){
				if(i1 == 0) return i2;
				i2 = i2 % i1;
				if(i2 == 0) return i1;
				i1 = i1 % i2;
			}
		}
	}
	public static class LcdFunction implements IntegerBiFunction{
		private LcdFunction(){}
		@Override public String displayString() {return "lcd";}
		@Override public int apply2Integers(int i1, int i2) {
			if(i1 == 0 && i2 == 0) return 0;
			int gcdRes = i1 * i2;
			while(true){
				if(i1 == 0) return gcdRes / i2;
				i2 = i2 % i1;
				if(i2 == 0) return gcdRes / i1;
				i1 = i1 % i2;
			}
		}
	}
	public static class CombineFunction implements IntegerBiFunction{
		private CombineFunction(){}
		@Override public String displayString() {return "combine";}
		@Override public int apply2Integers(int i1, int i2) {
			int res = 1;
			for(int i = 0; i < i2; ++i) {
				res *= i1 - i;
				res /= i + 1;
			}
			return res;
		}
	}
	public static class ArrangeFunction implements IntegerBiFunction{
		private ArrangeFunction(){}
		@Override public String displayString() {return "arrange";}
		@Override public int apply2Integers(int i1, int i2) {
			int res = 1;
			for(int i = 0; i < i2; ++i) res *= i1 - i;
			return res;
		}
	}
	public static class PowFunction implements IntegerBiFunction, DoubleBiFunction{
		private PowFunction(){}
		@Override public String displayString() {return "pow";}
		@Override public int apply2Integers(int i1, int i2) {
			if(i2 < 0) return 1;
			int res = 1;
			while(i2 != 0){
				if((i2 & 1) != 0) res *= i1;
				i2 >>= 1;
				i1 *= i1;
			}
			return res;
		}
		@Override public double apply2Doubles(double f1, double f2) {return Math.pow(f1, f2);}
	}
	public static class ModInverseFunction implements IntegerBiFunction{
		private ModInverseFunction(){}
		@Override public String displayString() {return "modInv";}
		@Override public int apply2Integers(int i1, int i2) {
			if(i2 == 0) return 1;
			int k = 1;
			i1 = i1 % i2;
			while(i1 != 0){
				int i = Math.ceilDiv(i2, i1);
				k *= i;
				i1 = i1 * i - i2;
			}
			return k;
		}
	}
	public static class ModPowFunction implements IntegerTriFunction{
		private ModPowFunction(){}
		@Override public String displayString() {return "pow";}
		@Override public int apply3Integers(int i1, int i2, int i3) {
			if(i3 == 0) return POW.apply2Integers(i1, i2);
			int res = 1;
			if(i2 < 0){
				i1 = MOD_INVERSE.apply2Integers(i1, i2);
				i2 = -i2;
			}
			else i1 = i1 % i3;
			while(i2 != 0){
				if((i2 & 1) != 0) res = res * i1 % i3;
				i2 >>>= 1;
				i1 = i1 * i1 % i3;
			}
			return res;
		}
	}
	public static class CoordinateX implements DoubleFromVec3dFunc, IntegerFromBlockPosFunction {
		private CoordinateX(){}
		@Override public String displayString() {return "coordinate X";}
		@Override public double doubleFromVec3d(Vec3d v) {return v.x;}
		@Override public int integerFromBlockPos(BlockPos p) {return p.getX();}
	}
	public static class CoordinateY implements DoubleFromVec3dFunc, IntegerFromBlockPosFunction {
		private CoordinateY(){}
		@Override public String displayString() {return "coordinate Y";}
		@Override public double doubleFromVec3d(Vec3d v) {return v.y;}
		@Override public int integerFromBlockPos(BlockPos p) {return p.getY();}
	}
	public static class CoordinateZ implements DoubleFromVec3dFunc, IntegerFromBlockPosFunction {
		private CoordinateZ(){}
		@Override public String displayString() {return "coordinate Z";}
		@Override public double doubleFromVec3d(Vec3d v) {return v.z;}
		@Override public int integerFromBlockPos(BlockPos p) {return p.getZ();}
	}
	public static class SquaredLength implements DoubleFromVec3dFunc, IntegerFromBlockPosFunction {
		private SquaredLength(){}
		@Override public String displayString() {return "squaredLength";}
		@Override public double doubleFromVec3d(Vec3d v) {return v.lengthSquared();}
		@Override public int integerFromBlockPos(BlockPos p) {
			int x = p.getX(), y = p.getY(), z = p.getZ();
			return x * x + y * y + z * z;
		}
	}
	public static class Length implements DoubleFromVec3dFunc{
		private Length(){}
		@Override public String displayString() {return "length";}
		@Override public double doubleFromVec3d(Vec3d v) {return v.length();}
	}
	
	public interface ISignInfo<T extends SignBase>{
		//参数：
		//curr：当前符号
		//isLeftButton：是否为左键点击
		//callback：回调函数，传入切换后的符号
		void mouseButtonClicked(T curr, boolean isLeftButton, Consumer<T> callback);
		@Nullable T get(String idString);
	}
	
	public static class ArrayListSignInfo<T extends SignBase> implements ISignInfo<T>{
		private final ArrayList<T> signs;
		public ArrayListSignInfo(){
			this.signs = new ArrayList<>();
		}
		public ArrayListSignInfo(ArrayList<T> signs){
			this.signs = signs;
		}
		public void addSign(T sign){
			signs.add(sign);
		}
		public T cycleSign(T curr, boolean forward){
			var i = signs.indexOf(curr);
			if(i == -1) return signs.getFirst();
			if(forward){
				int j = i + 1;
				if(j >= signs.size()) return signs.getFirst();
				else return signs.get(j);
			}
			else {
				int j = i - 1;
				if(j < 0) return signs.getLast();
				else return signs.get(j);
			}
		}
		
		@Override public void mouseButtonClicked(T curr, boolean isLeftButton, Consumer<T> callback) {
			callback.accept(cycleSign(curr, isLeftButton));
		}
		
		@Override public @Nullable T get(String idString){
			for(var sign : signs){
				if(sign.idString().equals(idString)) return sign;
			}
			return null;
		}
	}
	
	public static class FunctionsDefaultSignInfo<T extends SignBase> implements ISignInfo<T>{
		private final T[] signs;
		private final ImmutableMap<String, Integer> indexMap;
		private FunctionsDefaultSignInfo(Class<T> targetType){
			ArrayList<T> list = new ArrayList<>();
			for(var v : Functions.class.getFields()){
				if(Modifier.isStatic(v.getModifiers()) && targetType.isAssignableFrom(v.getType())) {
					try {
						//noinspection unchecked
						list.add((T) v.get(null));
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				}
			}
			//noinspection unchecked
			this.signs = list.toArray((T[])Array.newInstance(targetType, list.size()));
			ImmutableMap.Builder<String, Integer> indexMapBuilder = new ImmutableMap.Builder<>();
			for(int i = 0; i < signs.length; ++i) indexMapBuilder.put(signs[i].idString(), i);
			indexMap = indexMapBuilder.build();
		}
		public T cycleSign(T curr, boolean forward){
			var i = indexMap.get(curr.idString());
			if(i == null) return signs[0];
			if(forward){
				int j = i + 1;
				if(j >= signs.length) return signs[0];
				else return signs[j];
			}
			else {
				int j = i - 1;
				if(j < 0) return signs[signs.length - 1];
				else return signs[j];
			}
		}
		
		@Override public void mouseButtonClicked(T curr, boolean isLeftButton, Consumer<T> callback) {
			callback.accept(cycleSign(curr, isLeftButton));
		}
		
		@Override public @Nullable T get(String idString){
			var i = indexMap.get(idString);
			if(i == null) return null;
			else return signs[i];
		}
	}
}

