package lpctools.lpcfymasaapi.interfaces;

import com.google.gson.JsonElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IThirdListBase extends ILPCConfig, ILPCConfigList{
    @Override default @Nullable JsonElement getAsJsonElement() {
        return ILPCConfigList.super.getAsJsonElement();
    }
    @Override default void setValueFromJsonElement(@NotNull JsonElement data) {
        ILPCConfigList.super.setValueFromJsonElement(data);
    }
    String propertiesId = "properties";
}
