package dev.turtywurty.bettersponges.dispenser;

import dev.turtywurty.bettersponges.block.DampSpongeBlock;
import dev.turtywurty.bettersponges.block.LavaSpongeBlock;
import dev.turtywurty.bettersponges.block.PotionSpongeBlock;
import dev.turtywurty.bettersponges.init.BlockInit;
import io.netty.util.internal.ThreadLocalRandom;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.SpongeBlock;
import net.minecraft.world.level.block.WetSpongeBlock;
import net.minecraft.world.level.block.state.BlockState;

public class ShearsDispenserBehavior extends CopyVanillaDispenseItemBehavior {
    public ShearsDispenserBehavior(DispenseItemBehavior behavior) {
        super(behavior);
    }
    
    @Override
    protected ItemStack execute(BlockSource pSource, ItemStack pStack) {
        final ServerLevel level = pSource.getLevel();
        final BlockPos pos = pSource.getPos().relative(pSource.getBlockState().getValue(DispenserBlock.FACING));
        final BlockState state = level.getBlockState(pos);
        final Block block = state.getBlock();
        
        if (block instanceof SpongeBlock || block instanceof DampSpongeBlock || block instanceof WetSpongeBlock
            || block instanceof PotionSpongeBlock || block instanceof LavaSpongeBlock
            || block == BlockInit.BURNT_SPONGE.get()) {
            final ItemStack copy = pStack.copy();
            copy.hurt(1, ThreadLocalRandom.current(), null);
            
            setSuccess(true);
            level.destroyBlock(pos, true);
            
            return super.execute(pSource, copy);
        }
        
        return super.execute(pSource, pStack);
    }
}
