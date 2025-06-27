package lpctools.tools.slightXRay;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

public class RenderQuad{
    public final BlockPos attachedBlock;
    public final Direction direction;
    public final MutableInt color;
    public final Vec3d centerPos;
    public RenderQuad(BlockPos attachedBlock, Direction direction, @NotNull MutableInt color){
        this.attachedBlock = attachedBlock;
        this.direction = direction;
        this.color = color;
        this.centerPos = attachedBlock.toCenterPos().offset(direction, 0.5);
    }
    public void vertex(ByteBuffer vertexBuffer, int offsetX, int offsetZ){
        float minX = attachedBlock.getX() + offsetX, maxX = minX + 1;
        float minY = attachedBlock.getY(), maxY = minY + 1;
        float minZ = attachedBlock.getZ() + offsetZ, maxZ = minZ + 1;
        int color = this.color.intValue();
        switch (direction){
            case WEST -> {
                vertexBuffer.putFloat(minX).putFloat(minY).putFloat(minZ).putInt(color);
                vertexBuffer.putFloat(minX).putFloat(minY).putFloat(maxZ).putInt(color);
                vertexBuffer.putFloat(minX).putFloat(maxY).putFloat(maxZ).putInt(color);
                vertexBuffer.putFloat(minX).putFloat(maxY).putFloat(minZ).putInt(color);
            }
            case EAST -> {
                vertexBuffer.putFloat(maxX).putFloat(minY).putFloat(minZ).putInt(color);
                vertexBuffer.putFloat(maxX).putFloat(maxY).putFloat(minZ).putInt(color);
                vertexBuffer.putFloat(maxX).putFloat(maxY).putFloat(maxZ).putInt(color);
                vertexBuffer.putFloat(maxX).putFloat(minY).putFloat(maxZ).putInt(color);
            }
            case DOWN -> {
                vertexBuffer.putFloat(minX).putFloat(minY).putFloat(minZ).putInt(color);
                vertexBuffer.putFloat(maxX).putFloat(minY).putFloat(minZ).putInt(color);
                vertexBuffer.putFloat(maxX).putFloat(minY).putFloat(maxZ).putInt(color);
                vertexBuffer.putFloat(minX).putFloat(minY).putFloat(maxZ).putInt(color);
            }
            case UP -> {
                vertexBuffer.putFloat(minX).putFloat(maxY).putFloat(minZ).putInt(color);
                vertexBuffer.putFloat(minX).putFloat(maxY).putFloat(maxZ).putInt(color);
                vertexBuffer.putFloat(maxX).putFloat(maxY).putFloat(maxZ).putInt(color);
                vertexBuffer.putFloat(maxX).putFloat(maxY).putFloat(minZ).putInt(color);
            }
            case NORTH -> {
                vertexBuffer.putFloat(minX).putFloat(minY).putFloat(minZ).putInt(color);
                vertexBuffer.putFloat(minX).putFloat(maxY).putFloat(minZ).putInt(color);
                vertexBuffer.putFloat(maxX).putFloat(maxY).putFloat(minZ).putInt(color);
                vertexBuffer.putFloat(maxX).putFloat(minY).putFloat(minZ).putInt(color);
            }
            case SOUTH -> {
                vertexBuffer.putFloat(minX).putFloat(minY).putFloat(maxZ).putInt(color);
                vertexBuffer.putFloat(maxX).putFloat(minY).putFloat(maxZ).putInt(color);
                vertexBuffer.putFloat(maxX).putFloat(maxY).putFloat(maxZ).putInt(color);
                vertexBuffer.putFloat(minX).putFloat(maxY).putFloat(maxZ).putInt(color);
            }
        }
    }
}
