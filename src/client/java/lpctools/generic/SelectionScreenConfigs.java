package lpctools.generic;

import fi.dy.masa.malilib.util.data.Color4f;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.ColorConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.ButtonConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.ThirdListConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.UniqueDoubleConfig;
import lpctools.lpcfymasaapi.screen.SelectionScreen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Random;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;

public class SelectionScreenConfigs {
	public static final ThirdListConfig selectionScreenConfigs
		= new ThirdListConfig(GenericConfigs.generic, "selectionScreenConfigs", null);
	static {listStack.push(selectionScreenConfigs);}
	@SuppressWarnings("unused")
	public static final ButtonConfig previewScreen = addButtonConfig("previewScreen", (button, mouseButton)->
		SelectionScreen.openSelectionScreen(Text.of("Preview Screen"), createPreviewScreenNode(), v->{}));
	public static final UniqueDoubleConfig verticalScrollSpeed = addConfigEx(p->new UniqueDoubleConfig(p, "verticalScrollSpeed", 3.0, 0.125, 72.0, null).useSlider().logMode());
	public static final UniqueDoubleConfig horizontalScrollSpeed = addConfigEx(p->new UniqueDoubleConfig(p, "horizontalScrollSpeed", 3.0, 0.125, 72.0, null).useSlider().logMode());
	public static final UniqueDoubleConfig approachSpeed = addConfigEx(p->new UniqueDoubleConfig(p, "approachSpeed", 8.0, 0.125, 512.0, null).useSlider().logMode());
	// TODO: 对齐方式
	public static final ColorConfig backgroundHighlightColor = addColorConfig("backgroundHighlightColor", Color4f.fromColor(0x1fffffff));
	public static final ColorConfig scrollBarTrackHighlightColor = addColorConfig("scrollBarTrackHighlightColor", Color4f.fromColor(0x1fffffff));
	public static final ColorConfig scrollBarTrackBaseColor = addColorConfig("scrollBarTrackBaseColor", Color4f.fromColor(0x1a000000));
	public static final ColorConfig scrollBarThumbHighlightColor = addColorConfig("scrollBarThumbHighlightColor", Color4f.fromColor(0xe6ffffff));
	public static final ColorConfig scrollBarThumbBaseColor = addColorConfig("scrollBarThumbBaseColor", Color4f.fromColor(0x99cccccc));
	public static final ColorConfig dividerHighlightColor = addColorConfig("dividerHighlightColor", Color4f.fromColor(0xe6ffffff));
	public static final ColorConfig dividerBaseColor = addColorConfig("dividerBaseColor", Color4f.fromColor(0x99cccccc));
	
	public static int getScrollBarBackgroundColor(boolean highlighted){
		return (highlighted ? scrollBarTrackHighlightColor : scrollBarTrackBaseColor).getIntegerValue();
	}
	public static int getScrollBarColor(boolean highlighted){
		return (highlighted ? scrollBarThumbHighlightColor : scrollBarThumbBaseColor).getIntegerValue();
	}
	public static int getDividerColor(boolean highlighted){
		return (highlighted ? dividerHighlightColor : dividerBaseColor).getIntegerValue();
	}
	
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
	private static SelectionScreen.OptionNode<Void> createPreviewScreenNode(){
		return new SelectionScreen.OptionNode<>(()->{
			ArrayList<SelectionScreen.IOption<Void>> res = new ArrayList<>();
			int len = random.nextInt(1, 100);
			for(int i = 0; i < len; ++i){
				if(random.nextBoolean()) res.add(createPreviewScreenNode());
				else res.add(new SelectionScreen.OptionLeaf<>(null, Text.of("leaf_" + randomString(random.nextInt(1, 10)))));
			}
			return res;
		}, Text.of("node_" + randomString(random.nextInt(1, 10))));
	}
}
