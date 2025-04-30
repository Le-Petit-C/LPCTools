package lpctools.lpcfymasaapi.configbutton.derivedConfigs;

import lpctools.compact.derived.ShapeList;
import lpctools.compact.derived.SimpleTestableShape;
import lpctools.lpcfymasaapi.configbutton.*;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.OptionListConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.StringConfig;
import lpctools.util.LanguageExtra;
import org.jetbrains.annotations.NotNull;

public class RangeLimitConfig extends ThirdListConfig {
    public final StringConfig prefix;
    public final ThirdListConfig litematica;//决定是否启用投影渲染范围限制
    public final OptionListConfig<SimpleTestableShape.TestType> testType;//决定投影渲染范围检测方式
    public RangeLimitConfig(ILPCConfigList list, boolean defaultBoolean, String defaultPrefix) {
        super(list, "limitRange", defaultBoolean);
        addRedirect(this, "");
        prefix = addStringConfig("rangeNamePrefix", defaultPrefix);
        addRedirect(prefix, ".rangeNamePrefix");
        litematica = addThirdListConfig("rangeLitematica", false);
        addRedirect(litematica, ".rangeLitematica");
        OptionListConfig.OptionList<SimpleTestableShape.TestType> optionList = new OptionListConfig.OptionList<>();
        for(SimpleTestableShape.TestType testType : SimpleTestableShape.TestType.values())
            optionList.addOption(testType.getPrefix(), testType);
        testType = litematica.addOptionListConfig("renderRangeTestType", optionList.getFirst());
        addRedirect(testType, ".renderRangeTestType");
    }
    public ShapeList buildShapeList(){
        if(getAsBoolean())
            return new ShapeList(litematica.getAsBoolean() ? testType.getCurrentUserdata() : null, prefix.get());
        else return ShapeList.emptyList();
    }

    private static void addRedirect(@NotNull ILPC_MASAConfigWrapper<?> config, @NotNull String key){
        LanguageExtra.redirectPut(config.getFullNameTranslationKey(),
                "lpctools.configs.utils.limitRange" + key + ".name");
        LanguageExtra.redirectPut(config.getFullCommentTranslationKey(),
                "lpctools.configs.utils.limitRange" + key + ".comment");
    }
}
