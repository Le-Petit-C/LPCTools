package lpctools.util;

import com.google.common.collect.ImmutableMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

public class Signs {
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
	
	public interface SignBase{String signString();}
	
	//比较符号
	public static final SignInfo<IntegerCompareSign> integerCompareSignInfo = new SignInfo<>(EQUALS, NEQUAL, LESS, GREATER, LEQUAL, GEQUAL);
	public interface IntegerCompareSign extends SignBase { boolean compareIntegers(int i1, int i2);}
	
	public static final SignInfo<DoubleCompareSign> doubleCompareSignInfo = new SignInfo<>(EQUALS, NEQUAL, LESS, GREATER, LEQUAL, GEQUAL);
	public interface DoubleCompareSign extends SignBase { boolean compareDoubles(double f1, double f2);}
	
	public static final SignInfo<ObjectCompareSign> objectCompareSignInfo = new SignInfo<>(EQUALS, NEQUAL);
	public interface ObjectCompareSign extends SignBase{ boolean compareObjects(Object o1, Object o2);}
	
	//计算符号
	public static final SignInfo<IntegerCalculateSign> integerCalculateSignInfo = new SignInfo<>(ADD, SUBTRACT, MULTIPLY, DIVIDE, MOD, AND, OR, XOR, SHIFT_LEFT, SHIFT_RIGHT);
	public interface IntegerCalculateSign extends SignBase{ int calculateIntegers(int i1, int i2);}
	
	public static final SignInfo<DoubleCalculateSign> doubleCalculateSignInfo = new SignInfo<>(ADD, SUBTRACT, MULTIPLY, DIVIDE, MOD);
	public interface DoubleCalculateSign extends SignBase{ double calculateDoubles(double f1, double f2);}
	
	public static final SignInfo<BlockPosCalculateSign> blockPosCalculateSignInfo = new SignInfo<>(ADD, SUBTRACT, CROSS);
	public interface BlockPosCalculateSign extends SignBase{ BlockPos calculateBlockPoses(BlockPos p1, BlockPos p2);}
	
	public static final SignInfo<Vec3dCalculateSign> vec3dCalculateSignInfo = new SignInfo<>(ADD, SUBTRACT, MOD, CROSS);
	public interface Vec3dCalculateSign extends SignBase{ Vec3d calculateVec3ds(Vec3d v1, Vec3d v2);}
	
	//混合计算符号
	public static final SignInfo<IntegerFromBlockPosesSign> intFromBlockPosesSignInfo = new SignInfo<>(DOT, DISTANCE_SQUARED);
	public interface IntegerFromBlockPosesSign extends SignBase{ int intFromBlockPoses(BlockPos p1, BlockPos p2);}
	
	public static final SignInfo<DoubleFromVec3dsSign> doubleFromVec3dsSignInfo = new SignInfo<>(DOT, DIVIDE, DISTANCE_SQUARED, DISTANCE);
	public interface DoubleFromVec3dsSign extends SignBase{ double doubleFromVec3ds(Vec3d v1, Vec3d v2);}
	
	//拓展函数
	public static final SignInfo<DoubleConstant> doubleConstantInfo = new SignInfo<>(PI, E, PI_OVER_2, SQRT_2, PHI, EULER);
	public interface DoubleConstant extends SignBase{ double getDouble();}
	
	public static final SignInfo<IntegerFunction> integerFunctionInfo = new SignInfo<>(NEGATIVE, ABS, SIGN, SQUARE, FACTORIAL);
	public interface IntegerFunction extends SignBase{ int applyInteger(int i);}
	
	public static final SignInfo<DoubleFunction> doubleFunctionInfo = new SignInfo<>(NEGATIVE, ABS, SIGN, SQUARE, SQRT, EXP, LN, SIN, COS, TAN, ARCSIN, ARCCOS, ARCTAN, SINH, COSH);
	public interface DoubleFunction extends SignBase{ double applyDouble(double f);}
	
	public static final SignInfo<IntegerBiFunction> integerBiFunctionInfo = new SignInfo<>(GCD, LCD, COMBINE, ARRANGE, POW, MOD_INVERSE);
	public interface IntegerBiFunction extends SignBase{ int apply2Integers(int i1, int i2);}
	
	public static final SignInfo<DoubleBiFunction> doubleBiFunctionInfo = new SignInfo<>(POW);
	public interface DoubleBiFunction extends SignBase{ double apply2Doubles(double f1, double f2);}
	
	public static final SignInfo<IntegerTriFunction> integerTriFunctionInfo = new SignInfo<>(MOD_POW);
	public interface IntegerTriFunction extends SignBase{ int apply3Integers(int i1, int i2, int i3);}
	
	public static class EqualsSign implements IntegerCompareSign, DoubleCompareSign, ObjectCompareSign{
		private EqualsSign(){}
		@Override public String signString() {return "==";}
		@Override public boolean compareDoubles(double f1, double f2) {return f1 == f2;}
		@Override public boolean compareIntegers(int i1, int i2) {return i1 == i2;}
		@Override public boolean compareObjects(Object o1, Object o2) {return Objects.equals(o1, o2);}
	}
	public static class NEqualSign implements IntegerCompareSign, DoubleCompareSign, ObjectCompareSign{
		private NEqualSign(){}
		@Override public String signString() {return "!=";}
		@Override public boolean compareDoubles(double f1, double f2) {return f1 != f2;}
		@Override public boolean compareIntegers(int i1, int i2) {return i1 != i2;}
		@Override public boolean compareObjects(Object o1, Object o2) {return !Objects.equals(o1, o2);}
	}
	public static class LessSign implements IntegerCompareSign, DoubleCompareSign{
		private LessSign(){}
		@Override public String signString() {return "<";}
		@Override public boolean compareDoubles(double f1, double f2) {return f1 < f2;}
		@Override public boolean compareIntegers(int i1, int i2) {return i1 < i2;}
	}
	public static class GreaterSign implements IntegerCompareSign, DoubleCompareSign{
		private GreaterSign(){}
		@Override public String signString() {return ">";}
		@Override public boolean compareDoubles(double f1, double f2) {return f1 > f2;}
		@Override public boolean compareIntegers(int i1, int i2) {return i1 > i2;}
	}
	public static class LEqualSign implements IntegerCompareSign, DoubleCompareSign{
		private LEqualSign(){}
		@Override public String signString() {return "<=";}
		@Override public boolean compareDoubles(double f1, double f2) {return f1 <= f2;}
		@Override public boolean compareIntegers(int i1, int i2) {return i1 <= i2;}
	}
	public static class GEqualSign implements IntegerCompareSign, DoubleCompareSign{
		private GEqualSign(){}
		@Override public String signString() {return ">=";}
		@Override public boolean compareDoubles(double f1, double f2) {return f1 >= f2;}
		@Override public boolean compareIntegers(int i1, int i2) {return i1 >= i2;}
	}
	public static class AddSign implements IntegerCalculateSign, DoubleCalculateSign, BlockPosCalculateSign, Vec3dCalculateSign{
		private AddSign(){}
		@Override public String signString() {return "+";}
		@Override public int calculateIntegers(int i1, int i2) {return i1 + i2;}
		@Override public double calculateDoubles(double f1, double f2) {return f1 + f2;}
		@Override public BlockPos calculateBlockPoses(BlockPos p1, BlockPos p2) {return p1.add(p2);}
		@Override public Vec3d calculateVec3ds(Vec3d v1, Vec3d v2) {return v1.add(v2);}
	}
	public static class SubtractSign implements IntegerCalculateSign, DoubleCalculateSign, BlockPosCalculateSign, Vec3dCalculateSign{
		private SubtractSign(){}
		@Override public String signString() {return "-";}
		@Override public int calculateIntegers(int i1, int i2) {return i1 - i2;}
		@Override public double calculateDoubles(double f1, double f2) {return f1 - f2;}
		@Override public BlockPos calculateBlockPoses(BlockPos p1, BlockPos p2) {return p1.subtract(p2);}
		@Override public Vec3d calculateVec3ds(Vec3d v1, Vec3d v2) {return v1.subtract(v2);}
	}
	public static class MultiplySign implements IntegerCalculateSign, DoubleCalculateSign{
		private MultiplySign(){}
		@Override public String signString() {return "*";}
		@Override public int calculateIntegers(int i1, int i2) {return i1 * i2;}
		@Override public double calculateDoubles(double f1, double f2) {return f1 * f2;}
	}
	public static class DivideSign implements IntegerCalculateSign, DoubleCalculateSign, DoubleFromVec3dsSign{
		private DivideSign(){}
		@Override public String signString() {return "/";}
		@Override public int calculateIntegers(int i1, int i2) {return i1 / i2;}
		@Override public double calculateDoubles(double f1, double f2) {return f1 / f2;}
		@Override public double doubleFromVec3ds(Vec3d v1, Vec3d v2) {return v1.dotProduct(v2) / v2.lengthSquared();}
	}
	public static class ModSign implements IntegerCalculateSign, DoubleCalculateSign, Vec3dCalculateSign{
		private ModSign(){}
		@Override public String signString() {return "%";}
		@Override public int calculateIntegers(int i1, int i2) {return i1 % i2;}
		@Override public double calculateDoubles(double f1, double f2) {return f1 % f2;}
		@Override public Vec3d calculateVec3ds(Vec3d v1, Vec3d v2) {
			double k = v1.dotProduct(v2) / v2.lengthSquared();
			return v1.subtract(v2.x * k, v2.y * k, v2.z * k);
		}
	}
	public static class AndSign implements IntegerCalculateSign{
		private AndSign(){}
		@Override public String signString() {return "&";}
		@Override public int calculateIntegers(int i1, int i2) {return i1 & i2;}
	}
	public static class OrSign implements IntegerCalculateSign{
		private OrSign(){}
		@Override public String signString() {return "|";}
		@Override public int calculateIntegers(int i1, int i2) {return i1 | i2;}
	}
	public static class XOrSign implements IntegerCalculateSign{
		private XOrSign(){}
		@Override public String signString() {return "^";}
		@Override public int calculateIntegers(int i1, int i2) {return i1 ^ i2;}
	}
	public static class ShiftLeftSign implements IntegerCalculateSign{
		private ShiftLeftSign(){}
		@Override public String signString() {return "<<";}
		@Override public int calculateIntegers(int i1, int i2) {return i1 << i2;}
	}
	public static class ShiftRightSign implements IntegerCalculateSign{
		private ShiftRightSign(){}
		@Override public String signString() {return ">>";}
		@Override public int calculateIntegers(int i1, int i2) {return i1 >> i2;}
	}
	public static class CrossSign implements BlockPosCalculateSign, Vec3dCalculateSign{
		private CrossSign(){}
		@Override public String signString() {return "×";}
		@Override public BlockPos calculateBlockPoses(BlockPos p1, BlockPos p2) {return p1.crossProduct(p2);}
		@Override public Vec3d calculateVec3ds(Vec3d v1, Vec3d v2) {return v1.crossProduct(v2);}
	}
	public static class DotSign implements DoubleFromVec3dsSign, IntegerFromBlockPosesSign {
		private DotSign(){}
		@Override public String signString() {return "•";}
		@Override public double doubleFromVec3ds(Vec3d v1, Vec3d v2) {return v1.dotProduct(v2);}
		@Override public int intFromBlockPoses(BlockPos p1, BlockPos p2) {return p1.getX() * p2.getX() + p1.getY() * p2.getY() + p1.getZ() * p2.getZ();}
	}
	public static class DistanceSquared implements DoubleFromVec3dsSign, IntegerFromBlockPosesSign {
		private DistanceSquared(){}
		@Override public String signString() {return "squaredDistanceTo";}
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
		@Override public String signString() {return "distanceTo";}
		@Override public double doubleFromVec3ds(Vec3d v1, Vec3d v2) {return v1.distanceTo(v2);}
	}
	public static class Pi implements DoubleConstant{//about 3.142
		private Pi(){}
		@Override public String signString() {return "π";}
		@Override public double getDouble() {return Math.PI;}
	}
	public static class E implements DoubleConstant{//about 2.718
		private E(){}
		@Override public String signString() {return "e";}
		@Override public double getDouble() {return Math.E;}
	}
	public static class PiOver2 implements DoubleConstant {//about 1.571
		private static final double res = Math.PI / 2;
		private PiOver2() {}
		@Override public String signString() { return "π/2"; }
		@Override public double getDouble() { return res; }
	}
	public static class Sqrt2 implements DoubleConstant {//about 1.414
		private static final double res = Math.sqrt(2);
		private Sqrt2() {}
		@Override public String signString() { return "√2"; }
		@Override public double getDouble() { return res; }
	}
	public static class Phi implements DoubleConstant{//about 0.618
		private static final double res = 2.0 / (Math.sqrt(5) + 1.0);
		private Phi(){}
		@Override public String signString() {return "φ";}
		@Override public double getDouble() {return res;}//about 0.618
	}
	public static class Euler implements DoubleConstant{//about 0.577
		private Euler(){}
		@Override public String signString() {return "γ";}
		@Override public double getDouble() {return 0.57721566490153286;}
	}
	public static class NegativeFunction implements IntegerFunction, DoubleFunction{
		private NegativeFunction(){}
		@Override public String signString() {return "-";}
		@Override public int applyInteger(int i) {return -i;}
		@Override public double applyDouble(double f) {return -f;}
	}
	public static class AbsFunction implements IntegerFunction, DoubleFunction{
		private AbsFunction(){}
		@Override public String signString() {return "abs";}
		@Override public int applyInteger(int i) {return Math.abs(i);}
		@Override public double applyDouble(double f) {return Math.abs(f);}
	}
	public static class SignFunction implements IntegerFunction, DoubleFunction{
		private SignFunction(){}
		@Override public String signString() {return "sign";}
		@Override public int applyInteger(int i) {
			if(i == 0) return 0;
			else return i > 0 ? 1 : -1;
		}
		@Override public double applyDouble(double f) {return Math.signum(f);}
	}
	public static class SquareFunction implements IntegerFunction, DoubleFunction{
		private SquareFunction(){}
		@Override public String signString() {return "square";}
		@Override public int applyInteger(int i) {return i * i;}
		@Override public double applyDouble(double f) {return f * f;}
	}
	public static class FactorialFunction implements IntegerFunction{
		private FactorialFunction(){}
		@Override public String signString() {return "factorial";}
		@Override public int applyInteger(int i) {
			if(i < 0) return 1;
			int res = 1;
			while(i != 0) res *= i--;
			return res;
		}
	}
	public static class SqrtFunction implements DoubleFunction{
		private SqrtFunction(){}
		@Override public String signString() {return "sqrt";}
		@Override public double applyDouble(double f) {return Math.sqrt(f);}
	}
	public static class ExpFunction implements DoubleFunction{
		private ExpFunction(){}
		@Override public String signString() {return "exp";}
		@Override public double applyDouble(double f) {return Math.exp(f);}
	}
	public static class LnFunction implements DoubleFunction{
		private LnFunction(){}
		@Override public String signString() {return "ln";}
		@Override public double applyDouble(double f) {return Math.log(f);}
	}
	public static class SinFunction implements DoubleFunction{
		private SinFunction(){}
		@Override public String signString() {return "sin";}
		@Override public double applyDouble(double f) {return Math.sin(f);}
	}
	public static class CosFunction implements DoubleFunction{
		private CosFunction(){}
		@Override public String signString() {return "cos";}
		@Override public double applyDouble(double f) {return Math.cos(f);}
	}
	public static class TanFunction implements DoubleFunction{
		private TanFunction(){}
		@Override public String signString() {return "tan";}
		@Override public double applyDouble(double f) {return Math.tan(f);}
	}
	public static class ArcsinFunction implements DoubleFunction{
		private ArcsinFunction(){}
		@Override public String signString() {return "arcsin";}
		@Override public double applyDouble(double f) {return Math.asin(f);}
	}
	public static class ArccosFunction implements DoubleFunction{
		private ArccosFunction(){}
		@Override public String signString() {return "arccos";}
		@Override public double applyDouble(double f) {return Math.acos(f);}
	}
	public static class ArctanFunction implements DoubleFunction{
		private ArctanFunction(){}
		@Override public String signString() {return "arctan";}
		@Override public double applyDouble(double f) {return Math.atan(f);}
	}
	public static class SinhFunction implements DoubleFunction{
		private SinhFunction(){}
		@Override public String signString() {return "sinh";}
		@Override public double applyDouble(double f) {return Math.sinh(f);}
	}
	public static class CoshFunction implements DoubleFunction{
		private CoshFunction(){}
		@Override public String signString() {return "cosh";}
		@Override public double applyDouble(double f) {return Math.cosh(f);}
	}
	public static class GcdFunction implements IntegerBiFunction{
		private GcdFunction(){}
		@Override public String signString() {return "gcd";}
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
		@Override public String signString() {return "lcd";}
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
		@Override public String signString() {return "combine";}
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
		@Override public String signString() {return "arrange";}
		@Override public int apply2Integers(int i1, int i2) {
			int res = 1;
			for(int i = 0; i < i2; ++i) res *= i1 - i;
			return res;
		}
	}
	public static class PowFunction implements IntegerBiFunction, DoubleBiFunction{
		private PowFunction(){}
		@Override public String signString() {return "pow";}
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
		@Override public String signString() {return "modInv";}
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
		@Override public String signString() {return "pow";}
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
	
	public static class SignInfo<T extends SignBase>{
		private final T[] signs;
		private final ImmutableMap<String, Integer> indexMap;
		@SafeVarargs
		private SignInfo(T... signs){
			this.signs = Arrays.copyOf(signs, signs.length);
			ImmutableMap.Builder<String, Integer> indexMapBuilder = new ImmutableMap.Builder<>();
			for(int i = 0; i < signs.length; ++i) indexMapBuilder.put(signs[i].signString(), i);
			indexMap = indexMapBuilder.build();
		}
		public T cycleSign(T curr, boolean forward){
			var i = indexMap.get(curr.signString());
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
		public @Nullable T get(String signString){
			var i = indexMap.get(signString);
			if(i == null) return null;
			else return signs[i];
		}
	}
}

