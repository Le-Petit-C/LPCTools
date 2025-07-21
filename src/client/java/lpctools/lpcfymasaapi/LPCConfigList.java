package lpctools.lpcfymasaapi;

import com.google.gson.JsonElement;
import fi.dy.masa.malilib.config.ConfigType;
import lpctools.lpcfymasaapi.interfaces.ILPCConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigList;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class LPCConfigList implements ILPCConfig, ILPCConfigList {
	private final String nameKey;
	private final ILPCConfigReadable parent;
	private final ArrayList<ILPCConfig> subConfigs = new ArrayList<>();
	private int indent = 0;
	private String comment;
	private String translatedName;
	private String prettyName;
	private ILPCValueChangeCallback callback;
	public LPCConfigList(ILPCConfigReadable parent, String nameKey){
		this.parent = parent;
		this.nameKey = nameKey;
	}
	public boolean hasHotkeyConfig() {
		for(ILPCConfig config : getConfigs())
			if(config.hasHotkey()) return true;
		return false;
	}
	@Override public @NotNull ILPCConfigReadable getParent() {return parent;}
	@Override public @NotNull String getNameKey(){return nameKey;}
	@Override public @NotNull LPCConfigPage getPage() {return parent.getPage();}
	@Override public @NotNull ArrayList<ILPCConfig> getConfigs() {return subConfigs;}
	@Override public void setAlignedIndent(int indent) {this.indent = indent;}
	@Override public int getAlignedIndent() {return indent;}
	@Override public boolean hasHotkey() {return false;}
	@Override public ConfigType getType() {return null;}
	@Override public String getName() {return getNameKey();}
	@Override public String getComment() {return comment;}
	@Override public String getTranslatedName() {return translatedName;}
	@Override public String getPrettyName() {return prettyName;}
	@Override public void setPrettyName(String prettyName) {this.prettyName = prettyName;}
	@Override public void setTranslatedName(String translatedName) {this.translatedName = translatedName;}
	@Override public void setComment(String comment) {this.comment = comment;}
	@Override public void onValueChanged() {if(callback != null) callback.onValueChanged();}
	@Override public void setValueChangeCallback(@Nullable ILPCValueChangeCallback callback) {this.callback = callback;}
	@Override public @Nullable JsonElement getAsJsonElement() {return ILPCConfigList.super.getAsJsonElement();}
}
