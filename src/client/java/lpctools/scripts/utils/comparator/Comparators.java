package lpctools.scripts.utils.comparator;

import net.minecraft.block.Block;
import net.minecraft.util.math.Vec3i;

public class Comparators {
	public static final IAllComparable[] allComparable = new IAllComparable[]{
		Equal.instance,
		NEqual.instance,
		Less.instance,
		Greater.instance,
		LEqual.instance,
		GEqual.instance
	};
	public static final IEqualComparable[] equalComparable = new IEqualComparable[]{
		Equal.instance,
		NEqual.instance
	};
	
	public interface IComparator{
		String key();
	}
	
	public interface IAllComparable extends IComparator {
		boolean compare(double v1, double v2);
		boolean compare(int v1, int v2);
		boolean compare(String v1, String v2);
	}
	
	public interface IEqualComparable extends IComparator {
		boolean compare(boolean v1, boolean v2);
		boolean compare(Vec3i v1, Vec3i v2);
		boolean compare(Block v1, Block v2);
	}
	
	public static class Equal implements IAllComparable, IEqualComparable{
		public static final Equal instance = new Equal();
		@Override public String key() {return "==";}
		@Override public boolean compare(double v1, double v2) {return v1 == v2;}
		@Override public boolean compare(int v1, int v2) {return v1 == v2;}
		@Override public boolean compare(String v1, String v2) {return v1.compareTo(v2) == 0;}
		@Override public boolean compare(boolean v1, boolean v2) {return v1 == v2;}
		@Override public boolean compare(Vec3i v1, Vec3i v2) {return v1.equals(v2);}
		@Override public boolean compare(Block v1, Block v2) {return v1.equals(v2);}
	}
	
	public static class NEqual implements IAllComparable, IEqualComparable{
		public static final NEqual instance = new NEqual();
		@Override public String key() {return "!=";}
		@Override public boolean compare(double v1, double v2) {return v1 != v2;}
		@Override public boolean compare(int v1, int v2) {return v1 != v2;}
		@Override public boolean compare(String v1, String v2) {return v1.compareTo(v2) != 0;}
		@Override public boolean compare(boolean v1, boolean v2) {return v1 != v2;}
		@Override public boolean compare(Vec3i v1, Vec3i v2) {return !v1.equals(v2);}
		@Override public boolean compare(Block v1, Block v2) {return !v1.equals(v2);}
	}
	
	public static class Greater implements IAllComparable {
		public static final Greater instance = new Greater();
		@Override public String key() {return ">";}
		@Override public boolean compare(double v1, double v2) {return v1 > v2;}
		@Override public boolean compare(int v1, int v2) {return v1 > v2;}
		@Override public boolean compare(String v1, String v2) {return v1.compareTo(v2) > 0;}
	}
	
	public static class Less implements IAllComparable {
		public static final Less instance = new Less();
		@Override public String key() {return "<";}
		@Override public boolean compare(double v1, double v2) {return v1 < v2;}
		@Override public boolean compare(int v1, int v2) {return v1 < v2;}
		@Override public boolean compare(String v1, String v2) {return v1.compareTo(v2) < 0;}
	}
	
	public static class GEqual implements IAllComparable {
		public static final GEqual instance = new GEqual();
		@Override public String key() {return ">=";}
		@Override public boolean compare(double v1, double v2) {return v1 >= v2;}
		@Override public boolean compare(int v1, int v2) {return v1 >= v2;}
		@Override public boolean compare(String v1, String v2) {return v1.compareTo(v2) >= 0;}
	}
	
	public static class LEqual implements IAllComparable{
		public static final LEqual instance = new LEqual();
		@Override public String key() {return "<=";}
		@Override public boolean compare(double v1, double v2) {return v1 <= v2;}
		@Override public boolean compare(int v1, int v2) {return v1 <= v2;}
		@Override public boolean compare(String v1, String v2) {return v1.compareTo(v2) <= 0;}
	}
}
