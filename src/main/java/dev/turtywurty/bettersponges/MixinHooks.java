package dev.turtywurty.bettersponges;

import java.util.Random;

import dev.turtywurty.bettersponges.block.entity.PotionSpongeBlockEntity;
import dev.turtywurty.bettersponges.init.BlockInit;
import dev.turtywurty.bettersponges.sponge.ChangingSponge;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;

public class MixinHooks {
    public static void convertToLavaSponge(Level level, BlockPos pos) {
        level.setBlockAndUpdate(pos, BlockInit.LAVA_SPONGE.get().defaultBlockState());
    }

    public static void potionToSponge(Level level, BlockPos pos, Potion potion, boolean flag) {
        final Block block = level.getBlockState(pos).getBlock();
        if (block == Blocks.SPONGE && !flag) {
            level.setBlock(pos, BlockInit.POTION_SPONGE.get().defaultBlockState(), 2);
            if (level.getBlockEntity(pos) instanceof final PotionSpongeBlockEntity potionSponge) {
                potionSponge.setPotion(potion, potion.getEffects());
            }

            level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(Blocks.WATER.defaultBlockState()));
        }
    }

    public static void spongeTick(ChangingSponge block, BlockState pState, ServerLevel pLevel, BlockPos pPos,
        Random pRandom) {
        block.onRandomTick(pState, pLevel, pPos, pRandom);
    }

    public static void waterToSponge(Level level, BlockPos pos, Potion potion) {
        final Block block = level.getBlockState(pos).getBlock();
        if (block == Blocks.SPONGE) {
            level.setBlock(pos, BlockInit.DAMP_SPONGE.get().defaultBlockState(), 2);
            level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(Blocks.WATER.defaultBlockState()));
        } else if (block == BlockInit.DAMP_SPONGE.get()) {
            level.setBlock(pos, Blocks.WET_SPONGE.defaultBlockState(), 2);
            level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(Blocks.WATER.defaultBlockState()));
        }
    }
}
