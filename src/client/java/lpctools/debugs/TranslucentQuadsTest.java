package lpctools.debugs;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.ButtonConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.ThirdListConfig;
import lpctools.lpcfymasaapi.render.Quad;
import lpctools.lpcfymasaapi.render.TranslucentQuads;
import net.minecraft.client.MinecraftClient;
import org.joml.Vector3d;

import java.util.Random;

import static lpctools.lpcfymasaapi.LPCConfigStatics.addButtonConfig;
import static lpctools.lpcfymasaapi.LPCConfigStatics.listStack;

@SuppressWarnings("unused")
public class TranslucentQuadsTest {
	public static TranslucentQuads quads = null;
	public static final ThirdListConfig translucentQuadsTest
		= new ThirdListConfig(DebugConfigs.debugs, "translucentQuadsTest", null);
	static {listStack.push(translucentQuadsTest);}
	public static final ButtonConfig addQuadButton = addButtonConfig("addQuad", (b, m)->addQuad());
	public static final ButtonConfig clearQuadsButton = addButtonConfig("clearQuads", (b, m)->clearQuads());
	static {listStack.pop();}
	
	private static void addQuad(){
		var player = MinecraftClient.getInstance().player;
		if(player == null) return;
		Vector3d pos = new Vector3d(player.getX(), player.getY(), player.getZ());
		Random rand = new Random();
		Quad quad = new Quad(pos.add(-0.5, 0, -0.5, new Vector3d()), new Vector3d(1.0, 0.0, 0.0), new Vector3d(0.0, 0.0, 1.0), rand.nextInt());
		if(quads == null) quads = new TranslucentQuads();
		quads.addQuad(quad, false);
	}
	private static void clearQuads(){
		if(quads == null) return;
		quads.close();
		quads = null;
	}
}
