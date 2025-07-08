package lpctools.tools.breakRestriction;

import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.ConfigListOptionListConfigEx;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.ObjectListConfig;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.RangeLimitConfig;
import lpctools.lpcfymasaapi.configbutton.uniqueConfigs.BooleanHotkeyThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigBase;
import lpctools.tools.ToolConfigs;
import lpctools.tools.ToolUtils;
import net.minecraft.block.Block;
import org.jetbrains.annotations.NotNull;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;
import static lpctools.tools.breakRestriction.BreakRestrictionData.*;

public class BreakRestriction {
    public static final BooleanHotkeyThirdListConfig BRConfig = new BooleanHotkeyThirdListConfig(ToolConfigs.toolConfigs, "BR", false, false, null, null, false);
    static {ToolUtils.setLPCToolsToggleText(BRConfig);}
    static {listStack.push(BRConfig);}
    public static final RangeLimitConfig rangeLimit = addConfig(new RangeLimitConfig(BRConfig, false, "BR"){
        @Override public @NotNull String getFullNameTranslationKey() {
            return getFullPath() + ".name";
        }
        @Override public @NotNull String getFullCommentTranslationKey() {
            return getFullPath()  + ".comment";
        }
    });
    static {rangeLimit.setValueChangeCallback(() -> shapeList = rangeLimit.buildShapeList());}
    public static final ConfigListOptionListConfigEx<BlockTestMethod> blockTestMethod = addConfigListOptionListConfigEx("blockTestMethod");
    @SuppressWarnings("unused")
    public static final BlockTestMethod blockTestMethod_none = blockTestMethod.addList(new BlockTestMethod(blockTestMethod, "none", block->true));
    public static final BlockTestMethod blockTestMethod_whitelist = blockTestMethod.addList(new BlockTestMethod(blockTestMethod, "whitelist", BreakRestriction::blockTestByWhitelist));
    public static final ObjectListConfig.BlockListConfig blockTestWhiteList = addBlockListConfig(blockTestMethod_whitelist, "whitelist", defaultBlockWhitelist);
    public static final BlockTestMethod blockTestMethod_blacklist = blockTestMethod.addList(new BlockTestMethod(blockTestMethod, "blacklist", BreakRestriction::blockTestByBlacklist));
    public static final ObjectListConfig.BlockListConfig blockTestBlackList = addBlockListConfig(blockTestMethod_blacklist, "blacklist", defaultBlockBlacklist);
    static {listStack.pop();}
    public interface IBlockTestMethod{boolean canBreak(Block block);}
    public static class BlockTestMethod extends LPCConfigList implements IBlockTestMethod{
        public final IBlockTestMethod method;
        public BlockTestMethod(ILPCConfigBase parent, String nameKey, IBlockTestMethod method) {
            super(parent, nameKey);
            this.method = method;
        }
        @Override public boolean canBreak(Block block){
            return method.canBreak(block);
        }
    }
    private static boolean blockTestByWhitelist(Block block){
        return blockTestWhiteList.contains(block);
    }
    private static boolean blockTestByBlacklist(Block block){
        return !blockTestBlackList.contains(block);
    }
}
