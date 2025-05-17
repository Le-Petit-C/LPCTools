package lpctools.generic;

import com.google.common.collect.ImmutableList;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.ConfigOpenGuiConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.StringListConfig;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;

import java.util.ArrayList;
import java.util.HashSet;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;
import static lpctools.util.DataUtils.*;

public class GenericConfigs {
    public static void init(){
        configOpenGuiConfig = addConfigOpenGuiConfig("Z,C");
        extraSpawnBlockIds = addStringListConfig("extraSpawnBlocks",
                idListFromBlockList(defaultExtraSpawnBlocks),
                ()->{
            extraSpawnBlocks.clear();
            extraSpawnBlocks.addAll(blockSetFromIds(extraSpawnBlockIds));
        });
        extraNoSpawnBlockIds = addStringListConfig("extraNoSpawnBlockIds",
                idListFromBlockList(defaultExtraNoSpawnBlocks),
                ()->{
                    extraNoSpawnBlocks.clear();
                    extraNoSpawnBlocks.addAll(blockSetFromIds(extraNoSpawnBlockIds));
                });
    }

    public static ConfigOpenGuiConfig configOpenGuiConfig;
    public static StringListConfig extraSpawnBlockIds;
    public static StringListConfig extraNoSpawnBlockIds;

    public static final ImmutableList<Block> defaultExtraSpawnBlocks;
    public static final ImmutableList<Block> defaultExtraNoSpawnBlocks;
    static {
        ArrayList<Block> spawnBlocks = new ArrayList<>();
        ArrayList<Block> noSpawnBlocks = new ArrayList<>();
        for(Block block : Registries.BLOCK){
            String id = getBlockId(block);
            if(id.contains("trapdoor")) noSpawnBlocks.add(block);
            if(id.contains("glass") && !id.contains("pane")) noSpawnBlocks.add(block);
        }
        spawnBlocks.add(Blocks.SOUL_SAND);
        spawnBlocks.add(Blocks.MUD);
        noSpawnBlocks.add(Blocks.BEDROCK);
        noSpawnBlocks.add(Blocks.SCAFFOLDING);
        defaultExtraSpawnBlocks = ImmutableList.copyOf(spawnBlocks);
        defaultExtraNoSpawnBlocks = ImmutableList.copyOf(noSpawnBlocks);
    }
    public static final HashSet<Block> extraSpawnBlocks = new HashSet<>(defaultExtraSpawnBlocks);
    public static final HashSet<Block> extraNoSpawnBlocks = new HashSet<>(defaultExtraNoSpawnBlocks);
}
