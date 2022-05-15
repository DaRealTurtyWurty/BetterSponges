package dev.turtywurty.bettersponges.mixins;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import dev.turtywurty.bettersponges.MixinHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

@Mixin(ThrownPotion.class)
public abstract class ThrownBottleToSpongeMixin extends ThrowableItemProjectile implements ItemSupplier {
    private ThrownBottleToSpongeMixin(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
    }
    
    //@formatter:off
    @Inject(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/core/BlockPos;relative(Lnet/minecraft/core/Direction;)Lnet/minecraft/core/BlockPos;",
            ordinal = 0
        ),
        method = "Lnet/minecraft/world/entity/projectile/ThrownPotion;onHitBlock(Lnet/minecraft/world/phys/BlockHitResult;)V",
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    //@formatter:on
    public void potionToSponge(BlockHitResult result, CallbackInfo info, ItemStack stack, Potion potion,
        List<MobEffectInstance> list, boolean flag, Direction direction, BlockPos pos) {
        MixinHooks.potionToSponge(this.level, pos, potion, flag);
    }
    
    //@formatter:off
    @Inject(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/projectile/ThrownPotion;dowseFire(Lnet/minecraft/core/BlockPos;)V",
            ordinal = 0
        ),
        method = "Lnet/minecraft/world/entity/projectile/ThrownPotion;onHitBlock(Lnet/minecraft/world/phys/BlockHitResult;)V",
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    //@formatter:on
    public void waterToSponge(BlockHitResult result, CallbackInfo info, ItemStack stack, Potion potion,
        List<MobEffectInstance> list, boolean flag, Direction direction, BlockPos pos, BlockPos offset) {
        MixinHooks.waterToSponge(this.level, pos, potion);
    }
}