package lpctools.tools.fillingAssistant;

import com.google.common.collect.ImmutableSet;
import lpctools.lpcfymasaapi.configButtons.derivedConfigs.*;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.*;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.BlockItemListConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.BlockListConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.BooleanHotkeyThirdListConfig;
import lpctools.tools.ToolUtils;
import lpctools.util.inGame.InGameManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;
import static lpctools.tools.ToolUtils.*;
import static lpctools.tools.fillingAssistant.FillingAssistantData.*;

public class FillingAssistant {
    public static final BooleanHotkeyThirdListConfig FAConfig = ToolUtils.configBuilder("FA").withToolRunner(FillingAssistantRunner::new).build();
    static {listStack.push(FAConfig);}
    public static final ReachDistanceConfig reachDistanceConfig = addReachDistanceConfig(FillingAssistant::reachDistanceConfigCallback);
    public static final IntegerConfig testDistanceConfig = addIntegerConfig("testDistance", 6, 6, 64);
    public static final BooleanConfig disableOnLeftDownConfig = addBooleanConfig("disableOnLeftDown", true);
    public static final BooleanConfig disableOnGUIOpened = addBooleanConfig("disableOnGUIOpened", false);
    public static final BlockItemListConfig placeableItemsConfig = addBlockItemListConfig("placeableItems", defaultPlaceableItemList);
    public static final BlockListConfig passableBlocksConfig = addBlockListConfig("passableBlocks", defaultPassableBlockList);
    public static final BooleanConfig transparentAsPassableConfig = addBooleanConfig("transparentAsPassable", true);
    public static final BooleanConfig notOpaqueAsPassableConfig = addBooleanConfig("notOpaqueAsPassable", true);
    public static final BlockListConfig requiredBlocksConfig = addBlockListConfig("requiredBlocks", defaultRequiredBlockWhiteList);
    public static final InteractionHandConfig interactionHand = addInteractionHandConfig(false);
    public static final RangeLimitConfig limitFillingRange = addRangeLimitConfig();
    public static final ArrayOptionListConfig<OuterRangeBlockMethod> outerRangeBlockMethod = addArrayOptionListConfig(limitFillingRange, "outerRangeBlockMethod", outerRangeBlockMethods);
    static {listStack.pop();}

    private static void reachDistanceConfigCallback(){ testDistanceConfig.setMin((int)reachDistanceConfig.getAsDouble() + 1); }

    public static void disableTool(@Nullable String reasonKey){
        FAConfig.setBooleanValue(false);
        displayDisableReason(FAConfig, reasonKey);
    }
    public static @NotNull ImmutableSet<BlockItem> getPlaceableItems(){return placeableItemsConfig.getBlockItems();}
    public static boolean isBlockUnpassable(Block block){
        if(transparentAsPassableConfig.getAsBoolean() && block.defaultBlockState().propagatesSkylightDown()) return false;
        if(notOpaqueAsPassableConfig.getAsBoolean() && !block.defaultBlockState().canOcclude()) return false;
        return !passableBlocksConfig.contains(block);
    }
    public static boolean isUnpassable(BlockPos pos){
        ClientLevel world = Minecraft.getInstance().level;
        if (world != null){
            Block block = world.getBlockState(pos).getBlock();
            return isBlockUnpassable(block);
        }
        else return true;
    }
    public static boolean required(Block block){return requiredBlocksConfig.contains(block);}
    public static boolean required(InGameManager manager, BlockPos pos){ return required(manager.getBlockState(pos).getBlock()); }
    public interface OuterRangeBlockMethod {
        boolean isBlockUnpassable(Block block);
        default boolean isUnpassable(BlockPos pos, @Nullable BlockGetter world){
            if(world != null) return isBlockUnpassable(world.getBlockState(pos).getBlock());
            else return isBlockUnpassable(Blocks.VOID_AIR);
        }
    }
}
