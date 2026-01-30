package lpctools.generic;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.ButtonConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.ThirdListConfig;
import lpctools.lpcfymasaapi.screen.NewChooseScreen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Random;

import static lpctools.lpcfymasaapi.LPCConfigStatics.addButtonConfig;
import static lpctools.lpcfymasaapi.LPCConfigStatics.listStack;

public class ChooseScreenConfigs {
	public static final ThirdListConfig chooseScreenConfigs
		= new ThirdListConfig(GenericConfigs.generic, "chooseScreenConfigs", null);
	static {listStack.push(chooseScreenConfigs);}
	@SuppressWarnings("unused")
	public static final ButtonConfig randomChooseScreen = addButtonConfig("randomChooseScreen", (button, mouseButton)->
		NewChooseScreen.openChooseScreen(Text.of("Random Choose Screen"), createRandomChooseScreenNode(), v->{}));
	static {listStack.pop();}
	
	private static final Random random = new Random();
	private static String randomString(int length){
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<length;i++){
			char c = (char)('a' + random.nextInt(26));
			sb.append(c);
		}
		return sb.toString();
	}
	private static NewChooseScreen.OptionNode<Void> createRandomChooseScreenNode(){
		return new NewChooseScreen.OptionNode<>(()->{
			ArrayList<NewChooseScreen.IOption<Void>> res = new ArrayList<>();
			int len = random.nextInt(1, 100);
			for(int i = 0; i < len; ++i){
				if(random.nextBoolean()) res.add(createRandomChooseScreenNode());
				else res.add(new NewChooseScreen.OptionLeaf<>(null, Text.of("leaf_" + randomString(random.nextInt(1, 10)))));
			}
			return res;
		}, Text.of("node_" + randomString(random.nextInt(1, 10))));
	}
}
