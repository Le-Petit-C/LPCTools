package lpctools.lpcfymasaapi;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.MinecraftVersion;

public class Reference {
    public final String modName;
    public final String modId;

    public static String getMCVersion(){return MinecraftVersion.CURRENT.getName();}
    public static String getModType(){return "fabric";}
    public Reference(String modName){
        this(modName, modName.toLowerCase());
    }
    public Reference(String modName, String modId){
        this.modName = modName;
        this.modId = modId;
    }
    public String getModVersion(){
        //return StringUtils.getModVersionString(modId);
        return FabricLoader.getInstance().getModContainer(modId).orElseThrow().getMetadata().getVersion().getFriendlyString();
    }
    @SuppressWarnings("unused")
    public String getModString(){return modId + "-" + getModType() + "-" + getMCVersion() + "-" + getModVersion();}
}
