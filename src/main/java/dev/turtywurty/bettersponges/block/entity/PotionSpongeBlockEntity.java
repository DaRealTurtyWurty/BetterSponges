package dev.turtywurty.bettersponges.block.entity;

import java.util.ArrayList;
import java.util.List;

import dev.turtywurty.bettersponges.init.BlockEntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class PotionSpongeBlockEntity extends BlockEntity {
    private Potion origin;
    private final List<MobEffectInstance> effects = new ArrayList<>();
    private int ticks;
    
    public PotionSpongeBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(BlockEntityInit.POTION_SPONGE.get(), pWorldPosition, pBlockState);
    }

    public int getColor() {
        return getColor(this.effects);
    }

    public List<MobEffectInstance> getEffects() {
        return List.copyOf(this.effects);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    
    @Override
    public CompoundTag getUpdateTag() {
        return serializeNBT();
    }
    
    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        
        if (!pTag.contains("Potion", Tag.TAG_STRING))
            return;
        
        this.origin = Potion.byName(pTag.getString("Potion"));

        this.effects.clear();
        final var effects = pTag.getList("Effects", Tag.TAG_COMPOUND);
        for (final Tag tag : effects) {
            this.effects.add(MobEffectInstance.load((CompoundTag) tag));
        }
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        super.onDataPacket(net, pkt);
        handleUpdateTag(pkt.getTag());
    }
    
    public void setPotion(Potion potion, List<MobEffectInstance> effects) {
        if (this.origin != null || !this.effects.isEmpty())
            return;

        this.effects.clear();
        
        this.origin = potion;
        this.effects.addAll(effects);
    }

    public void tick() {
        if (!validateEffects())
            return;
        this.ticks++;

        final List<LivingEntity> touching = this.level.getEntitiesOfClass(LivingEntity.class,
            new AABB(this.worldPosition).inflate(0.05D));

        if (touching.isEmpty())
            return;

        // every 1.5 seconds
        if (this.ticks % 100 == 0) {
            final List<MobEffectInstance> replace = new ArrayList<>();
            for (final MobEffectInstance effect : this.effects) {
                boolean empty = false;
                for (final LivingEntity livingEntity : touching) {
                    livingEntity.addEffect(new MobEffectInstance(effect.getEffect(),
                        effect.getDuration() < 100 ? effect.getDuration() : 100, effect.getAmplifier(),
                        effect.isAmbient(), effect.isVisible(), effect.showIcon()));
                    
                    if (effect.getDuration() <= 100) {
                        empty = true;
                    }
                }
                
                if (!empty) {
                    replace.add(new MobEffectInstance(effect.getEffect(), effect.getDuration() - 100,
                        effect.getAmplifier(), effect.isAmbient(), effect.isVisible(), effect.showIcon()));
                }
            }

            this.effects.clear();
            this.effects.addAll(replace);
            setChanged();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        
        if (this.origin == null)
            return;
        pTag.putString("Potion", this.origin.getRegistryName().toString());
        
        final var effects = new ListTag();
        for (final MobEffectInstance mobEffectInstance : this.effects) {
            final var effectTag = new CompoundTag();
            mobEffectInstance.save(effectTag);
            effects.add(effectTag);
        }
        
        pTag.put("Effects", effects);
    }

    private void revertToSponge() {
        final BlockState sponge = Blocks.SPONGE.defaultBlockState();
        this.level.setBlock(this.worldPosition, sponge, 2);
        this.level.levelEvent(2001, this.worldPosition, Block.getId(sponge));
    }

    private boolean validateEffects() {
        if (this.origin == null || this.effects.isEmpty()) {
            revertToSponge();
            return false;
        }

        for (final MobEffectInstance effect : this.effects) {
            if (effect.getDuration() <= 0) {
                this.effects.remove(effect);
            }
        }
        
        if (this.effects.isEmpty()) {
            revertToSponge();
            return false;
        }

        return true;
    }

    public static int getColor(List<MobEffectInstance> effects) {
        int result = 0xFFFFFF;
        
        if (effects.isEmpty())
            return result;
        
        for (final MobEffectInstance mobEffectInstance : effects) {
            final int color = mobEffectInstance.getEffect().getColor();
            if (result == 0xFFFFFF) {
                result = color;
            } else {
                final int r1 = color >> 8 & 0xFF;
                final int g1 = color >> 4 & 0xFF;
                final int b1 = color & 0xFF;
                
                final int r2 = result >> 8 & 0xFF;
                final int g2 = result >> 4 & 0xFF;
                final int b2 = result & 0xFF;
                
                final int red = (r1 + r2) / 2;
                final int green = (g1 + g2) / 2;
                final int blue = (b1 + b2) / 2;
                
                result = red << 16 | green << 8 | blue;
            }
        }
        
        return result;
    }
}
