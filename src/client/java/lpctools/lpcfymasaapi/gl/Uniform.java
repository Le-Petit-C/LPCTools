package lpctools.lpcfymasaapi.gl;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL45;

import static lpctools.lpcfymasaapi.gl.LPCGLInitializer.initialized;

@SuppressWarnings({"ExternalizableWithoutPublicNoArgConstructor", "unused"})
public interface Uniform {
    void uniform();
    int getLocation();
    class UniformMatrix4f extends Matrix4f implements Uniform {
        UniformData uniform;
        public UniformMatrix4f(Program<?> program, String name) {
            uniform = new UniformData(program, name);
        }
        @Override public void uniform() {
            GL45.glUniformMatrix4fv(uniform.location, false, super.get(new float[16]));
        }
        @Override public int getLocation() {return uniform.location;}
    }
    class Uniform4f extends Vector4f implements Uniform {
        UniformData uniform;
        public Uniform4f(Program<?> program, String name) {
            uniform = new UniformData(program, name);
        }
        @Override public void uniform() {
            GL45.glUniform4fv(uniform.location, new float[]{x, y, z, w});
        }
        @Override public int getLocation() {return uniform.location;}
    }
}

//package private
class UniformData implements AutoCloseable{
    public final Program<?> program;
    public final String name;
    int location = -1;
    UniformData(Program<?> program, String name){
        this.program = program;
        this.name = name;
        LPCGLInitializer.uniforms.add(this);
        if(initialized()) updateLocation();
    }
    @Override public void close() {
        LPCGLInitializer.uniforms.remove(this);
        location = -1;
    }
    public void updateLocation() {
        location = GL45.glGetUniformLocation(program.getGlProgramId(), name);
    }
}
