package lpctools.tools.breakRestriction;

import fi.dy.masa.litematica.util.ToBooleanFunction;
import lpctools.lpcfymasaapi.configButtons.derivedConfigs.ConfigListOptionListConfigEx;
import lpctools.lpcfymasaapi.configButtons.derivedConfigs.ObjectListConfig;
import lpctools.lpcfymasaapi.configButtons.derivedConfigs.RangeLimitConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.BooleanHotkeyThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigList;
import lpctools.tools.ToolConfigs;
import lpctools.tools.ToolUtils;
import net.minecraft.block.Block;
import org.jetbrains.annotations.NotNull;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;
import static lpctools.tools.breakRestriction.BreakRestrictionData.*;

public class BreakRestriction {
    public static final BooleanHotkeyThirdListConfig BRConfig = new BooleanHotkeyThirdListConfig(ToolConfigs.toolConfigs, "BR");
    static {ToolUtils.setLPCToolsToggleText(BRConfig);}
    static {listStack.push(BRConfig);}
    public static final RangeLimitConfig rangeLimit = addConfig(new RangeLimitConfig(BRConfig, "BR"){
        @Override public @NotNull String getFullNameTranslationKey() {
            return getFullPath() + ".name";
        }
        @Override public @NotNull String getFullCommentTranslationKey() {
            return getFullPath()  + ".comment";
        }
    });
    static {rangeLimit.setValueChangeCallback(() -> shapeList = rangeLimit.buildShapeList());}
    public static final ConfigListOptionListConfigEx<ToBooleanFunction<Block>> blockTestMethod = addConfigListOptionListConfigEx("blockTestMethod");
    @SuppressWarnings("unused")
    public static final ILPCConfigList blockTestMethod_none = blockTestMethod.addList("none", block->true);
    public static final ILPCConfigList blockTestMethod_whitelist = blockTestMethod.addList("whitelist", BreakRestriction::blockTestByWhitelist);
    public static final ObjectListConfig.BlockListConfig blockTestWhiteList = addBlockListConfig(blockTestMethod_whitelist, "whitelist", defaultBlockWhitelist);
    public static final ILPCConfigList blockTestMethod_blacklist = blockTestMethod.addList("blacklist", BreakRestriction::blockTestByBlacklist);
    public static final ObjectListConfig.BlockListConfig blockTestBlackList = addBlockListConfig(blockTestMethod_blacklist, "blacklist", defaultBlockBlacklist);
    static {listStack.pop();}
    
    private static boolean blockTestByWhitelist(Block block){
        return blockTestWhiteList.contains(block);
    }
    private static boolean blockTestByBlacklist(Block block){
        return !blockTestBlackList.contains(block);
    }
}
