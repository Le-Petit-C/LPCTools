package lpctools.util;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import org.joml.Vector3dc;
import org.joml.Vector3fc;

public interface DirectionVectorPredicator {
	boolean test(float dx, float dy, float dz);
	default boolean test(double dx, double dy, double dz) { return test((float) dx, (float) dy, (float) dz); }
	default boolean test(Vector3fc vec) { return test(vec.x(), vec.y(), vec.z()); }
	default boolean test(Position vec) { return test(vec.x(), vec.y(), vec.z()); }
	default boolean test(Vector3dc vec) { return test(vec.x(), vec.y(), vec.z()); }

	DirectionVectorPredicator NONE = (_, _, _) -> true;

	static DirectionVectorPredicator predicate6(Direction direction) {
		return (dx, dy, dz) -> DataUtils.approximateNearstDirection(dx, dy, dz) == direction;
	}

	static DirectionVectorPredicator predicate4(Direction direction) {
		return (dx, _, dz) -> DataUtils.approximateNearestDirectionHorizontal(dx, dz) == direction;
	}
}
