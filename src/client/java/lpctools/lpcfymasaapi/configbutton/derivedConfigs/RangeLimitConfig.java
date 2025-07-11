package lpctools.lpcfymasaapi.configbutton.derivedConfigs;

import fi.dy.masa.malilib.interfaces.IRangeChangeListener;
import lpctools.compact.derived.ShapeList;
import lpctools.compact.derived.SimpleTestableShape;
import lpctools.lpcfymasaapi.Registries;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.StringConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigList;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.NotNull;

import static lpctools.lpcfymasaapi.configbutton.derivedConfigs.DerivedConfigUtils.*;

//不只在配置被修改时会调用onValueChanged，形状发生变化时也会
public class RangeLimitConfig extends ThirdListConfig implements Registries.ScreenChangeCallback, AutoCloseable, IRangeChangeListener {
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
        registerAll(true);
    }
    @Override public void close() {registerAll(false);}
    public ShapeList buildShapeList(){
        if(getAsBoolean())
            return new ShapeList(litematica.getAsBoolean() ? testType.get() : null, prefix.get());
        else return ShapeList.emptyList();
    }
    protected void registerAll(boolean b){
        Registries.ON_SCREEN_CHANGED.register(this, b);
        Registries.LITEMATICA_RANGE_CHANGED.register(this, b);
    }
    @Override public @NotNull String getFullTranslationKey() {return fullKeyFromUtilBase(this);}
    @Override public void onScreenChanged(Screen newScreen) {onValueChanged();}
    @Override public void updateAll() {onValueChanged();}
    @Override public void updateBetweenX(int minX, int maxX) {onValueChanged();}
    @Override public void updateBetweenY(int minY, int maxY) {onValueChanged();}
    @Override public void updateBetweenZ(int minZ, int maxZ) {onValueChanged();}
}
