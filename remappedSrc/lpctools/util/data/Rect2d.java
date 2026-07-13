package lpctools.util.data;

public class Rect2d {
	public double left, top, right, bottom;
	public Rect2d(){resetForRange();}
	public Rect2d(double left, double top, double right, double bottom){
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
	}
	public Rect2d(Rect2d rec){this(rec.left, rec.top, rec.right, rec.bottom);}
	public Rect2d set(double left, double top, double right, double bottom){
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
		return this;
	}
	public Rect2d set(Rect2d rec){return set(rec.left, rec.top, rec.right, rec.bottom);}
	public void expandToInclude(Rect2d rect){
		if(rect == null) return;
		if(left > rect.left) left = rect.left;
		if(top > rect.top) top = rect.top;
		if(right < rect.right) right = rect.right;
		if(bottom < rect.bottom) bottom = rect.bottom;
	}
	public void resetForRange(){
		left = top = Integer.MAX_VALUE;
		right = bottom = Integer.MIN_VALUE;
	}
	public double width(){return right - left;}
	public double height(){return bottom - top;}
	public boolean isValid(){return width() > 0 && height() > 0;}
}
