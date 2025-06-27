package lpctools.lpcfymasaapi.gl;

import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;
import org.joml.*;
import org.lwjgl.opengl.GL45;

import static lpctools.lpcfymasaapi.gl.LPCGLInitializer.initialized;

@SuppressWarnings({"ExternalizableWithoutPublicNoArgConstructor", "unused"})
public interface Uniform {
    void uniform();
    int getLocation();
    class UniformMatrix4f extends Matrix4f implements Uniform {
        UniformData uniform;
        public UniformMatrix4f(Program program, String name) {uniform = new UniformData(program, name);}
        @Override public void uniform() {GL45.glUniformMatrix4fv(uniform.location, false, super.get(new float[16]));}
        @Override public int getLocation() {return uniform.location;}
    }
    class Uniform4f extends Vector4f implements Uniform {
        UniformData uniform;
        public Uniform4f(Program program, String name) {uniform = new UniformData(program, name);}
        @Override public void uniform() {GL45.glUniform4f(uniform.location, x, y, z, w);}
        @Override public int getLocation() {return uniform.location;}
    }
    class Uniform2f extends Vector2f implements Uniform {
        UniformData uniform;
        public Uniform2f(Program program, String name) {uniform = new UniformData(program, name);}
        @Override public void uniform() {GL45.glUniform2f(uniform.location, x, y);}
        @Override public int getLocation() {return uniform.location;}
    }
    class Uniform1f extends MutableFloat implements Uniform {
        UniformData uniform;
        public Uniform1f(Program program, String name) {uniform = new UniformData(program, name);}
        @Override public void uniform() {GL45.glUniform1f(uniform.location, floatValue());}
        @Override public int getLocation() {return uniform.location;}
    }
    class Uniform2d extends Vector2d implements Uniform {
        UniformData uniform;
        public Uniform2d(Program program, String name) {uniform = new UniformData(program, name);}
        @Override public void uniform() {GL45.glUniform2d(uniform.location, x, y);}
        @Override public int getLocation() {return uniform.location;}
    }
    class Uniform1i extends MutableInt implements Uniform {
        UniformData uniform;
        public Uniform1i(Program program, String name) {uniform = new UniformData(program, name);}
        @Override public void uniform() {GL45.glUniform1i(uniform.location, intValue());}
        @Override public int getLocation() {return uniform.location;}
    }
    class Uniform2i extends Vector2i implements Uniform {
        UniformData uniform;
        public Uniform2i(Program program, String name) {uniform = new UniformData(program, name);}
        @Override public void uniform() {GL45.glUniform2i(uniform.location, x, y);}
        @Override public int getLocation() {return uniform.location;}
    }
    class Uniform4i extends Vector4i implements Uniform {
        UniformData uniform;
        public Uniform4i(Program program, String name) {uniform = new UniformData(program, name);}
        @Override public void uniform() {GL45.glUniform4i(uniform.location, x, y, z, w);}
        @Override public int getLocation() {return uniform.location;}
    }
    class Uniform4ui extends Vector4i implements Uniform {
        UniformData uniform;
        public Uniform4ui(Program program, String name) {uniform = new UniformData(program, name);}
        @Override public void uniform() {GL45.glUniform4ui(uniform.location, x, y, z, w);}
        @Override public int getLocation() {return uniform.location;}
    }
}

//package private
class UniformData implements AutoCloseable{
    public final Program program;
    public final String name;
    int location = -1;
    UniformData(Program program, String name){
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
