package dev.turtywurty.bettersponges.dispenser;

import java.util.function.BiPredicate;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;

public class BlockDispenseItemBehaviour extends CopyVanillaDispenseItemBehavior {
    public static final BiPredicate<BlockSource, ItemStack> EMPTY_BLOCK = (source, stack) -> source.getLevel()
        .isEmptyBlock(relative(source));
    private BlockState toPlace;
    private BiPredicate<BlockSource, ItemStack> validationCheck;

    public BlockDispenseItemBehaviour(DispenseItemBehavior vanilla, @NotNull BlockState toPlace,
        @NotNull BiPredicate<BlockSource, ItemStack> validation) {
        super(vanilla);
        this.toPlace = toPlace;
        this.validationCheck = validation;
    }

    public void addAndValidation(@NotNull BiPredicate<BlockSource, ItemStack> check) {
        if (check != null) {
            this.validationCheck = this.validationCheck.and(check);
        }
    }

    public void addOrValidation(@NotNull BiPredicate<BlockSource, ItemStack> check) {
        if (check != null) {
            this.validationCheck = this.validationCheck.or(check);
        }
    }

    public void negativeValidation() {
        this.validationCheck = this.validationCheck.negate();
    }

    public void setToPlace(@NotNull BlockState toPlace) {
        if (toPlace != null) {
            this.toPlace = toPlace;
        }
    }

    public void setValidationCheck(@NotNull BiPredicate<BlockSource, ItemStack> check) {
        if (check != null) {
            this.validationCheck = check;
        }
    }

    @Override
    protected ItemStack execute(BlockSource blockSource, ItemStack stack) {
        if (this.validationCheck.test(blockSource, stack)) {
            final ItemStack copy = stack.copy();
            final Level level = blockSource.getLevel();

            level.setBlockAndUpdate(relative(blockSource), this.toPlace);
            level.gameEvent((Entity) null, GameEvent.BLOCK_PLACE, relative(blockSource));
            copy.shrink(1);
            
            setSuccess(true);
            return copy;
        }

        return super.execute(blockSource, stack);
    }

    public static BiPredicate<BlockSource, ItemStack> isBlock(Block block) {
        return (source, stack) -> source.getLevel().getBlockState(relative(source)).is(block);
    }

    public static BiPredicate<BlockSource, ItemStack> isBlockInTag(TagKey<Block> tag) {
        return (source, stack) -> source.getLevel().getBlockState(relative(source)).is(tag);
    }

    public static BiPredicate<BlockSource, ItemStack> isFluid(Fluid fluid) {
        return (source, stack) -> source.getLevel().getBlockState(relative(source)).getFluidState().is(fluid);
    }

    public static BiPredicate<BlockSource, ItemStack> isFluidInTag(TagKey<Fluid> tag) {
        return (source, stack) -> source.getLevel().getBlockState(relative(source)).getFluidState().is(tag);
    }

    public static BlockPos relative(BlockSource source) {
        return source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
    }
}
