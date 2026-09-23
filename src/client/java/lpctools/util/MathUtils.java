package lpctools.util;

import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Contract;
import org.joml.*;

import java.lang.Math;

@SuppressWarnings("unused")
public class MathUtils {
	@Contract(pure = true) public static double unorderedClamp(double v, double v1, double v2) {
		if(v1 < v2) return Math.clamp(v, v1, v2);
		else return Math.clamp(v, v2, v1);
	}
	@Contract(pure = true) public static float unorderedClamp(float v, float v1, float v2) {
		if(v1 < v2) return Math.clamp(v, v1, v2);
		else return Math.clamp(v, v2, v1);
	}
	@Contract(pure = true) public static double square(double v) { return v * v; }
	@Contract(pure = true) public static float square(float v) { return v * v; }
	@Contract(pure = true) public static Matrix4f inverseOffsetMatrix4f(Vector3f offset){
		return new Matrix4f().setColumn(3, new Vector4f(offset.mul(-1), 1));
	}
	@Contract(pure = true)
	public static Matrix4f worldToCameraMatrix4f(Camera camera){
		Vector3f vec = camera.position().toVector3f().mul(-1);
		Matrix4f matrix =  new Matrix4f()
				.rotate(camera.xRot() / 180 * Mth.PI, new Vector3f(1, 0, 0))
				.rotate((camera.yRot() + 180) / 180 * Mth.PI, new Vector3f(0, 1, 0));
		return matrix.setColumn(3, matrix.transform(new Vector4f(vec, 1)));
	}
	@Contract(pure = true)
	public static Matrix4d worldToCameraMatrix4d(Camera camera){
		Vec3 vec3d = camera.position();
		Vector3d vec = new Vector3d(vec3d.x(), vec3d.y(), vec3d.z()).mul(-1);
		Matrix4d matrix =  new Matrix4d()
				.rotate(camera.xRot() / 180 * Math.PI, new Vector3d(1, 0, 0))
				.rotate((camera.yRot() + 180) / 180 * Math.PI, new Vector3d(0, 1, 0));
		return matrix.setColumn(3, matrix.transform(new Vector4d(vec, 1)));
	}
	@Contract(pure = true)
	public static int getManhattanDistanceToZero(Vec3i pos){
		return Math.abs(pos.getX()) + Math.abs(pos.getY()) + Math.abs(pos.getZ());
	}
	@Contract(pure = true)
	public static double distanceSquared(Vec3 pos, ChunkPos chunkPos){
		return square(chunkPos.x() * 16 + 8.0 - pos.x) + square(chunkPos.z() * 16 + 8.0 - pos.z);
	}
	@Contract(pure = true)
	public static double distanceSquared(Vec3 pos1, Vector3dc pos2) {
		return square(pos1.x() - pos2.x()) + square(pos1.y() - pos2.y()) + square(pos1.z() - pos2.z());
	}
	@Contract(pure = true)
	public static double distanceSquared(Vec3 pos1, double x, double y, double z) {
		return square(pos1.x() - x) + square(pos1.y() - y) + square(pos1.z() - z);
	}
	@Contract(pure = true)
	public static float moddedAbs(float a, float mod) {
		float r = Math.abs(a % mod);
		return Math.min(r, mod - r);
	}
	@Contract(pure = true)
	public static double moddedAbs(double a, double mod) {
		double r = Math.abs(a % mod);
		return Math.min(r, mod - r);
	}
	// 把 x 归一到 [-mod/2, mod/2) 区间（与 moddedAbs 同源，返回 moddedAbs 的"带符号"版本）
	@Contract(pure = true)
	public static float modToCenter(float a, float mod) {
		float r = a % mod;
		float halfMod = mod * 0.5f;
		if(r >= halfMod) return r - mod;
		else if(r < -halfMod) return r + mod;
		return r;
	}
	@Contract(pure = true)
	public static double modToCenter(double a, double mod) {
		double r = a % mod;
		double halfMod = mod * 0.5;
		if(r >= halfMod) return r - mod;
		else if(r < -halfMod) return r + mod;
		return r;
	}
	public static float rotDistanceSquared(float YRot1, float XRot1, float YRot2, float XRot2) {
		return square(moddedAbs(YRot1 - YRot2, Mth.TWO_PI)) + square(XRot1 - XRot2);
	}
	// TODO 其他比较手长的工具也使用此距离计算
	@Contract(pure = true)
	public static double minSquaredDistanceToBlock(Vec3 pos, BlockPos blockPos) {
		double dx = pos.x - Math.clamp(pos.x, blockPos.getX(), blockPos.getX() + 1);
		double dy = pos.y - Math.clamp(pos.y, blockPos.getY(), blockPos.getY() + 1);
		double dz = pos.z - Math.clamp(pos.z, blockPos.getZ(), blockPos.getZ() + 1);
		return dx * dx + dy * dy + dz * dz;
	}

	public static double cycledClosestDistanceSquared(double v, int start, int width) {
		v = v - start;
		if(v >= (1L << 32) || v < -(1L << 32)) v = v % (1L << 32);
		if(v < 0) v = v + (1L << 32);
		long uWidth = Integer.toUnsignedLong(width);
		if(v < uWidth) return 0;
		else return MathUtils.square(Math.min(Math.abs(v - uWidth), Math.abs(v - (1L << 32))));
	}

	public static double cycledClosestDistanceSquared(double v, int start) {
		v = v - start;
		if(v >= (1L << 32) || v < -(1L << 32)) v = v % (1L << 32);
		if(v >= (1L << 31)) v -= 1L << 32;
		else if(v < -(1L << 31)) v += 1L << 32;
		return v * v;
	}

	public static double cycledClosestDistanceSquared(double startX, double startY, double startZ, int x, int y, int z, int dx, int dy, int dz) {
		return cycledClosestDistanceSquared(startX, x, dx)
			+ cycledClosestDistanceSquared(startY, y, dy)
			+ cycledClosestDistanceSquared(startZ, z, dz);
	}

	public static double cycledClosestDistanceSquared(double startX, double startY, double startZ, int x, int y, int z) {
		return cycledClosestDistanceSquared(startX, x) + cycledClosestDistanceSquared(startY, y) + cycledClosestDistanceSquared(startZ, z);
	}

	public static double cycledClosestDistanceSquared(Vec3 start, Vec3i pos, Vec3i expand) {
		return cycledClosestDistanceSquared(start.x, start.y, start.z, pos.getX(), pos.getY(), pos.getZ(), expand.getX(), expand.getY(), expand.getZ());
	}

	public static double cycledClosestDistanceToFullCubeSquared(Vec3 start, Vec3i pos) {
		return cycledClosestDistanceSquared(start.x, start.y, start.z, pos.getX(), pos.getY(), pos.getZ(), 1, 1, 1);
	}

	@Contract(pure = true)
	public static Vector3i getSubChunkPos(BlockPos pos){
		return new Vector3i(pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4);
	}
	@Contract(pure = true)//请自行确保vec三个分量为正
	public static void clamp(BlockPos.MutableBlockPos src, Vec3i vec){
		src.setX(src.getX() % vec.getX());
		if(src.getX() < 0 ) src.setX(src.getX() + vec.getX());
		src.setY(src.getY() % vec.getY());
		if(src.getY() < 0 ) src.setY(src.getY() + vec.getY());
		src.setZ(src.getZ() % vec.getZ());
		if(src.getZ() < 0 ) src.setZ(src.getZ() + vec.getZ());
	}
	//转换整数为罗马数字字符串
	public static String romanNumerals(int n) {
		//数值过大时返回阿拉伯数字字符串
		if (n < 1 || n > 3999) return String.valueOf(n);
		
		// 定义所有可能的罗马数字组合（包含减法规则）
		enum RomanNumeralPart{
			M("M", 1000),
			CM("CM", 900),
			D("D", 500),
			CD("CD", 400),
			C("C", 100),
			XC("XC", 90),
			L("L", 50),
			XL("XL", 40),
			X("X", 10),
			IX("IX", 9),
			V("V", 5),
			IV("IV", 4),
			I("I", 1);
			RomanNumeralPart(String symbol, int value){
				this.symbol = symbol;
				this.value = value;
			}
			public final String symbol;
			public final int value;
		}
		StringBuilder result = new StringBuilder();
		for(RomanNumeralPart part : RomanNumeralPart.values()){
			while(n >= part.value){
				result.append(part.symbol);
				n -= part.value;
			}
		}
		return result.toString();
	}

	@Contract(pure = true) public static double choose(Direction.Axis axis, Position vec) { return axis.choose(vec.x(), vec.y(), vec.z()); }
	@Contract(pure = true) public static int choose(Direction.Axis axis, Vec3i vec) { return axis.choose(vec.getX(), vec.getY(), vec.getZ()); }
	@Contract(pure = true) public static int blockEdgeCoordinate(Direction direction, Vec3i vec) { return choose(direction.getAxis(), vec) + ((direction.getAxisDirection().getStep() + 1) >> 1); }

	@Contract(pure = true) public static Direction possibleHitDirection(Vec3i blockPos, Vec3 eyePos, Vec3 viewVec) {
		double k = Double.NEGATIVE_INFINITY;
		Direction res = Direction.DOWN;
		for(var axis : Direction.Axis.values()) {
			double v = choose(axis, viewVec);
			if(v != 0) {
				double c = axis.choose(blockPos.getX(), blockPos.getY(), blockPos.getZ()) + (v > 0 ? 0 : 1);
				double kc = (c - choose(axis, eyePos)) / v;
				if(kc > k) {
					k = kc;
					res = v > 0 ? axis.getNegative() : axis.getPositive();
				}
			}
		}
		return res;
	}

	@Contract(pure = true) public static double XRotOrDefault(double x, double y, double z, double def) { return x == 0 && y == 0 && z == 0 ? def : Math.atan2(-y, Math.sqrt(x * x + z * z)); }
	@Contract(pure = true) public static double YRotOrDefault(double x, double y, double z, double def) { return x == 0 && z == 0 ? def : Math.atan2(-x, z); }
	@Contract(pure = true) public static float XRotOrDefault(float x, float y, float z, float def) { return x == 0 && y == 0 && z == 0 ? def : (float)Math.atan2(-y, Math.sqrt(x * x + z * z)); }
	@Contract(pure = true) public static float YRotOrDefault(float x, float y, float z, float def) { return x == 0 && z == 0 ? def : (float)Math.atan2(-x, z); }
	@Contract("_,_,_,_,_,_->param6") public static <T extends Vector2d> T XYRotOrDef(double x, double y, double z, double xDef, double yDef, T res) { res.set(XRotOrDefault(x, y, z, xDef), YRotOrDefault(x, y, z, yDef)); return res; }
	@Contract("_,_,_,_,_,_->param6") public static <T extends Vector2f> T XYRotOrDef(float x, float y, float z, float xDef, float yDef, T res) { res.set(XRotOrDefault(x, y, z, xDef), YRotOrDefault(x, y, z, yDef)); return res; }
	@Contract("_,_,_->param3") public static <T extends Vector3d> T viewVecFromXYRot(double xRot, double yRot, T res) {
		double yCos = Math.cos(yRot);
		double ySin = Math.sin(yRot);
		double xCos = Math.cos(xRot);
		double xSin = Math.sin(xRot);
		res.set(ySin * xCos, -xSin, yCos * xCos);
		return res;
	}
	@Contract("_,_,_->param3") public static <T extends Vector3f> T viewVecFromXYRot(float xRot, float yRot, T res) {
		float yCos = Mth.cos(yRot);
		float ySin = Mth.sin(yRot);
		float xCos = Mth.cos(xRot);
		float xSin = Mth.sin(xRot);
		res.set(ySin * xCos, -xSin, yCos * xCos);
		return res;
	}
}
