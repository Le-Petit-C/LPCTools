package lpctools.lpcfymasaapi.interfaces;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

public interface IThirdListBase extends ILPCConfig, ILPCConfigList{
    @Override default @Nullable JsonObject getAsJsonElement() {
        return ILPCConfigList.super.getAsJsonElement();
    }
    String propertiesId = "properties";
}
