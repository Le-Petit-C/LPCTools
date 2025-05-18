package lpctools.lpcfymasaapi.configbutton.derivedConfigs;

import lpctools.lpcfymasaapi.implementations.ILPCConfig;
import lpctools.lpcfymasaapi.implementations.ILPCConfigList;
import lpctools.util.LanguageExtra;
import org.jetbrains.annotations.NotNull;

import java.util.Stack;

public class DerivedConfigUtils {
    public static final Stack<ILPCConfigList> parentConfigs = new Stack<>();
    public static class ParentConfigLayer implements AutoCloseable{
        ParentConfigLayer(ILPCConfigList parent){parentConfigs.push(parent);}
        @Override public void close() {parentConfigs.pop();}
    }
    public static void addRedirect(@NotNull ILPCConfig config){
        StringBuilder builder = new StringBuilder();
        builder.append("lpctools.configs.utils.");
        for(ILPCConfigList parent : parentConfigs)
            builder.append(parent.getNameKey()).append('.');
        builder.append(config.getNameKey());
        String prefix = builder.toString();
        LanguageExtra.redirectPut(config.getFullNameTranslationKey(), prefix + ".name");
        LanguageExtra.redirectPut(config.getFullCommentTranslationKey(), prefix + ".comment");
    }
}
