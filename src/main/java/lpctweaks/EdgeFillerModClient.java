package lpctweaks;

import net.fabricmc.api.ClientModInitializer;
//import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
//import net.minecraft.client.option.KeyBinding;
//import org.lwjgl.glfw.GLFW;

public class EdgeFillerModClient implements ClientModInitializer {
	//public static KeyBinding myKeyBinding;

	@Override
	public void onInitializeClient() {
		// 注册热键
		/*myKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.edge_filler.active", // 热键的名称
				GLFW.GLFW_KEY_G, // 设定的键位
				"edge_filler.active" // 热键分类
		));*/
	}
}