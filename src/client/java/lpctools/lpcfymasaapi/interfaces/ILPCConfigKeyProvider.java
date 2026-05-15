package lpctools.lpcfymasaapi.interfaces;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface ILPCConfigKeyProvider{
    String getFullTranslationKey();
    //获取当前配置标题的完整本地化键名，用于按钮文本中
    default @NotNull String getFullTitleTranslationKey(){
        return getFullTranslationKey() + ".title";
    }
    //获取当前配置名称的完整本地化键名
    default @NotNull String getFullNameTranslationKey(){
        return getFullTranslationKey() + ".name";
    }
    //获取当前配置注解的完整本地化键名
    default @NotNull String getFullCommentTranslationKey(){
        return getFullTranslationKey() + ".comment";
    }
    //获取当前配置标题的完整本地化键名，用于按钮文本中
    default @NotNull String getTitleTranslation(){
        return Component.translatable(getFullTitleTranslationKey()).getString();
    }
    //获取当前配置名
    default @NotNull String getNameTranslation(){
        return Component.translatable(getFullNameTranslationKey()).getString();
    }
    //获取当前配置注解
    default @NotNull String getCommentTranslation(){
        return Component.translatable(getFullCommentTranslationKey()).getString();
    }
    static ILPCConfigKeyProvider of(String fullTranslationKey){return ()->fullTranslationKey;}
    static ILPCConfigKeyProvider of(String... strings){
        StringBuilder builder = new StringBuilder();
        for(String s : strings){
            if(s == null || s.isEmpty()) continue;
            if(!builder.isEmpty()) builder.append('.');
            builder.append(s);
        }
        return of(builder.toString());
    }
}
