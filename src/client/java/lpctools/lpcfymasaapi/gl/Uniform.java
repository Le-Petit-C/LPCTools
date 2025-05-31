package lpctools.lpcfymasaapi.gl;

import net.minecraft.resource.ResourceManager;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL30;

import java.util.function.Consumer;

@SuppressWarnings("ExternalizableWithoutPublicNoArgConstructor")
public class Uniform implements AutoCloseable, Consumer<ResourceManager>{
    public interface IUniform{
        void upload();
    }
    public final Program program;
    public final String name;
    private int location = -1;
    public int getLocation() {return location;}
    private Uniform(Program program, String name){
        this.program = program;
        this.name = name;
        Initializer.register(this);
    }
    @Override public void close() {
        Initializer.unregister(this);
    }
    @Override public void accept(ResourceManager manager) {
        location = GL30.glGetUniformLocation(program.getGlProgramId(), name);
    }
    public static class UniformMatrix4f extends Matrix4f implements IUniform {
        Uniform uniform;
        public UniformMatrix4f(Program program, String name) {
            uniform = new Uniform(program, name);
        }
        @Override public void upload() {
            GL30.glUniformMatrix4fv(uniform.location, false, super.get(new float[16]));
        }
    }
    public static class Uniform4f extends Vector4f implements IUniform {
        Uniform uniform;
        public Uniform4f(Program program, String name, boolean transpose) {
            uniform = new Uniform(program, name);
        }
        @Override public void upload() {
            GL30.glUniform4fv(uniform.location, new float[]{x, y, z, w});
        }
    }
}
