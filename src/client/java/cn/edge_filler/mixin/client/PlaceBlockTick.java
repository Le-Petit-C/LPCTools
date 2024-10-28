package cn.edge_filler.mixin.client;

import cn.edge_filler.Data;
import cn.edge_filler.HandRestock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class PlaceBlockTick {
    @Shadow @Final protected MinecraftClient client;

    @Unique
    private Data.BLOCKTYPE blockType(BlockPos blockPosToCheck) {
        MinecraftClient client = MinecraftClient.getInstance();
        if(client.world == null) return Data.BLOCKTYPE.ERROR;
        Block block = client.world.getBlockState(blockPosToCheck).getBlock();
        if(block == Blocks.AIR
                || block == Blocks.CAVE_AIR
                || block == Blocks.VINE
                || block == Blocks.VOID_AIR
                || block == Blocks.WATER
                || block == Blocks.LAVA
                || block == Blocks.TALL_GRASS
                || block == Blocks.SHORT_GRASS
                || block == Blocks.GLOW_LICHEN
                || block == Blocks.SEAGRASS
                || block == Blocks.TALL_SEAGRASS
                || block == Blocks.BUBBLE_COLUMN
        ){
            return Data.BLOCKTYPE.EMPTY;
        }
        else if(block == Blocks.STONE
                || block == Blocks.DEEPSLATE
                || block == Blocks.GRAVEL
                || block == Blocks.TUFF
                || block == Blocks.DIORITE
                || block == Blocks.GRANITE
                || block == Blocks.ANDESITE
                || block == Blocks.DIRT
                || block == Blocks.GRASS_BLOCK
                || block == Blocks.CLAY
                || block == Blocks.MAGMA_BLOCK
                || block == Blocks.SAND
                || block == Blocks.SMOOTH_BASALT
                || block == Blocks.BEDROCK
                || block == Blocks.OBSIDIAN
                || block == Blocks.NETHERRACK
                || block == Blocks.COBBLESTONE
                || block == Blocks.COBBLED_DEEPSLATE
                || block == Blocks.MOSS_BLOCK
                || block == Blocks.ROOTED_DIRT
        ){
            return Data.BLOCKTYPE.STONE;
        }
        else return Data.BLOCKTYPE.OTHERS;
    }

    @Unique
    Boolean put(BlockPos blockpos){
        if(client.interactionManager == null) return false;
        HandRestock.restock();
        if(!Data.shouldplace) return false;
        Vec3d pos = new Vec3d(blockpos.getX() + 0.5, blockpos.getY() + 0.5, blockpos.getZ() + 0.5);
        BlockHitResult hit = new BlockHitResult(pos, Direction.UP, blockpos, true);
        return client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, hit) == ActionResult.SUCCESS;
    }

    @Unique
    Boolean tryput(BlockPos pos){
        boolean px = blockType(pos.east()) == Data.BLOCKTYPE.STONE,
                nx = blockType(pos.west()) == Data.BLOCKTYPE.STONE,
                py = blockType(pos.up()) == Data.BLOCKTYPE.STONE,
                ny = blockType(pos.down()) == Data.BLOCKTYPE.STONE,
                pz = blockType(pos.south()) == Data.BLOCKTYPE.STONE,
                nz = blockType(pos.north()) == Data.BLOCKTYPE.STONE;
        int count = 0;
        if(px) ++ count;
        if(nx) ++ count;
        if(py) ++ count;
        if(ny) ++ count;
        if(pz) ++ count;
        if(nz) ++ count;
        boolean set;
        if(count < 3) set = false;
        else if(count >= 5) set = true;
        else{
            do{
                set = false;
                boolean ppy = blockType(pos.up().up()) == Data.BLOCKTYPE.STONE;
                boolean nny = blockType(pos.down().down()) == Data.BLOCKTYPE.STONE;
                boolean pxpy = blockType(pos.east().up()) == Data.BLOCKTYPE.STONE,
                        nxpy = blockType(pos.west().up()) == Data.BLOCKTYPE.STONE,
                        pxny = blockType(pos.east().down()) == Data.BLOCKTYPE.STONE,
                        nxny = blockType(pos.west().down()) == Data.BLOCKTYPE.STONE,
                        pxpz = blockType(pos.east().south()) == Data.BLOCKTYPE.STONE,
                        nxpz = blockType(pos.west().south()) == Data.BLOCKTYPE.STONE,
                        pxnz = blockType(pos.east().north()) == Data.BLOCKTYPE.STONE,
                        nxnz = blockType(pos.west().north()) == Data.BLOCKTYPE.STONE,
                        pypz = blockType(pos.up().south()) == Data.BLOCKTYPE.STONE,
                        nypz = blockType(pos.down().south()) == Data.BLOCKTYPE.STONE,
                        pynz = blockType(pos.up().north()) == Data.BLOCKTYPE.STONE,
                        nynz = blockType(pos.down().north()) == Data.BLOCKTYPE.STONE;
                boolean pxpypz = blockType(pos.east().up().south()) == Data.BLOCKTYPE.STONE,
                        nxpypz = blockType(pos.west().up().south()) == Data.BLOCKTYPE.STONE,
                        pxnypz = blockType(pos.east().down().south()) == Data.BLOCKTYPE.STONE,
                        nxnypz = blockType(pos.west().down().south()) == Data.BLOCKTYPE.STONE,
                        pxpynz = blockType(pos.east().up().north()) == Data.BLOCKTYPE.STONE,
                        nxpynz = blockType(pos.west().up().north()) == Data.BLOCKTYPE.STONE,
                        pxnynz = blockType(pos.east().down().north()) == Data.BLOCKTYPE.STONE,
                        nxnynz = blockType(pos.west().down().north()) == Data.BLOCKTYPE.STONE;
                if(ppy && !py){
                    if(!pxpy && !nxpy) break;
                    if(!pypz && !pynz) break;
                }
                if(nny && !ny){
                    if(!pxny && !nxny) break;
                    if(!nypz && !nynz) break;
                }
                if(!px && !py && pxpy && (pxpz || pxpypz || pypz) && (pxnz || pxpynz || pynz)) break;
                if(!nx && !py && nxpy && (nxpz || nxpypz || pypz) && (nxnz || nxpynz || pynz)) break;
                if(!px && !ny && pxny && (pxpz || pxnypz || nypz) && (pxnz || pxnynz || nynz)) break;
                if(!nx && !ny && nxny && (nxpz || nxnypz || nypz) && (nxnz || nxnynz || nynz)) break;
                if(!px && !pz && pxpz && (pxpy || pxpypz || pypz) && (pxny || pxnypz || nypz)) break;
                if(!nx && !pz && nxpz && (nxpy || nxpypz || pypz) && (nxny || nxnypz || nypz)) break;
                if(!px && !nz && pxnz && (pxpy || pxpynz || pynz) && (pxny || pxnynz || nynz)) break;
                if(!nx && !nz && nxnz && (nxpy || nxpynz || pynz) && (nxny || nxnynz || nynz)) break;
                if(!py && !pz && pypz && (pxpy || pxpypz || pxpz) && (nxpy || nxpypz || nxpz)) break;
                if(!ny && !pz && nypz && (pxny || pxnypz || pxpz) && (nxny || nxnypz || nxpz)) break;
                if(!py && !nz && pynz && (pxpy || pxpynz || pxnz) && (nxpy || nxpynz || nxnz)) break;
                if(!ny && !nz && nynz && (pxny || pxnynz || pxnz) && (nxny || nxnynz || nxnz)) break;
                boolean ppypx = blockType(pos.up().up().east()) == Data.BLOCKTYPE.STONE,
                        ppynx = blockType(pos.up().up().west()) == Data.BLOCKTYPE.STONE,
                        ppypz = blockType(pos.up().up().south()) == Data.BLOCKTYPE.STONE,
                        ppynz = blockType(pos.up().up().north()) == Data.BLOCKTYPE.STONE;
                boolean p2xp = px || pxpy,
                        n2xp = nx || nxpy,
                        p2zp = pz || pypz,
                        n2zp = nz || pynz,
                        p2y = py || ppy;
                boolean nnypx = blockType(pos.down().down().east()) == Data.BLOCKTYPE.STONE,
                        nnynx = blockType(pos.down().down().west()) == Data.BLOCKTYPE.STONE,
                        nnypz = blockType(pos.down().down().south()) == Data.BLOCKTYPE.STONE,
                        nnynz = blockType(pos.down().down().north()) == Data.BLOCKTYPE.STONE;
                boolean p2xn = px || pxny,
                        n2xn = nx || nxny,
                        p2zn = pz || nypz,
                        n2zn = nz || nynz,
                        n2y = ny || nny;
                if(!py && !ny) break;
                if(!px && !nx && (pxpy || ppypx || py || ppy || nxpy || ppynx) && (pxny || nnypx || ny || nny || nxny || nnynx)) break;
                if(!pz && !nz && (pypz || ppypz || py || ppy || pynz || ppynz) && (pynz || nnypz || ny || nny || nynz || nnynz)) break;
                if(!p2y){
                    if(!p2xp && ppypx) break;
                    if(!n2xp && ppynx) break;
                    if(!p2zp && ppypz) break;
                    if(!n2zp && ppynz) break;
                }
                if(!n2y){
                    if(!p2xn && nnypx) break;
                    if(!n2xn && nnynx) break;
                    if(!p2zn && nnypz) break;
                    if(!n2zn && nnynz) break;
                }
                boolean pxppypz = blockType(pos.east().up().up().south()) == Data.BLOCKTYPE.STONE,
                        nxppypz = blockType(pos.west().up().up().south()) == Data.BLOCKTYPE.STONE,
                        pxnnypz = blockType(pos.east().down().down().south()) == Data.BLOCKTYPE.STONE,
                        nxnnypz = blockType(pos.west().down().down().south()) == Data.BLOCKTYPE.STONE,
                        pxppynz = blockType(pos.east().up().up().north()) == Data.BLOCKTYPE.STONE,
                        nxppynz = blockType(pos.west().up().up().north()) == Data.BLOCKTYPE.STONE,
                        pxnnynz = blockType(pos.east().down().down().north()) == Data.BLOCKTYPE.STONE,
                        nxnnynz = blockType(pos.west().down().down().north()) == Data.BLOCKTYPE.STONE;
                boolean pxpz2p = pxpz || pxpypz,
                        nxpz2p = nxpz || nxpypz,
                        pxnz2p = pxnz || pxpynz,
                        nxnz2p = nxnz || nxpynz,
                        pxpz2n = pxpz || pxnypz,
                        nxpz2n = nxpz || nxnypz,
                        pxnz2n = pxnz || pxnynz,
                        nxnz2n = nxnz || nxnynz;
                if(!p2xp && !p2zp && pxpz2p && (ppypx || pxppypz || ppypz)) break;
                if(!n2xp && !p2zp && nxpz2p && (ppynx || nxppypz || ppypz)) break;
                if(!p2xp && !n2zp && pxnz2p && (ppypx || pxppynz || ppynz)) break;
                if(!n2xp && !n2zp && nxnz2p && (ppynx || nxppynz || ppynz)) break;
                if(!p2xn && !p2zn && pxpz2n && (nnypx || pxnnypz || nnypz)) break;
                if(!n2xn && !p2zn && nxpz2n && (nnynx || nxnnypz || nnypz)) break;
                if(!p2xn && !n2zn && pxnz2n && (nnypx || pxnnynz || nnynz)) break;
                if(!n2xn && !n2zn && nxnz2n && (nnynx || nxnnynz || nnynz)) break;
                set = true;
            }while(false);
        }
        if(set)
            return put(pos);
        return false;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci){
        if(!Data.shouldplace || client . player == null) return;
        Vec3d eyepos = client.player.getEyePos();
        BlockPos blockpos = new BlockPos((int)Math.floor(eyepos.getX()), (int)Math.floor(eyepos.getY()), (int)Math.floor(eyepos.getZ()));
        boolean loop;
        do {
            loop = false;
            for (int y = -4; y <= 4; ++y) {
                BlockPos p1 = blockpos.add(0, y, 0);
                if (p1.getY() < -64 || p1.getY() >= 320) continue;
                for (int x = -4; x <= 4; ++x) {
                    BlockPos p2 = p1.add(x, 0, 0);
                    for (int z = -4; z <= 4; ++z) {
                        BlockPos pos = p2.add(0, 0, z);
                        Vec3d posd = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
                        if (posd.distanceTo(eyepos) >= 4.0) continue;
                        if (blockType(pos) == Data.BLOCKTYPE.EMPTY) {
                            if(tryput(pos)) loop = true;
                        }
                    }
                }
            }
        }while(loop);
    }
}