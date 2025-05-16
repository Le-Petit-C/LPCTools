package lpctools.lpcfymasaapi.configbutton.derivedConfigs;

import lpctools.compact.derived.ShapeList;
import lpctools.compact.derived.SimpleTestableShape;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.StringConfig;
import lpctools.lpcfymasaapi.implementations.ILPCConfig;
import lpctools.lpcfymasaapi.implementations.ILPCConfigList;
import lpctools.util.LanguageExtra;
import org.jetbrains.annotations.NotNull;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;

public class RangeLimitConfig extends ThirdListConfig {
    public final StringConfig prefix;
    public final ThirdListConfig litematica;//决定是否启用投影渲染范围限制
    public final ArrayOptionListConfig<SimpleTestableShape.TestType> testType;//决定投影渲染范围检测方式
    public RangeLimitConfig(ILPCConfigList list, boolean defaultBoolean, String defaultPrefix) {
        super(list, "limitRange", defaultBoolean);
        addRedirect(this, "");
        prefix = addStringConfig(this, "rangeNamePrefix", defaultPrefix);
        addRedirect(prefix, ".rangeNamePrefix");
        litematica = addThirdListConfig(this, "rangeLitematica", false);
        addRedirect(litematica, ".rangeLitematica");
        testType = addArrayOptionListConfig(litematica, "renderRangeTestType");
        for(SimpleTestableShape.TestType testType : SimpleTestableShape.TestType.values())
            this.testType.addOption(testType.getPrefix(), testType);
        addRedirect(testType, ".renderRangeTestType");
    }
    public ShapeList buildShapeList(){
        if(getAsBoolean())
            return new ShapeList(litematica.getAsBoolean() ? testType.get() : null, prefix.get());
        else return ShapeList.emptyList();
    }

    private static void addRedirect(@NotNull ILPCConfig config, @NotNull String key){
        LanguageExtra.redirectPut(config.getFullNameTranslationKey(),
                "lpctools.configs.utils.limitRange" + key + ".name");
        LanguageExtra.redirectPut(config.getFullCommentTranslationKey(),
                "lpctools.configs.utils.limitRange" + key + ".comment");
    }
}
