package lpctools.util.data;

public class Rect2i {
	public int left, top, right, bottom;
	public Rect2i(){resetForRange();}
	public Rect2i(int left, int top, int right, int bottom){
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
	}
	public Rect2i(Rect2i rec){this(rec.left, rec.top, rec.right, rec.bottom);}
	public Rect2i set(int left, int top, int right, int bottom){
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
		return this;
	}
	public Rect2i set(Rect2i rec){return set(rec.left, rec.top, rec.right, rec.bottom);}
	public void expandToInclude(Rect2i rect){
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
	public int width(){return right - left;}
	public int height(){return bottom - top;}
	public boolean isValid(){return width() > 0 && height() > 0;}
}
