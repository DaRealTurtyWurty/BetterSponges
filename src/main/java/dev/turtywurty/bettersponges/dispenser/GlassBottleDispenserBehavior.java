package dev.turtywurty.bettersponges.dispenser;

import java.util.List;

import dev.turtywurty.bettersponges.block.PotionSpongeBlock;
import dev.turtywurty.bettersponges.block.entity.PotionSpongeBlockEntity;
import dev.turtywurty.bettersponges.init.BlockEntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;

public class GlassBottleDispenserBehavior extends CopyVanillaDispenseItemBehavior {
    public GlassBottleDispenserBehavior(DispenseItemBehavior behavior) {
        super(behavior);
    }
    
    @Override
    protected ItemStack execute(BlockSource pSource, ItemStack pStack) {
        final ServerLevel level = pSource.getLevel();
        final BlockPos pos = pSource.getPos().relative(pSource.getBlockState().getValue(DispenserBlock.FACING));
        final BlockState state = level.getBlockState(pos);
        
        if (state.getBlock() instanceof PotionSpongeBlock) {
            final PotionSpongeBlockEntity potionSponge = level.getBlockEntity(pos, BlockEntityInit.POTION_SPONGE.get())
                .orElse(null);
            final List<MobEffectInstance> effects = potionSponge.getEffects();
            
            final ItemStack potion = PotionUtils.setCustomEffects(Items.POTION.getDefaultInstance(), effects);
            potion.setHoverName(new TextComponent("Potion of ")
                .append(PotionUtils.getMobEffects(potion).get(0).getEffect().getDisplayName()));
            
            pSource.getLevel()
                .addFreshEntity(new ItemEntity(pSource.getLevel(), pos.getX(), pos.getY(), pos.getZ(), potion));
            pSource.getLevel().setBlockAndUpdate(pos, Blocks.SPONGE.defaultBlockState());

            setSuccess(true);

            final ItemStack copy = pStack.copy();
            copy.shrink(1);
            return super.execute(pSource, copy);
        }

        return super.execute(pSource, pStack);
    }
}
