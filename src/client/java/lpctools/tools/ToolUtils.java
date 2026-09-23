package lpctools.tools;

import fi.dy.masa.malilib.config.IConfigBoolean;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.util.InfoUtils;
import fi.dy.masa.malilib.util.StringUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import lpctools.generic.UpdateCounter;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.BooleanHotkeyThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.*;
import lpctools.util.LPCMathHelper;
import lpctools.util.Packed;
import lpctools.util.javaex.ToBooleanFunction;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings({"UnusedReturnValue", "unused"})
public class ToolUtils {
    // 通过设置配置的热键回调函数设置一个Boolean配置的切换文本显示为LPCTools默认风格
    public static <T extends IConfigBoolean & IHotkey & ILPCConfig> T setLPCToolsToggleText(T config){
        config.getKeybind().setCallback((action, key)->{
            displayToggleMessage(!config.getBooleanValue(), config);
            config.toggleBooleanValue();
            return true;
        });
        return config;
    }
    public static class RunnerCreateFailedException extends Exception {
        public final @Nullable Component failReason;
        public RunnerCreateFailedException(@Nullable Component failReason) { this.failReason = failReason; }
        public RunnerCreateFailedException(@Nullable String failReason) { this(failReason == null ? null : Component.literal(failReason)); }
        public RunnerCreateFailedException() { this.failReason = null; }
    }
    public interface ToolRunnerSupplier<T extends ToolRunner> { T createRunner() throws RunnerCreateFailedException; }
    // TODO: 其他工具也用这个Builder构建
    public static class ToolConfigBuilder {
        protected @NotNull ILPCConfigList parent;
        protected final String key;
        protected @Nullable ILPCValueChangeCallback callback;
        private ToolConfigBuilder(@NotNull ILPCConfigList parent, String key, @Nullable ILPCValueChangeCallback callback)
        { this.parent = parent; this.key = key; this.callback = callback; }
        public ToolConfigBuilder withExtraCallback(ILPCValueChangeCallback callback) {
            this.callback = callback;
            return this;
        }
        public <T extends ToolRunner> ToolWithRunnerConfigBuilder<T> withToolRunner(ToolRunnerSupplier<T> toolRunner) {
            return new ToolWithRunnerConfigBuilder<>(parent, key, callback, toolRunner);
        }
        public ToolConfigBuilder withParent(ILPCConfigList parent) {
            this.parent = parent;
            return this;
        }
        public BooleanHotkeyThirdListConfig build() {
            return setLPCToolsToggleText(new BooleanHotkeyThirdListConfig(parent, key, callback));
        }
    }
    public static class ToolWithRunnerConfigBuilder<T extends ToolRunner> extends ToolConfigBuilder {
        private final ToolRunnerSupplier<T> toolRunner;
        private ToolWithRunnerConfigBuilder(@NotNull ILPCConfigList parent, String key, @Nullable ILPCValueChangeCallback callback, ToolRunnerSupplier<T> toolRunner) {
            super(parent, key, callback);
            this.toolRunner = toolRunner;
        }
        @Override public ToolWithRunnerConfig<T> build() {
            return setLPCToolsToggleText(new ToolWithRunnerConfig<>(parent, key, toolRunner, callback));
        }

        @Override public ToolWithRunnerConfigBuilder<T> withExtraCallback(ILPCValueChangeCallback callback) { super.withExtraCallback(callback); return this; }
        @Override public ToolWithRunnerConfigBuilder<T> withParent(ILPCConfigList parent) { super.withParent(parent); return this; }
    }
    public interface ToolRunner { void registerAll(boolean b); }

    public static ToolConfigBuilder configBuilder(String key) {
        return new ToolConfigBuilder(ToolConfigs.toolConfigs, key, null);
    }
    public static void displayDisableReason(@NotNull ILPCConfig tool, @Nullable String reasonKey){
        String reason = StringUtils.translate("lpctools.tools.disableNotification", tool.getNameTranslation());
        if(reasonKey != null)
            reason += " : " + StringUtils.translate("lpctools.tools.disableReason." + reasonKey);
        InfoUtils.sendVanillaMessage(Component.literal(reason));
    }
    public static void displayDisableMessage(@NotNull ILPCConfig tool){displayDisableReason(tool, null);}
    public static void displayEnableMessage(@NotNull ILPCConfig tool){
        InfoUtils.sendVanillaMessage(Component.translatable("lpctools.tools.enableNotification", tool.getNameTranslation()));
    }
    
    public static void displayToggleMessage(boolean b, ILPCConfig tool){
        if(b) displayEnableMessage(tool);
        else displayDisableMessage(tool);
    }
    
    public static <T, U extends Collection<T>> U recordCollection(U result, @Nullable Collection<? extends T> source){
        if(source != null) result.addAll(source);
        return result;
    }
    public static <T, U extends Collection<T>, V> U recordCollection(U result, @Nullable V source, Function<? super V, ? extends Collection<? extends T>> mappingFunction){
        if(source != null) {
            var collection = mappingFunction.apply(source);
            UpdateCounter.updated(collection.size() / 16);
            int sz = collection.size();
            result.addAll(collection);
        }
        return result;
    }
    public static <T, U, V extends Map<T, U>> V recordMap(V result, @Nullable Map<? extends T, ? extends U> source){
        if(source != null) {
            result.putAll(source);
            UpdateCounter.updated(source.size() / 16);
        }
        return result;
    }
    
    public static <T, U extends Collection<T>> U combineCollections(@NotNull U collection1, @Nullable U collection2) {
        if(collection2 == null) return collection1;
        if(collection1.size() >= collection2.size()) {
            collection1.addAll(collection2);
            UpdateCounter.updated(collection2.size() / 16);
            return collection1;
        }
        else {
            collection2.addAll(collection1);
            UpdateCounter.updated(collection1.size() / 16);
            return collection2;
        }
    }

    public static <T> void clearMapDataOutOfRange(double chunkedCamX, double chunkedCamZ, double chunkDistanceLimitSquared, Long2ObjectMap<T> mapToClean, ToBooleanFunction<T> emptyCheck, Consumer<T> cleaner) {
        var it = mapToClean.long2ObjectEntrySet().iterator();
        while (it.hasNext()) {
            var entry = it.next();
            var obj = entry.getValue();
            if(emptyCheck != null && emptyCheck.applyAsBoolean(obj)) {
                it.remove();
                continue;
            }
            long packedChunkPos = entry.getLongKey();
            double distanceSquared = LPCMathHelper.squaredLength(
                Packed.unpackChunkPosX(packedChunkPos) - chunkedCamX,
                Packed.unpackChunkPosZ(packedChunkPos) - chunkedCamZ
            );
            if(distanceSquared >= chunkDistanceLimitSquared) {
                it.remove();
                if(cleaner != null) cleaner.accept(obj);
            }
        }
    }
}
