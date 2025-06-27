package lpctools.tools.canSpawnDisplay;

import lpctools.lpcfymasaapi.gl.*;
import lpctools.lpcfymasaapi.gl.furtherWarpped.VertexAttribElements;
import lpctools.shader.FragmentShaders;
import lpctools.shader.VertexShaders;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL45;

import static lpctools.shader.ShaderPrograms.*;
import static lpctools.lpcfymasaapi.gl.Uniform.*;

//canSpawnDisplay专用RenderProgram类
public class RenderProgram extends Program implements WithFinalMatrix, WithStaticColor, WithModelViewMatrix {
    public static final Shader vertexShader = VertexShaders.newLPCVert("can_spawn_display.glsl");
    public static final VertexAttrib instanceAttrib = new VertexAttrib(VertexAttribElements.F32VEC3);
    public static final VertexAttrib userAttrib = new VertexAttrib(VertexAttribElements.F32VEC3);
    public static final RenderProgram program = new RenderProgram();
    public final UniformMatrix4f finalMatrix = addUniform(new UniformMatrix4f(this, "matrix"));
    public final UniformMatrix4f modelMatrix = addUniform(new UniformMatrix4f(this, "modelMatrix"));
    public final Uniform4f staticColor = addUniform(new Uniform4f(this, "color"));
    public final Uniform1f distanceSquared = addUniform(new Uniform1f(this, "distanceSquared"));
    private RenderProgram() {super(vertexShader, FragmentShaders.static_color, new VertexAttrib(instanceAttrib, userAttrib));}
    public void attribAndEnable(@NotNull Buffer instanceBuffer, @NotNull Buffer userBuffer){
        instanceBuffer.bindAsArray();
        instanceAttrib.attribAndEnable();
        GL45.glVertexAttribDivisor(0, 1);
        userBuffer.bindAsArray();
        userAttrib.attribAndEnableShifted(1);
    }
    public void setUniformCache(Matrix4f finalMatrix, Matrix4f modelMatrix, int color, double distanceSquared){
        setFinalMatrix(finalMatrix);
        setModelMatrix(modelMatrix);
        setColor32(color);
        setDistanceSquared(distanceSquared);
    }
    @Override public void setFinalMatrix(Matrix4f matrix) {finalMatrix.set(matrix);}
    @Override public void setColor4f(Vector4f color) {staticColor.set(color);}
    @Override public void setColor32(int color) {staticColor.set(WithStaticColor.color322color4f(color));}
    @Override public void setModelMatrix(Matrix4f matrix) {modelMatrix.set(matrix);}
    public void setDistanceSquared(double distanceSquared){this.distanceSquared.setValue(distanceSquared);}
}
