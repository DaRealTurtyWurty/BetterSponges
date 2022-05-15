package dev.turtywurty.bettersponges.sponge;

import java.util.Optional;
import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChangeOverTimeBlock;
import net.minecraft.world.level.block.state.BlockState;

public interface BetterChangeOverTimeBlock<Type extends Enum<Type>> {
    int SCAN_DISTANCE = 4;
    float CHANCE_THRESHOLD = 0.05688889F;

    default void applyChangeOverTime(BlockState pState, ServerLevel pLevel, BlockPos pPos, Random pRandom) {
        final int i = this.getAge().ordinal();
        int j = 0;
        int k = 0;

        for (final BlockPos blockpos : BlockPos.withinManhattan(pPos, SCAN_DISTANCE, SCAN_DISTANCE, SCAN_DISTANCE)) {
            final int l = blockpos.distManhattan(pPos);
            if (l > SCAN_DISTANCE) {
                break;
            }

            if (!blockpos.equals(pPos)) {
                final BlockState blockstate = pLevel.getBlockState(blockpos);
                final Block block = blockstate.getBlock();
                if (block instanceof final ChangeOverTimeBlock changeOverTimeBlock) {
                    final Enum<?> age = changeOverTimeBlock.getAge();
                    if (this.getAge().getClass() == age.getClass()) {
                        final int ordinal = age.ordinal();
                        if (ordinal < i)
                            return;

                        if (ordinal > i) {
                            ++k;
                        } else {
                            ++j;
                        }
                    }
                }
            }
        }

        final float modifier = (float) (k + 1) / (float) (k + j + 1);
        final float chance = modifier * modifier * this.getChanceModifier(pState, pLevel, pPos, pRandom);
        if (pRandom.nextFloat() < chance) {
            this.getNext(pState, pLevel, pPos, pRandom).ifPresent(state -> pLevel.setBlockAndUpdate(pPos, state));
        }
    }

    Type getAge();

    float getChanceModifier(BlockState pState, ServerLevel pLevel, BlockPos pPos, Random pRandom);

    Optional<BlockState> getNext(BlockState pState, ServerLevel pLevel, BlockPos pPos, Random pRandom);

    default void onRandomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, Random pRandom) {
        if (pRandom.nextFloat() < CHANCE_THRESHOLD) {
            this.applyChangeOverTime(pState, pLevel, pPos, pRandom);
        }
    }
}