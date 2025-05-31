package lpctools.lpcfymasaapi.gl;

import net.minecraft.resource.ResourceManager;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL30;

import java.util.function.Consumer;

@SuppressWarnings({"ExternalizableWithoutPublicNoArgConstructor", "unused"})
public class Uniforms{
    public interface IUniform{
        void uniform();
        int getLocation();
    }
    public static class UniformMatrix4f extends Matrix4f implements IUniform {
        UniformData uniform;
        public UniformMatrix4f(Program program, String name) {
            uniform = new UniformData(program, name);
        }
        @Override public void uniform() {
            GL30.glUniformMatrix4fv(uniform.location, false, super.get(new float[16]));
        }
        @Override public int getLocation() {return uniform.location;}
    }
    public static class Uniform4f extends Vector4f implements IUniform {
        UniformData uniform;
        public Uniform4f(Program program, String name) {
            uniform = new UniformData(program, name);
        }
        @Override public void uniform() {
            GL30.glUniform4fv(uniform.location, new float[]{x, y, z, w});
        }
        @Override public int getLocation() {return uniform.location;}
    }
    //package-private
    static class UniformData implements AutoCloseable, Consumer<ResourceManager>{
        public final Program program;
        public final String name;
        private int location = -1;
        private UniformData(Program program, String name){
            this.program = program;
            this.name = name;
            LPCGLInitializer.register(this);
        }
        @Override public void close() {
            LPCGLInitializer.unregister(this);
        }
        @Override public void accept(ResourceManager manager) {
            location = GL30.glGetUniformLocation(program.getGlProgramId(), name);
        }
    }
}
