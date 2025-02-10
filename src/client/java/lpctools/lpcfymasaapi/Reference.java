package lpctools.lpcfymasaapi;

import fi.dy.masa.malilib.util.StringUtils;
import net.minecraft.MinecraftVersion;

public class Reference {
    public String modName;
    public String modId;
    public String modVersion;
    public String MCVersion;
    public String modType;
    public String modString;

    public Reference(String modName){
        this(modName, modName.toLowerCase());
    }

    public Reference(String modName, String modId){
        this.modName = modName;
        this.modId = modId;
        modVersion = StringUtils.getModVersionString(modId);
        MCVersion  = MinecraftVersion.CURRENT.getName();
        modType = "fabric";
        modString  = modId + "-" + modType + "-" + MCVersion + "-" + modVersion;
    }
}
