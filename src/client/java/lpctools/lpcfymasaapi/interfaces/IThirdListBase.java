package lpctools.lpcfymasaapi.interfaces;

import com.google.gson.JsonElement;
import org.jetbrains.annotations.Nullable;

public interface IThirdListBase extends ILPCConfig, ILPCConfigList{
    @Override default @Nullable JsonElement getAsJsonElement() {
        return ILPCConfigList.super.getAsJsonElement();
    }
    String propertiesId = "properties";
}
