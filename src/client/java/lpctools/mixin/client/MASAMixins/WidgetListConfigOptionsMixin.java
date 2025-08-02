package lpctools.mixin.client.MASAMixins;

import com.llamalad7.mixinextras.sugar.Local;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.interfaces.ISelectionListener;
import fi.dy.masa.malilib.gui.widgets.WidgetConfigOptionBase;
import fi.dy.masa.malilib.gui.widgets.WidgetListBase;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptions;
import lpctools.lpcfymasaapi.LPCConfigPage;
import lpctools.lpcfymasaapi.interfaces.ILPCConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigBase;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import static lpctools.generic.GenericConfigs.*;

@Mixin(value = WidgetListConfigOptions.class, remap = false)
public abstract class WidgetListConfigOptionsMixin<TYPE, WIDGET extends WidgetConfigOptionBase<TYPE>> extends WidgetListBase<TYPE, WIDGET> {
	public WidgetListConfigOptionsMixin(int x, int y, int width, int height, ISelectionListener<TYPE> selectionListener) {
		super(x, y, width, height, selectionListener);
	}
	
	@ModifyArg(method = "createListEntryWidget(IIIZLfi/dy/masa/malilib/gui/GuiConfigsBase$ConfigOptionWrapper;)Lfi/dy/masa/malilib/gui/widgets/WidgetConfigOption;",
		at = @At(value = "INVOKE", target = "Lfi/dy/masa/malilib/gui/widgets/WidgetConfigOption;<init>(IIIIIILfi/dy/masa/malilib/gui/GuiConfigsBase$ConfigOptionWrapper;ILfi/dy/masa/malilib/gui/interfaces/IKeybindConfigGui;Lfi/dy/masa/malilib/gui/widgets/WidgetListConfigOptionsBase;)V"),
	index = 0)
	int modifyX(int x, @Local(argsOnly = true) GuiConfigsBase.ConfigOptionWrapper wrapper){
		if(wrapper.getConfig() instanceof ILPCConfig config){
			if(useLabelIndent.getAsBoolean()){
				ILPCConfigReadable parent = config.getParent().getParent();
				while(!(parent instanceof LPCConfigPage)){
					x += parent.getAlignedIndent();
					parent = parent.getParent();
				}
			}
			x += indentShift.getAsInt() * config.getAlignLevel() + indentAll.getAsInt();
		}
		return x;
	}
	@ModifyArg(method = "createListEntryWidget(IIIZLfi/dy/masa/malilib/gui/GuiConfigsBase$ConfigOptionWrapper;)Lfi/dy/masa/malilib/gui/widgets/WidgetConfigOption;",
		at = @At(value = "INVOKE", target = "Lfi/dy/masa/malilib/gui/widgets/WidgetConfigOption;<init>(IIIIIILfi/dy/masa/malilib/gui/GuiConfigsBase$ConfigOptionWrapper;ILfi/dy/masa/malilib/gui/interfaces/IKeybindConfigGui;Lfi/dy/masa/malilib/gui/widgets/WidgetListConfigOptionsBase;)V"),
		index = 4)
	int modifyLabelWidth(int labelWidth, @Local(argsOnly = true) GuiConfigsBase.ConfigOptionWrapper wrapper){
		if(useLabelIndent.getAsBoolean()
			&& wrapper.getConfig() instanceof ILPCConfigBase config
			&& config.getParent() instanceof ILPCConfigReadable readable)
			return readable.getAlignedIndent();
		return labelWidth;
	}
}
