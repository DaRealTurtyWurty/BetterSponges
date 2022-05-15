package dev.turtywurty.bettersponges.block;

import java.util.Queue;
import java.util.Random;

import com.google.common.collect.Lists;

import dev.turtywurty.bettersponges.MixinHooks;
import dev.turtywurty.bettersponges.sponge.ChangingSponge;
import dev.turtywurty.bettersponges.sponge.DryStage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SpongeBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Material;

public class DampSpongeBlock extends SpongeBlock implements ChangingSponge {
    public static final int MAX_DEPTH = 3;
    public static final int MAX_COUNT = 32;
    
    public DampSpongeBlock(Properties properties) {
        super(properties);
    }
    
    @Override
    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, Random pRand) {
        if (pRand.nextInt(5) != 0)
            return;
        
        final Direction dir = Direction.getRandom(pRand);
        if (dir != Direction.UP) {
            final BlockPos blockpos = pPos.relative(dir);
            final BlockState blockstate = pLevel.getBlockState(blockpos);
            if (!pState.canOcclude() || !blockstate.isFaceSturdy(pLevel, blockpos, dir.getOpposite())) {
                double x = pPos.getX();
                double y = pPos.getY();
                double z = pPos.getZ();
                if (dir == Direction.DOWN) {
                    y -= 0.05D;
                    x += pRand.nextDouble();
                    z += pRand.nextDouble();
                } else {
                    y += pRand.nextDouble() * 0.8D;
                    if (dir.getAxis() == Direction.Axis.X) {
                        z += pRand.nextDouble();
                        if (dir == Direction.EAST) {
                            ++x;
                        } else {
                            x += 0.05D;
                        }
                    } else {
                        x += pRand.nextDouble();
                        if (dir == Direction.SOUTH) {
                            ++z;
                        } else {
                            z += 0.05D;
                        }
                    }
                }
                
                pLevel.addParticle(ParticleTypes.DRIPPING_WATER, x, y, z, 0.0D, 0.0D, 0.0D);
            }
        }
    }
    
    @Override
    public DryStage getAge() {
        return DryStage.DAMP;
    }
    
    @Override
    public boolean isRandomlyTicking(BlockState pState) {
        return true;
    }
    
    @Override
    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos,
        boolean pIsMoving) {
        tryAbsorbWater(pLevel, pPos);
        DebugPackets.sendNeighborsUpdatePacket(pLevel, pPos);
    }
    
    @Override
    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
        if (pLevel.dimensionType().ultraWarm()) {
            MixinHooks.convertToLavaSponge(pLevel, pPos);
            pLevel.levelEvent(LevelEvent.PARTICLES_WATER_EVAPORATING, pPos, 0);
            pLevel.playSound((Player) null, pPos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F,
                (1.0F + pLevel.getRandom().nextFloat() * 0.2F) * 0.7F);
        } else if (!pOldState.is(pState.getBlock())) {
            tryAbsorbWater(pLevel, pPos);
        }
    }
    
    @Override
    public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, Random pRandom) {
        MixinHooks.spongeTick(this, pState, pLevel, pPos, pRandom);
    }
    
    @Override
    protected void tryAbsorbWater(Level pLevel, BlockPos pPos) {
        if (removeWater(pLevel, pPos)) {
            pLevel.setBlock(pPos, Blocks.WET_SPONGE.defaultBlockState(), 2);
            pLevel.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pPos, Block.getId(Blocks.WATER.defaultBlockState()));
        }
    }
    
    private static boolean removeWater(Level pLevel, BlockPos pPos) {
        final Queue<Tuple<BlockPos, Integer>> queue = Lists.newLinkedList();
        queue.add(new Tuple<>(pPos, 0));
        int counter = 0;
        
        while (!queue.isEmpty()) {
            final Tuple<BlockPos, Integer> tuple = queue.poll();
            final BlockPos blockpos = tuple.getA();
            final int depth = tuple.getB();
            
            for (final Direction direction : Direction.values()) {
                final BlockPos blockpos1 = blockpos.relative(direction);
                final BlockState blockstate = pLevel.getBlockState(blockpos1);
                final FluidState fluidstate = pLevel.getFluidState(blockpos1);
                final Material material = blockstate.getMaterial();
                if (fluidstate.is(FluidTags.WATER)) {
                    if (blockstate.getBlock() instanceof final BucketPickup bucketPickup
                        && !bucketPickup.pickupBlock(pLevel, blockpos1, blockstate).isEmpty()) {
                        ++counter;
                        if (depth < MAX_DEPTH) {
                            queue.add(new Tuple<>(blockpos1, depth + 1));
                        }
                    } else if (blockstate.getBlock() instanceof LiquidBlock) {
                        pLevel.setBlock(blockpos1, Blocks.AIR.defaultBlockState(), 3);
                        ++counter;
                        if (depth < MAX_DEPTH) {
                            queue.add(new Tuple<>(blockpos1, depth + 1));
                        }
                    } else if (material == Material.WATER_PLANT || material == Material.REPLACEABLE_WATER_PLANT) {
                        final BlockEntity blockentity = blockstate.hasBlockEntity() ? pLevel.getBlockEntity(blockpos1)
                            : null;
                        dropResources(blockstate, pLevel, blockpos1, blockentity);
                        pLevel.setBlock(blockpos1, Blocks.AIR.defaultBlockState(), 3);
                        ++counter;
                        if (depth < MAX_DEPTH) {
                            queue.add(new Tuple<>(blockpos1, depth + 1));
                        }
                    }
                }
            }
            
            if (counter > MAX_COUNT) {
                break;
            }
        }
        
        return counter > 0;
    }
}
