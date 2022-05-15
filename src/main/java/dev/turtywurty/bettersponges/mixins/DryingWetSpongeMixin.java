package dev.turtywurty.bettersponges.mixins;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;

import dev.turtywurty.bettersponges.MixinHooks;
import dev.turtywurty.bettersponges.sponge.ChangingSponge;
import dev.turtywurty.bettersponges.sponge.DryStage;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WetSpongeBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(WetSpongeBlock.class)
public class DryingWetSpongeMixin extends Block implements ChangingSponge {
    public DryingWetSpongeMixin(Properties properties) {
        super(properties);
    }
    
    @Override
    public DryStage getAge() {
        return DryStage.WET;
    }
    
    @Override
    public boolean isRandomlyTicking(BlockState pState) {
        return true;
    }

    @Override
    public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, Random pRandom) {
        MixinHooks.spongeTick(this, pState, pLevel, pPos, pRandom);
    }
}
