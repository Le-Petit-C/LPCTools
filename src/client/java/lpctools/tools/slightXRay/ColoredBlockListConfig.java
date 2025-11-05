package lpctools.tools.slightXRay;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.util.data.Color4f;
import lpctools.lpcfymasaapi.Registries;
import lpctools.lpcfymasaapi.configButtons.UpdateTodo;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.BlockConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.ConfigListConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.UniqueColorConfig;
import lpctools.lpcfymasaapi.interfaces.IExpandableThirdList;
import lpctools.lpcfymasaapi.interfaces.ILPCConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.util.DataUtils;
import net.minecraft.block.Blocks;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

import static lpctools.lpcfymasaapi.Registries.CLIENT_RESOURCE_RELOAD;
import static lpctools.tools.slightXRay.SlightXRay.*;

public class ColoredBlockListConfig extends ConfigListConfig<ColoredBlockListConfig.ColoredBlockConfig> implements AutoCloseable, Registries.ResourceReloadCallback {
	public ColoredBlockListConfig(@NotNull ILPCConfigReadable parent, @NotNull String nameKey) {
		super(parent, nameKey, ColoredBlockConfig::new, null);
		CLIENT_RESOURCE_RELOAD.register(this);
	}
	public void updateDefaultColor(){
		iterateConfigs().forEach(c->c.color.updateDefaultColor());
		onValueChanged();
	}
	
	@Override public void onResourceReload(ResourceManager manager) {updateDefaultColor();}
	
	@Override public void close() throws Exception {
		super.close();
		CLIENT_RESOURCE_RELOAD.unregister(this);
	}
	
	public static class ColoredBlockConfig extends BlockConfig implements ILPCConfigReadable {
		boolean expanded = false;
		int alignedIndent;
		private final ColorMethodConfig color = new ColorMethodConfig();
		public ColoredBlockConfig(@NotNull ILPCConfigReadable parent) {super(parent, "block", Blocks.AIR, null);}
		@Override public @NotNull Iterable<? extends ILPCConfig> getConfigs() {return List.of(color);}
		
		@Override public void getButtonOptions(ButtonOptionArrayList res) {
			res.add(IExpandableThirdList.thirdListIconPreset(()->expanded, e->{
				expanded = e;
				getPage().markNeedUpdate();
			}));
			super.getButtonOptions(res);
		}
		
		@Override public void setAlignedIndent(int indent) {alignedIndent = indent;}
		@Override public int getAlignedIndent() {return alignedIndent;}
		public Color4f getColor(){return color.getColor();}
		
		@Override public ArrayList<GuiConfigsBase.ConfigOptionWrapper> buildConfigWrappers(ToIntFunction<String> getStringWidth, ArrayList<GuiConfigsBase.ConfigOptionWrapper> wrapperList) {
			if(expanded) return ILPCConfigReadable.super.buildConfigWrappers(getStringWidth, wrapperList);
			else return wrapperList;
		}
		
		@Override public void onValueChanged() {
			color.updateDefaultColor();
			super.onValueChanged();
		}
		
		@Override public @Nullable JsonElement getAsJsonElement() {
			JsonObject res = new JsonObject();
			res.add("block", super.getAsJsonElement());
			res.add("color", color.getAsJsonElement());
			return res;
		}
		
		@Override public UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement element) {
			if(element instanceof JsonObject object){
				UpdateTodo res = new UpdateTodo();
				if(object.get("block") instanceof JsonElement blockElement)
					res.combine(super.setValueFromJsonElementEx(blockElement));
				if(object.get("color") instanceof JsonElement colorElement){
					var todo = color.setValueFromJsonElementEx(colorElement);
					todo.apply(color);
					res.combine(todo);
				}
				return res;
			}
			else return setValueFailed(element);
		}
		
		public class ColorMethodConfig extends UniqueColorConfig {
			boolean followDefault = true;
			public static final Text defaultButtonText = Text.translatable("lpctools.configs.tools.SX.XRayBlocks.block.color.default");
			public static final Text customizeButtonText = Text.translatable("lpctools.configs.tools.SX.XRayBlocks.block.color.customize");
			public ColorMethodConfig() {super(ColoredBlockConfig.this, "color", 0, null);}
			@Override public void getButtonOptions(ButtonOptionArrayList res) {
				if(followDefault) res.add(1, (button, mouseButton)->toggleFollowMethod(), defaultButtonText::getString, buttonGenericAllocator);
				else {
					res.add(0.5f, (button, mouseButton)->toggleFollowMethod(), customizeButtonText::getString, buttonGenericAllocator);
					super.getButtonOptions(res);
				}
			}
			public void toggleFollowMethod(){
				followDefault = !followDefault;
				if(followDefault) resetToDefault();
				onValueChanged();
				getPage().markNeedUpdate();
			}
			public void updateDefaultColor(){
				defaultColor = Color4f.fromColor(DataUtils.argb2agbr(SlightXRay.defaultColorMethod.get().right.applyAsInt(getBlock())));
				if(followDefault) setColor(defaultColor);
			}
			
			@Override public @Nullable JsonElement getAsJsonElement() {
				if(followDefault) return JsonNull.INSTANCE;
				else return super.getAsJsonElement();
			}
			
			@Override public UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement element) {
				if(element instanceof JsonNull){
					if(!followDefault){
						toggleFollowMethod();
						return new UpdateTodo().valueChanged();
					}
					else return new UpdateTodo();
				}
				else return super.setValueFromJsonElementEx(element);
			}
			
			@Override public void onValueChanged() {
				markNeedRefreshXRayBlocks();
				super.onValueChanged();
			}
		}
	}
}
