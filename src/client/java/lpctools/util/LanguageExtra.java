package lpctools.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class LanguageExtra {
    public static final HashMap<String, @NotNull String> redirectMap = new HashMap<>();
    @SuppressWarnings("UnusedReturnValue")
    @Nullable public static String redirectPut(@NotNull String key, @NotNull String redirectDestination){
        return redirectMap.put(key, redirectDestination);
    }
    @Nullable public static String redirectGet(@NotNull String key){
        return redirectMap.get(key);
    }
    public static boolean redirectContainsKey(String key){
        return redirectMap.containsKey(key);
    }
}
