package dev.turtywurty.bettersponges.sponge;

import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

import dev.turtywurty.bettersponges.init.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public interface ChangingSponge extends BetterChangeOverTimeBlock<DryStage> {
    Supplier<BiMap<Block, Block>> NEXT_BY_BLOCK = Suppliers.memoize(() -> ImmutableBiMap.<Block, Block>builder()
        .put(Blocks.WET_SPONGE, BlockInit.DAMP_SPONGE.get()).put(BlockInit.DAMP_SPONGE.get(), Blocks.SPONGE).build());
    Supplier<BiMap<Block, Block>> PREVIOUS_BY_BLOCK = Suppliers.memoize(() -> NEXT_BY_BLOCK.get().inverse());

    @Override
    default float getChanceModifier(BlockState pState, ServerLevel pLevel, BlockPos pPos, Random pRandom) {
        final BlockState down = pLevel.getBlockState(pPos.below());
        return pLevel.getBrightness(LightLayer.BLOCK, pPos) > 14 || down.getBlock() instanceof BaseFireBlock
            || down.getFluidState().is(Fluids.LAVA) ? 0.9f : 0.75f;
    }

    @Override
    default Optional<BlockState> getNext(BlockState pState, ServerLevel pLevel, BlockPos pPos, Random pRandom) {
        if (pLevel.isRainingAt(pPos.above()))
            return getPrevious(pState.getBlock()).map(block -> block.withPropertiesOf(pState));
        
        if (pLevel.canSeeSky(pPos.above()) && pLevel.dimension() == Level.OVERWORLD)
            return getNext(pState.getBlock()).map(block -> block.withPropertiesOf(pState));
        
        final BlockState down = pLevel.getBlockState(pPos.below());
        if (pLevel.getBrightness(LightLayer.BLOCK, pPos) > 14 || down.getBlock() instanceof BaseFireBlock
            || down.getFluidState().is(Fluids.LAVA))
            return getNext(pState.getBlock()).map(block -> block.withPropertiesOf(pState));
        
        return Optional.empty();
    }

    static Block getFirst(Block block) {
        Block copy = block;

        for (Block block1 = PREVIOUS_BY_BLOCK.get().get(copy); block1 != null; block1 = PREVIOUS_BY_BLOCK.get()
            .get(block1)) {
            copy = block1;
        }

        return copy;
    }

    static BlockState getFirst(BlockState state) {
        return getFirst(state.getBlock()).withPropertiesOf(state);
    }

    static Optional<Block> getNext(Block block) {
        return Optional.ofNullable(NEXT_BY_BLOCK.get().get(block));
    }
    
    static Optional<Block> getPrevious(Block block) {
        return Optional.ofNullable(PREVIOUS_BY_BLOCK.get().get(block));
    }
    
    static Optional<BlockState> getPrevious(BlockState state) {
        return getPrevious(state.getBlock()).map(block -> block.withPropertiesOf(state));
    }
}
