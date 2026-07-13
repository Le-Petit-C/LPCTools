package lpctools.util.data;

import org.joml.Vector3i;

@SuppressWarnings("unused")
public class Box3i{
    public final Vector3i pos1 = new Vector3i(), pos2 = new Vector3i();
    public Box3i(){}
    public Box3i(Vector3i pos1, Vector3i pos2){set(pos1, pos2);}
    public Box3i(Box3i box){set(box);}
    public Box3i ensureMinMax(Box3i res){
        int min, max;
        if(pos1.x < pos2.x) {min = pos1.x; max = pos2.x;}
        else {min = pos2.x; max = pos1.x;}
        res.pos1.x = min; res.pos2.x = max;
        if(pos1.y < pos2.y) {min = pos1.y; max = pos2.y;}
        else {min = pos2.y; max = pos1.y;}
        res.pos1.y = min; res.pos2.y = max;
        if(pos1.z < pos2.z) {min = pos1.z; max = pos2.z;}
        else {min = pos2.z; max = pos1.z;}
        res.pos1.z = min; res.pos2.z = max;
        return res;
    }
    public Box3i ensureMinMax(){return ensureMinMax(this);}
    public Vector3i getMin(Vector3i res){
        res.x = Math.min(pos1.x, pos2.x);
        res.y = Math.min(pos1.y, pos2.y);
        res.z = Math.min(pos1.z, pos2.z);
        return res;
    }
    public Vector3i getMax(Vector3i res){
        res.x = Math.max(pos1.x, pos2.x);
        res.y = Math.max(pos1.y, pos2.y);
        res.z = Math.max(pos1.z, pos2.z);
        return res;
    }
    public Vector3i getMin(){return getMin(new Vector3i());}
    public Vector3i getMax(){return getMax(new Vector3i());}
    public Box3i set(Box3i box){
        return set(box.pos1, box.pos2);
    }
    public Box3i set(Vector3i pos1, Vector3i pos2){
        this.pos1.set(pos1);
        this.pos2.set(pos2);
        return this;
    }
}
