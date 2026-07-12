package lpctools.tools.litematicaMaterial;

import com.google.common.collect.ImmutableList;
import lpctools.compact.CompactMain;
import lpctools.compact.litematica.LitematicaMethods;
import lpctools.lpcfymasaapi.Registries;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.BooleanHotkeyConfig;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.StringListConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.BooleanHotkeyThirdListConfig;
import lpctools.mixin.client.accessors.ConfigStringListAccessor;
import lpctools.tools.ToolConfigs;
import lpctools.tools.ToolUtils;
import lpctools.util.CachedSupplier;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;

public class LitematicaMaterial {
    private static @Nullable final LitematicaMaterialRunner runner;
    static {
        LitematicaMethods methods = CompactMain.getLitematicaInstance();
        if(methods != null) runner = new LitematicaMaterialRunner(methods);
        else runner = null;
    }
    public static final BooleanHotkeyThirdListConfig LMConfig = new BooleanHotkeyThirdListConfig(ToolConfigs.toolConfigs, "LM", runner);
    static { ToolUtils.setLPCToolsToggleText(LMConfig); }
    static { listStack.push(LMConfig); }
    public static final StringListConfig warehouseContainersConfig = addStringListConfig("warehouseContainers", ImmutableList.of(), LitematicaMaterial::invalidateProtectedContainers);
    public static final StringListConfig materialContainersConfig = addStringListConfig("materialContainers", ImmutableList.of(), LitematicaMaterial::invalidateMaterialContainers);
    public static final BooleanHotkeyConfig buildingMode = addBooleanHotkeyConfig("buildingMode", false, "");
    static { listStack.pop(); }

    public static final CachedSupplier<HashSet<String>> warehouseContainers = new CachedSupplier<>(()->new HashSet<>(warehouseContainersConfig.get()));
    public static final CachedSupplier<HashSet<String>> materialContainers = new CachedSupplier<>(()->new HashSet<>(materialContainersConfig.get()));

    private static void invalidateProtectedContainers() { warehouseContainers.invalidate(); }
    private static void invalidateMaterialContainers() { materialContainers.invalidate(); }
    private static void refreshDefaultValues() {
        ((ConfigStringListAccessor) warehouseContainersConfig).setDefaultValue(ImmutableList.of(
            Component.translatable("container.chest").getString(),
            Component.translatable("container.chestDouble").getString(),
            Component.translatable("container.barrel").getString(),
            Component.translatable("container.shulkerBox").getString(),
            Component.translatable("block.minecraft.white_shulker_box").getString(),
            Component.translatable("block.minecraft.light_gray_shulker_box").getString(),
            Component.translatable("block.minecraft.gray_shulker_box").getString(),
            Component.translatable("block.minecraft.black_shulker_box").getString(),
            Component.translatable("block.minecraft.brown_shulker_box").getString(),
            Component.translatable("block.minecraft.red_shulker_box").getString(),
            Component.translatable("block.minecraft.orange_shulker_box").getString(),
            Component.translatable("block.minecraft.yellow_shulker_box").getString(),
            Component.translatable("block.minecraft.lime_shulker_box").getString(),
            Component.translatable("block.minecraft.green_shulker_box").getString(),
            Component.translatable("block.minecraft.cyan_shulker_box").getString(),
            Component.translatable("block.minecraft.light_blue_shulker_box").getString(),
            Component.translatable("block.minecraft.blue_shulker_box").getString(),
            Component.translatable("block.minecraft.purple_shulker_box").getString(),
            Component.translatable("block.minecraft.magenta_shulker_box").getString(),
            Component.translatable("block.minecraft.pink_shulker_box").getString()
        ));
        ((ConfigStringListAccessor)materialContainersConfig).setDefaultValue(ImmutableList.of(Component.translatable("lpctools.tools.LM.defaultMaterialBoxName").getString()));
    }

    static {
        Registries.CLIENT_RESOURCE_RELOAD.register(_ -> refreshDefaultValues());
        refreshDefaultValues();
        warehouseContainersConfig.resetToDefault();
        materialContainersConfig.resetToDefault();
    }
}
