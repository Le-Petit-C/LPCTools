package lpctools.util;

import lpctools.LPCTools;
import org.lwjgl.opengl.GL11;

import java.util.function.IntConsumer;

public class RenderUtils {
	/** 检查并输出所有待处理的 OpenGL 错误到 LPCTools logger */
	public static void checkGlErrors(String label, boolean output) {
		IntConsumer outputter;
		if(output) {
			if(label != null) outputter = e -> LPCTools.LOGGER.warn("[GL Error|{}] {} (0x{})", label, getGlErrorName(e), Integer.toHexString(e));
			else outputter = e -> LPCTools.LOGGER.warn("[GL Error] {} (0x{})", getGlErrorName(e), Integer.toHexString(e));
		}
		else outputter = _ -> {};
		while (true) {
			int error = GL11.glGetError();
			if(error == GL11.GL_NO_ERROR) break;
			outputter.accept(error);
		}
	}

	public static void checkGlErrors(String label) { checkGlErrors(label, true); }
	public static void clearGlErrors() { checkGlErrors(null, false); }

	private static String getGlErrorName(int error) {
		return switch (error) {
			case GL11.GL_NO_ERROR -> "NO_ERROR";
			case GL11.GL_INVALID_ENUM -> "INVALID_ENUM";
			case GL11.GL_INVALID_VALUE -> "INVALID_VALUE";
			case GL11.GL_INVALID_OPERATION -> "INVALID_OPERATION";
			case GL11.GL_STACK_OVERFLOW -> "STACK_OVERFLOW";
			case GL11.GL_STACK_UNDERFLOW -> "STACK_UNDERFLOW";
			case GL11.GL_OUT_OF_MEMORY -> "OUT_OF_MEMORY";
			default -> "UNKNOWN";
		};
	}

}
