package dev.turtywurty.bettersponges.dispenser;

import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.world.item.ItemStack;

public class CopyVanillaDispenseItemBehavior extends OptionalDispenseItemBehavior {
    private final DispenseItemBehavior behavior;

    public CopyVanillaDispenseItemBehavior(DispenseItemBehavior behavior) {
        this.behavior = behavior;
    }

    @Override
    protected ItemStack execute(BlockSource pSource, ItemStack pStack) {
        return this.behavior.dispense(pSource, pStack);
    }
}
