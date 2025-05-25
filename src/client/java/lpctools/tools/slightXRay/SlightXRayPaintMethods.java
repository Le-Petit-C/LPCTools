package lpctools.tools.slightXRay;

import lpctools.compact.derived.ShapeList;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.util.math.BlockPos;
import org.joml.Matrix4d;
import org.joml.Vector3f;
import org.joml.Vector4d;

import static lpctools.tools.slightXRay.SlightXRay.*;
import static lpctools.util.MathUtils.vecTrans;

public class SlightXRayPaintMethods {
    private static class VertexVectorBuffer{
        public final Vector4d center = new Vector4d();
        public final Vector4d buf = new Vector4d();
        public final Vector3f nnn = new Vector3f();
        public final Vector3f nnp = new Vector3f();
        public final Vector3f npn = new Vector3f();
        public final Vector3f npp = new Vector3f();
        public final Vector3f pnn = new Vector3f();
        public final Vector3f pnp = new Vector3f();
        public final Vector3f ppn = new Vector3f();
        public final Vector3f ppp = new Vector3f();
        public final BlockPos.Mutable mutablePos = new BlockPos.Mutable();
    }
    private final VertexVectorBuffer vertexVectorBuffer = new VertexVectorBuffer();
    @SuppressWarnings("SameParameterValue")
    void vertexBlock(Matrix4d matrix, BufferBuilder buffer, BlockPos pos, int color, ShapeList shapes){
        VertexVectorBuffer vBuf = vertexVectorBuffer;
        Vector4d center = vBuf.center.set(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1);
        Vector4d buf = vBuf.buf;
        Vector3f nnn = vecTrans(center.add(-0.5, -0.5, -0.5, 0, buf).mul(matrix), vBuf.nnn);
        Vector3f nnp = vecTrans(center.add(-0.5, -0.5, 0.5, 0, buf).mul(matrix), vBuf.nnp);
        Vector3f npn = vecTrans(center.add(-0.5, 0.5, -0.5, 0, buf).mul(matrix), vBuf.npn);
        Vector3f npp = vecTrans(center.add(-0.5, 0.5, 0.5, 0, buf).mul(matrix), vBuf.npp);
        Vector3f pnn = vecTrans(center.add(0.5, -0.5, -0.5, 0, buf).mul(matrix), vBuf.pnn);
        Vector3f pnp = vecTrans(center.add(0.5, -0.5, 0.5, 0, buf).mul(matrix), vBuf.pnp);
        Vector3f ppn = vecTrans(center.add(0.5, 0.5, -0.5, 0, buf).mul(matrix), vBuf.ppn);
        Vector3f ppp = vecTrans(center.add(0.5, 0.5, 0.5, 0, buf).mul(matrix), vBuf.ppp);
        BlockPos.Mutable mutablePos = vBuf.mutablePos.set(pos);
        mutablePos.setX(pos.getX() - 1);
        if(!shapes.testPos(mutablePos) || !markedBlocks.containsKey(mutablePos)){
            buffer.vertex(nnn).color(color);
            buffer.vertex(nnp).color(color);
            buffer.vertex(npp).color(color);
            buffer.vertex(npn).color(color);
        }
        mutablePos.setX(pos.getX() + 1);
        if(!shapes.testPos(mutablePos) || !markedBlocks.containsKey(mutablePos)){
            buffer.vertex(pnn).color(color);
            buffer.vertex(ppn).color(color);
            buffer.vertex(ppp).color(color);
            buffer.vertex(pnp).color(color);
        }
        mutablePos.setX(pos.getX());
        mutablePos.setY(pos.getY() - 1);
        if(!shapes.testPos(mutablePos) || !markedBlocks.containsKey(mutablePos)){
            buffer.vertex(nnn).color(color);
            buffer.vertex(pnn).color(color);
            buffer.vertex(pnp).color(color);
            buffer.vertex(nnp).color(color);
        }
        mutablePos.setY(pos.getY() + 1);
        if(!shapes.testPos(mutablePos) || !markedBlocks.containsKey(mutablePos)){
            buffer.vertex(npn).color(color);
            buffer.vertex(npp).color(color);
            buffer.vertex(ppp).color(color);
            buffer.vertex(ppn).color(color);
        }
        mutablePos.setY(pos.getY());
        mutablePos.setZ(pos.getZ() - 1);
        if(!shapes.testPos(mutablePos) || !markedBlocks.containsKey(mutablePos)){
            buffer.vertex(nnn).color(color);
            buffer.vertex(npn).color(color);
            buffer.vertex(ppn).color(color);
            buffer.vertex(pnn).color(color);
        }
        mutablePos.setZ(pos.getZ() + 1);
        if(!shapes.testPos(mutablePos) || !markedBlocks.containsKey(mutablePos)){
            buffer.vertex(nnp).color(color);
            buffer.vertex(pnp).color(color);
            buffer.vertex(ppp).color(color);
            buffer.vertex(npp).color(color);
        }
    }
}
