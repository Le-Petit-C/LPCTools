package lpctools.lpcfymasaapi.configbutton.derivedConfigs;

import lpctools.compact.derived.ShapeList;
import lpctools.compact.derived.SimpleTestableShape;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.StringConfig;
import lpctools.lpcfymasaapi.implementations.ILPCConfigList;
import org.jetbrains.annotations.NotNull;

import static lpctools.lpcfymasaapi.configbutton.derivedConfigs.DerivedConfigUtils.*;

public class RangeLimitConfig extends ThirdListConfig {
    public final StringConfig prefix;
    public final ThirdListConfig litematica;//决定是否启用投影渲染范围限制
    public final ArrayOptionListConfig<SimpleTestableShape.TestType> testType;//决定投影渲染范围检测方式
    public RangeLimitConfig(ILPCConfigList list, boolean defaultBoolean, String defaultPrefix) {
        super(list, "limitRange", defaultBoolean);
        prefix = addConfig(new StringConfig(this, "rangeNamePrefix", defaultPrefix){
            @Override public @NotNull String getFullTranslationKey(){return fullKeyByParent(this);}
        });
        litematica = addConfig(new ThirdListConfig(this, "rangeLitematica", false){
            @Override public @NotNull String getFullTranslationKey(){return fullKeyByParent(this);}
        });
        testType = litematica.addConfig(new ArrayOptionListConfig<>(litematica, "renderRangeTestType"){
            @Override public @NotNull String getFullTranslationKey() {return fullKeyByParent(this);}
        });
        for(SimpleTestableShape.TestType testType : SimpleTestableShape.TestType.values())
            this.testType.addOption(testType.getPrefix(), testType);
    }
    public ShapeList buildShapeList(){
        if(getAsBoolean())
            return new ShapeList(litematica.getAsBoolean() ? testType.get() : null, prefix.get());
        else return ShapeList.emptyList();
    }

    @Override public @NotNull String getFullTranslationKey() {return "lpctools.configs.utils.limitRange";}
}
