package lpctools.tools.blockBreakRestriction;

import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.ConfigListOptionListConfigEx;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.ObjectListConfig;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.RangeLimitConfig;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.ThirdListConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.BooleanHotkeyConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigBase;
import lpctools.tools.ToolConfigs;
import lpctools.tools.ToolUtils;
import net.minecraft.block.Block;
import org.jetbrains.annotations.NotNull;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;
import static lpctools.tools.blockBreakRestriction.BlockBreakRestrictionData.*;

public class BlockBreakRestriction {
    public static final ThirdListConfig BBConfig = new ThirdListConfig(ToolConfigs.toolConfigs, "BB", false);
    static {listStack.push(BBConfig);}
    public static final BooleanHotkeyConfig blockBreakRestriction = addBooleanHotkeyConfig("blockBreakRestriction", false, null);
    static {ToolUtils.setLPCToolsToggleText(blockBreakRestriction);}
    public static final RangeLimitConfig rangeLimit = addConfig(new RangeLimitConfig(BBConfig, false, "BB"){
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
    public static final BlockTestMethod blockTestMethod_whitelist = blockTestMethod.addList(new BlockTestMethod(blockTestMethod, "whitelist", BlockBreakRestriction::blockTestByWhitelist));
    public static final ObjectListConfig.BlockListConfig blockTestWhiteList = addBlockListConfig(blockTestMethod_whitelist, "whitelist", defaultBlockWhitelist);
    public static final BlockTestMethod blockTestMethod_blacklist = blockTestMethod.addList(new BlockTestMethod(blockTestMethod, "blacklist", BlockBreakRestriction::blockTestByBlacklist));
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
