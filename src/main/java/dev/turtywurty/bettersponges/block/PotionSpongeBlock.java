package dev.turtywurty.bettersponges.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import dev.turtywurty.bettersponges.BetterSponges;
import dev.turtywurty.bettersponges.block.entity.PotionSpongeBlockEntity;
import dev.turtywurty.bettersponges.init.ItemInit;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class PotionSpongeBlock extends Block implements EntityBlock, BlockEntityTicker<PotionSpongeBlockEntity> {
    private static final Component NO_EFFECT = new TranslatableComponent("effect.none").withStyle(ChatFormatting.GRAY);

    public PotionSpongeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack pStack, BlockGetter pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
        super.appendHoverText(pStack, pLevel, pTooltip, pFlag);

        final CompoundTag nbt = pStack.getOrCreateTagElement(BetterSponges.MODID);
        if (!nbt.contains("Potion", Tag.TAG_STRING))
            return;

        final ListTag effects = nbt.getList("Effects", Tag.TAG_COMPOUND);
        final List<MobEffectInstance> mobEffects = new ArrayList<>();
        for (final Tag tag : effects) {
            final CompoundTag effectTag = (CompoundTag) tag;
            final var effect = MobEffectInstance.load(effectTag);
            mobEffects.add(effect);
        }

        addPotionTooltip(mobEffects, pTooltip);
    }

    @Override
    @SuppressWarnings("deprecation")
    public ItemStack getCloneItemStack(BlockGetter pLevel, BlockPos pPos, BlockState pState) {
        final ItemStack item = super.getCloneItemStack(pLevel, pPos, pState);
        if (pLevel.getBlockEntity(pPos) instanceof final PotionSpongeBlockEntity potionSponge) {
            final CompoundTag nbt = item.getOrCreateTagElement(BetterSponges.MODID);
            final CompoundTag saved = potionSponge.saveWithoutMetadata();
            nbt.put("Effects", saved.getList("Effects", Tag.TAG_COMPOUND));
            nbt.putString("Potion", saved.getString("Potion"));
        }
        return item;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState,
        BlockEntityType<T> pBlockEntityType) {
        return pLevel.isClientSide ? null : (BlockEntityTicker<T>) this;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new PotionSpongeBlockEntity(pPos, pState);
    }

    @Override
    public void playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
        final BlockEntity be = pLevel.getBlockEntity(pPos);
        if (be instanceof final PotionSpongeBlockEntity potionSponge && !pLevel.isClientSide && !pPlayer.isCreative()) {
            final ItemStack item = ItemInit.POTION_SPONGE.get().getDefaultInstance();
            final CompoundTag nbt = item.getOrCreateTagElement(BetterSponges.MODID);
            final CompoundTag saved = potionSponge.saveWithoutMetadata();
            nbt.put("Effects", saved.getList("Effects", Tag.TAG_COMPOUND));
            nbt.putString("Potion", saved.getString("Potion"));

            final var entity = new ItemEntity(pLevel, pPos.getX() + 0.5D, pPos.getY() + 0.5D, pPos.getZ() + 0.5D, item);
            entity.setDefaultPickUpDelay();
            pLevel.addFreshEntity(entity);
        }

        super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
        super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
        if (pLevel.getBlockEntity(pPos) instanceof final PotionSpongeBlockEntity potionSponge
            && !pLevel.isClientSide()) {
            final CompoundTag nbt = pStack.getOrCreateTagElement(BetterSponges.MODID);
            if (!nbt.contains("Potion", Tag.TAG_STRING)) {
                nbt.putString("Potion", Potions.EMPTY.getRegistryName().toString());
            }
            
            if (!nbt.contains("Effects", Tag.TAG_LIST)) {
                nbt.put("Effects", new ListTag());
            }
            
            final List<MobEffectInstance> effects = new ArrayList<>();
            for (final Tag tag : nbt.getList("Effects", Tag.TAG_COMPOUND)) {
                effects.add(MobEffectInstance.load((CompoundTag) tag));
            }

            potionSponge.setPotion(Potion.byName(nbt.getString("Potion")), effects);
        }
    }

    @Override
    public void tick(Level pLevel, BlockPos pPos, BlockState pState, PotionSpongeBlockEntity pBlockEntity) {
        pBlockEntity.tick();
    }

    // Thx PotionUtils.java 😏
    private static void addPotionTooltip(List<MobEffectInstance> effects, List<Component> pTooltips) {
        final List<Pair<Attribute, AttributeModifier>> list1 = Lists.newArrayList();
        if (effects.isEmpty()) {
            pTooltips.add(NO_EFFECT);
        } else {
            for (final MobEffectInstance mobeffectinstance : effects) {
                MutableComponent mutablecomponent = new TranslatableComponent(mobeffectinstance.getDescriptionId());
                final MobEffect mobeffect = mobeffectinstance.getEffect();
                final Map<Attribute, AttributeModifier> map = mobeffect.getAttributeModifiers();
                if (!map.isEmpty()) {
                    for (final Entry<Attribute, AttributeModifier> entry : map.entrySet()) {
                        final AttributeModifier attributemodifier = entry.getValue();
                        final AttributeModifier attributemodifier1 = new AttributeModifier(attributemodifier.getName(),
                            mobeffect.getAttributeModifierValue(mobeffectinstance.getAmplifier(), attributemodifier),
                            attributemodifier.getOperation());
                        list1.add(new Pair<>(entry.getKey(), attributemodifier1));
                    }
                }

                if (mobeffectinstance.getAmplifier() > 0) {
                    mutablecomponent = new TranslatableComponent("potion.withAmplifier", mutablecomponent,
                        new TranslatableComponent("potion.potency." + mobeffectinstance.getAmplifier()));
                }

                if (mobeffectinstance.getDuration() > 20) {
                    mutablecomponent = new TranslatableComponent("potion.withDuration", mutablecomponent,
                        MobEffectUtil.formatDuration(mobeffectinstance, 1.0f));
                }

                pTooltips.add(mutablecomponent.withStyle(mobeffect.getCategory().getTooltipFormatting()));
            }
        }

        if (!list1.isEmpty()) {
            pTooltips.add(TextComponent.EMPTY);
            pTooltips.add(new TranslatableComponent("potion.whenDrank").withStyle(ChatFormatting.DARK_PURPLE));

            for (final Pair<Attribute, AttributeModifier> pair : list1) {
                final AttributeModifier attributemodifier2 = pair.getSecond();
                final double d0 = attributemodifier2.getAmount();
                double d1;
                if (attributemodifier2.getOperation() != AttributeModifier.Operation.MULTIPLY_BASE
                    && attributemodifier2.getOperation() != AttributeModifier.Operation.MULTIPLY_TOTAL) {
                    d1 = attributemodifier2.getAmount();
                } else {
                    d1 = attributemodifier2.getAmount() * 100.0D;
                }

                if (d0 > 0.0D) {
                    pTooltips.add(new TranslatableComponent(
                        "attribute.modifier.plus." + attributemodifier2.getOperation().toValue(),
                        ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d1),
                        new TranslatableComponent(pair.getFirst().getDescriptionId())).withStyle(ChatFormatting.BLUE));
                } else if (d0 < 0.0D) {
                    d1 *= -1.0D;
                    pTooltips.add(new TranslatableComponent(
                        "attribute.modifier.take." + attributemodifier2.getOperation().toValue(),
                        ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d1),
                        new TranslatableComponent(pair.getFirst().getDescriptionId())).withStyle(ChatFormatting.RED));
                }
            }
        }
    }
}
