package lpctools.debugs.ThreeBodyDisplay;

import lpctools.lpcfymasaapi.render.translucentShapes.Line;
import lpctools.lpcfymasaapi.render.translucentShapes.ShapeReference;
import lpctools.lpcfymasaapi.render.translucentShapes.ShapeRegister;
import lpctools.util.javaex.QuietAutoCloseable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.Arrays;

import static lpctools.debugs.ThreeBodyDisplay.Utils.getBrightness;
import static lpctools.debugs.ThreeBodyDisplay.Utils.vector3d2Color;

class StarRenderData implements QuietAutoCloseable {
	float radius;
	final Vector3d light = new Vector3d();
	final Vector3d position = new Vector3d();
	int lineColor = 0;
	double rSquare;
	int workingThreadTrackIndex = 0;
	int rawWorkingThreadTrackIndex = 0;
	int rawTrackedIndex = 0;
	int trackedIndex = 0;
	@NotNull TrackingData @NotNull [] trackedPoints = new TrackingData[0];
	
	static class TrackingData {
		final Vector3d pos = new Vector3d();
		@Nullable ShapeReference shapeRef;
		@Nullable ShapeRegister<Line> lineReg;
		final Line shape = new Line();
		
		void setRef(@Nullable ShapeReference shapeRef) {
			if (this.shapeRef != null) this.shapeRef.close();
			this.shapeRef = shapeRef;
		}
		
		@Contract("_,_,_->this")
		TrackingData withPrevious(TrackingData previous, int color, Vector3d basePoint) {
			Vector3d posCache = new Vector3d();
			posCache.set(basePoint).add(previous.pos);
			shape.vertices[0].setPositionColor(posCache, color);
			posCache.set(basePoint).add(pos);
			shape.vertices[1].setPositionColor(posCache, color);
			shape.updateCenter();
			if (lineReg == null) lineReg = Line.register(false);
			setRef(lineReg.register(shape));
			return this;
		}
	}
	
	StarRenderData(Star star) {set(star, false);}
	
	void set(Star star, boolean track) {
		radius = (float)star.radius;
		rSquare = star.radius * star.radius;
		light.set(star.light);
		lineColor = vector3d2Color(light, 2.0 / getBrightness(light));
		position.set(star.position);
		if (track && trackedPoints.length != 0) {
			++rawWorkingThreadTrackIndex;
			if (++workingThreadTrackIndex >= trackedPoints.length) workingThreadTrackIndex = 0;
			var tracked = trackedPoints[workingThreadTrackIndex];
			tracked.pos.set(position);
		}
	}
	
	void resetTracks(int trackCount) {
		for (var track : trackedPoints) track.setRef(null);
		trackedPoints = new TrackingData[trackCount];
		Arrays.setAll(trackedPoints, i -> new TrackingData());
		rawTrackedIndex = trackedIndex = 1;
		rawWorkingThreadTrackIndex = workingThreadTrackIndex = 0;
	}
	
	void updateRenderThreadTracks(Vector3d basePoint) {
		if (trackedPoints.length == 0) return;
		if (rawWorkingThreadTrackIndex - rawTrackedIndex >= trackedPoints.length) {
			rawTrackedIndex = rawWorkingThreadTrackIndex - trackedPoints.length + 1;
			trackedIndex = rawTrackedIndex % trackedPoints.length;
			trackedPoints[trackedIndex].setRef(null);
		}
		TrackingData previous = trackedPoints[trackedIndex];
		while (rawWorkingThreadTrackIndex - rawTrackedIndex > 0) {
			++rawTrackedIndex;
			if (++trackedIndex >= trackedPoints.length) trackedIndex = 0;
			previous = trackedPoints[trackedIndex].withPrevious(previous, lineColor, basePoint);
		}
	}
	
	@Override public void close() {
		resetTracks(0);
	}
}
