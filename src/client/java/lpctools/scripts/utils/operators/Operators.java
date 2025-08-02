package lpctools.scripts.utils.operators;

import net.minecraft.util.math.BlockPos;
import org.joml.Vector3d;

public class Operators {
	public interface IOperator{
		String key();
	}
	public interface IBasicOperator extends IOperator{
		double operate(double v1, double v2);
		int operate(int v1, int v2);
	}
	public interface IExtraOperator extends IBasicOperator{
		void operate(BlockPos v1, BlockPos v2, BlockPos.Mutable res);
		void operate(Vector3d v1, Vector3d v2, Vector3d res);
	}
	public static final IBasicOperator[] basicOperators = {
		Add.instance,
		Subtract.instance,
		Multiply.instance,
		Divide.instance,
		Mod.instance
	};
	public static final IExtraOperator[] extraOperators = {
		Add.instance,
		Subtract.instance
	};
	public static class Add implements IExtraOperator{
		public static final Add instance = new Add();
		@Override public void operate(BlockPos v1, BlockPos v2, BlockPos.Mutable res) {
			res.setX(v1.getX() + v2.getX());
			res.setY(v1.getY() + v2.getY());
			res.setZ(v1.getZ() + v2.getZ());
		}
		@Override public void operate(Vector3d v1, Vector3d v2, Vector3d res) {v1.add(v2, res);}
		@Override public double operate(double v1, double v2) {return v1 + v2;}
		@Override public int operate(int v1, int v2) {return v1 + v2;}
		@Override public String key() {return "+";}
	}
	public static class Subtract implements IExtraOperator{
		public static final Subtract instance = new Subtract();
		@Override public void operate(BlockPos v1, BlockPos v2, BlockPos.Mutable res) {
			res.setX(v1.getX() - v2.getX());
			res.setY(v1.getY() - v2.getY());
			res.setZ(v1.getZ() - v2.getZ());
		}
		@Override public void operate(Vector3d v1, Vector3d v2, Vector3d res) {v1.sub(v2, res);}
		@Override public double operate(double v1, double v2) {return v1 - v2;}
		@Override public int operate(int v1, int v2) {return v1 - v2;}
		@Override public String key() {return "-";}
	}
	public static class Multiply implements IBasicOperator{
		public static final Multiply instance = new Multiply();
		@Override public double operate(double v1, double v2) {return v1 * v2;}
		@Override public int operate(int v1, int v2) {return v1 * v2;}
		@Override public String key() {return "*";}
	}
	public static class Divide implements IBasicOperator{
		public static final Divide instance = new Divide();
		@Override public double operate(double v1, double v2) {return v1 / v2;}
		@Override public int operate(int v1, int v2) {return v1 / v2;}
		@Override public String key() {return "/";}
	}
	public static class Mod implements IBasicOperator{
		public static final Mod instance = new Mod();
		@Override public double operate(double v1, double v2) {return v1 % v2;}
		@Override public int operate(int v1, int v2) {return v1 % v2;}
		@Override public String key() {return "%";}
	}
}
