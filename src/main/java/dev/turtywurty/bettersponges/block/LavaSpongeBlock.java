package dev.turtywurty.bettersponges.block;

import java.util.Queue;
import java.util.Random;

import com.google.common.collect.Lists;

import dev.turtywurty.bettersponges.init.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class LavaSpongeBlock extends Block {
    public static final int MAX_DEPTH = 6;
    public static final int MAX_COUNT = 64;
    
    public LavaSpongeBlock(Properties properties) {
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
                
                pLevel.addParticle(ParticleTypes.DRIPPING_LAVA, x, y, z, 0.0D, 0.0D, 0.0D);
            }
        }
    }
    
    @Override
    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos,
        boolean pIsMoving) {
        tryAbsorbLava(pLevel, pPos);
        DebugPackets.sendNeighborsUpdatePacket(pLevel, pPos);
    }
    
    @Override
    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
        if (!pOldState.is(pState.getBlock())) {
            tryAbsorbLava(pLevel, pPos);
        }
    }
    
    protected void tryAbsorbLava(Level pLevel, BlockPos pPos) {
        if (removeLava(pLevel, pPos)) {
            pLevel.setBlock(pPos, BlockInit.BURNT_SPONGE.get().defaultBlockState(), 2);
            pLevel.levelEvent(2001, pPos, Block.getId(Blocks.LAVA.defaultBlockState()));
        }
    }
    
    private static boolean removeLava(Level pLevel, BlockPos pPos) {
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
                if (fluidstate.is(FluidTags.LAVA)) {
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