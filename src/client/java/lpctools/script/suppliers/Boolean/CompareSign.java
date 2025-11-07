package lpctools.script.suppliers.Boolean;

import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public interface CompareSign {
	static @Nullable CompareSign getCompareSign(String signString){
		return compareSignMap.get(signString);
	}
	static @Nullable ObjectCompareSign getObjectCompareSign(String signString){
		return objectCompareSignMap.get(signString);
	}
	
	ObjectCompareSign EQUALS = new ObjectCompareSign() {
		@Override public String signString(){return "==";}
		
		@Override public ObjectCompareSign nextObjectCompareSign(){return NEQUAL;}
		@Override public CompareSign nextCompareSign() {return NEQUAL;}
		
		@Override public boolean compareIntegers(int i1, int i2){return i1 == i2;}
		@Override public boolean compareDoubles(double f1, double f2){return f1 == f2;}
		
		@Override public boolean compareObjects(Object o1, Object o2) {return Objects.equals(o1, o2);}
	};
	ObjectCompareSign NEQUAL = new ObjectCompareSign() {
		@Override public String signString(){return "!=";}
		
		@Override public ObjectCompareSign nextObjectCompareSign(){return EQUALS;}
		@Override public CompareSign nextCompareSign() {return LESS;}
		
		@Override public boolean compareIntegers(int i1, int i2){return i1 != i2;}
		@Override public boolean compareDoubles(double f1, double f2){return f1 != f2;}
		
		@Override public boolean compareObjects(Object o1, Object o2) {return !Objects.equals(o1, o2);}
	};
	CompareSign LESS = new CompareSign() {
		@Override public String signString(){return "<";}
		
		@Override public CompareSign nextCompareSign() {return GREATER;}
		
		@Override public boolean compareIntegers(int i1, int i2){return i1 < i2;}
		@Override public boolean compareDoubles(double f1, double f2){return f1 < f2;}
	};
	CompareSign GREATER = new CompareSign() {
		@Override public String signString(){return ">";}
		
		@Override public CompareSign nextCompareSign() {return LEQUAL;}
		
		@Override public boolean compareIntegers(int i1, int i2){return i1 > i2;}
		@Override public boolean compareDoubles(double f1, double f2){return f1 > f2;}
	};
	CompareSign LEQUAL = new CompareSign() {
		@Override public String signString(){return "<=";}
		
		@Override public CompareSign nextCompareSign() {return GEQUAL;}
		
		@Override public boolean compareIntegers(int i1, int i2){return i1 <= i2;}
		@Override public boolean compareDoubles(double f1, double f2){return f1 <= f2;}
	};
	CompareSign GEQUAL = new CompareSign() {
		@Override public String signString(){return ">=";}
		
		@Override public CompareSign nextCompareSign() {return EQUALS;}
		
		@Override public boolean compareIntegers(int i1, int i2){return i1 >= i2;}
		@Override public boolean compareDoubles(double f1, double f2){return f1 >= f2;}
	};
	ImmutableMap<String, CompareSign> compareSignMap = ImmutableMap.of(
		EQUALS.signString(), EQUALS,
		NEQUAL.signString(), NEQUAL,
		LESS.signString(), LESS,
		GREATER.signString(), GREATER,
		LEQUAL.signString(), LEQUAL,
		GEQUAL.signString(), GEQUAL
	);
	ImmutableMap<String, ObjectCompareSign> objectCompareSignMap = ImmutableMap.of(
		EQUALS.signString(), EQUALS,
		NEQUAL.signString(), NEQUAL
	);
	String signString();
	
	CompareSign nextCompareSign();
	
	boolean compareIntegers(int i1, int i2);
	boolean compareDoubles(double f1, double f2);
	
	interface ObjectCompareSign extends CompareSign{
		ObjectCompareSign nextObjectCompareSign();
		
		boolean compareObjects(Object o1, Object o2);
	}
}
