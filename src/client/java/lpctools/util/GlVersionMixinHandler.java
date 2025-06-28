package lpctools.util;

import org.lwjgl.glfw.GLFW;

public class GlVersionMixinHandler {
    private static final int TARGET_MAJOR = 3;
    private static final int TARGET_MINOR = 3;
    private static int recordedMajor = 0;
    private static int recordedMinor = 0;
    public static void resetRequiredGlVersion() {
        if(recordedMajor < TARGET_MAJOR ||
            recordedMajor == TARGET_MAJOR && recordedMinor < TARGET_MINOR){
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, TARGET_MAJOR);
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, TARGET_MINOR);
        }
    }
    public static void recordVersion(int hint, int value){
        if(hint == GLFW.GLFW_CONTEXT_VERSION_MAJOR) recordedMajor = value;
        if(hint == GLFW.GLFW_CONTEXT_VERSION_MINOR) recordedMinor = value;
    }
}
