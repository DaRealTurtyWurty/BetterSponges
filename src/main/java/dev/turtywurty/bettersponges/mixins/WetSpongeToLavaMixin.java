package dev.turtywurty.bettersponges.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.turtywurty.bettersponges.MixinHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WetSpongeBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(WetSpongeBlock.class)
public class WetSpongeToLavaMixin {
    //@formatter:off
    @Inject(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;levelEvent(ILnet/minecraft/core/BlockPos;I)V"
        ),
        method = "Lnet/minecraft/world/level/block/WetSpongeBlock;onPlace(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)V"
    )
    //@formatter:on
    private void toLavaSponge(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving,
        CallbackInfo info) {
        MixinHooks.convertToLavaSponge(level, pos);
    }
}
